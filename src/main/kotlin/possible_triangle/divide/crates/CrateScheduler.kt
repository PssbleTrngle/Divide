package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.Config
import possible_triangle.divide.crates.CrateEvents.CRATE_UUID_TAG
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.callbacks.CleanCallback
import possible_triangle.divide.crates.callbacks.FillLootCallback
import possible_triangle.divide.crates.callbacks.MessageCallback
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.PerTeamData
import possible_triangle.divide.extensions.blocksIn
import possible_triangle.divide.data.spawnMarker
import possible_triangle.divide.events.Countdown
import possible_triangle.divide.extensions.*
import possible_triangle.divide.logic.Teams
import java.util.*
import kotlin.random.Random

object CrateScheduler {

    internal val COUNTDOWN = Countdown("loot_crate", "Loot Drop")

    private val ORDERED_MAX = Dynamic3CommandExceptionType { item, max, already ->
        Component.literal("You can only order $max of ").append(item as Component)
            .append(", your team already ordered $already")
    }

    private val ORDERS =
        PerTeamData<MutableMap<Order?, MutableList<ItemStack>>, CompoundTag>("orders", mutableMapOf(), { orders ->
            val tag = CompoundTag()
            orders.filter { it.value.isNotEmpty() }.forEach { (order, stacks) ->
                val list = ListTag()
                stacks.forEach { ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it).get().ifLeft(list::add) }
                tag.put(order?.id ?: "", list)
            }
            tag
        }, { tag ->
            tag.allKeys.associate { key ->
                val list = tag.getList(key, 10)
                val order = Order[key]
                val stacks =
                    list.map { ItemStack.CODEC.parse(NbtOps.INSTANCE, it).get().left().orElse(ItemStack.EMPTY) }
                        .filterNot { it.isEmpty }.toMutableList()
                order to stacks
            }.toMutableMap()
        })

    fun markersAt(server: MinecraftServer, pos: BlockPos): List<Entity> {
        return server.mainWorld().getEntitiesOfClass(Entity::class.java, AABB(pos).inflate(0.5)) {
            it.tags.contains(CRATE_UUID_TAG)
        }
    }

    fun crateAt(
        server: MinecraftServer,
        pos: BlockPos,
        ignoreTag: Boolean = false,
        uuid: UUID? = null,
    ): RandomizableContainerBlockEntity? {
        val tile = server.mainWorld().getBlockEntity(pos)
        if (tile is RandomizableContainerBlockEntity) {
            val isCrate = tile.tileData().hasUUID(CRATE_UUID_TAG)
            if (ignoreTag) return tile
            else if (isCrate && (uuid == null || uuid == tile.tileData().getUUID(CRATE_UUID_TAG))) return tile
        }
        return null
    }

    fun setLock(container: RandomizableContainerBlockEntity, lock: String?) {
        val nbt = container.saveWithoutMetadata()
        if (lock != null) nbt.putString("Lock", lock)
        else nbt.remove("Lock")
        container.load(nbt)
    }

    fun getOrders(server: MinecraftServer, team: PlayerTeam): Map<Order, Int> {
        return ORDERS[server][team].filterKeys { it != null }.mapKeys { it.key as Order }
            .mapValues { it.value.sumOf { stack -> stack.count } }.filter { it.value > 0 }
    }

    private fun addOrder(server: MinecraftServer, team: PlayerTeam? = null, order: Order? = null, stack: ItemStack) {
        val orders = ORDERS[server]
        val stacks = orders[team].getOrPut(order) { mutableListOf() }
        stacks.add(stack.copy())
        orders.setDirty()
    }

    fun saveItem(server: MinecraftServer, stack: ItemStack) {
        stack.setLore(Component.literal("Saved").withStyle(ChatFormatting.GREEN))
        var chance = Config.CONFIG.crate.itemSaveChance
        if (!stack.isStackable) chance *= 4
        if (Random.nextDouble() <= chance) addOrder(server, stack = stack)
    }

    fun order(server: MinecraftServer, team: PlayerTeam, stack: ItemStack, order: Order) {
        val stacks = ORDERS[server][team][order]

        val already = stacks?.sumOf { it.count } ?: 0
        if (order.max != null && already + stack.count > order.max) throw ORDERED_MAX.create(
            stack.displayName, order.max, already
        )

        addOrder(server, team, order, stack)
    }

    fun findInRange(server: MinecraftServer, center: BlockPos, range: Double): BlockPos? {
        val world = server.mainWorld()
        return AABB(center).inflate(range, 10.0, range).blocksIn().asSequence()
            .filter { world.worldBorder.isWithinBounds(it) }
            .filter {
                val below = it.below()
                world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)
            }.filter { pos ->
                val column = (0..3).map { pos.above(it) }
                column.all { world.isAir(it) }
            }.filter { pos ->
                val surrounding = AABB(pos).inflate(7.0, 4.0, 7.0).blocksIn()
                surrounding.count { world.isAir(it) } >= 15
            }.sortedBy { it.distManhattan(center) }.firstOrNull()
    }

    private fun spawnCrate(server: MinecraftServer, pos: BlockPos): UUID {
        val state = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP)
        server.mainWorld().setBlock(pos, state, 2)

        val crate = crateAt(server, pos, true) ?: throw NullPointerException("crate missing at $pos")
        val uuid = UUID.randomUUID()
        crate.tileData().putUUID(CRATE_UUID_TAG, uuid)
        crate.tileData().putBoolean(UNBREAKABLE_TAG, true)
        crate.customName = Component.literal("Loot Crate")
        setLock(crate, UUID.randomUUID().toString())
        return uuid
    }

    fun prepare(
        server: MinecraftServer,
        seconds: Int,
        pos: BlockPos,
        type: CrateLoot,
        withoutOrders: Boolean = false,
    ): Long {
        val time = server.time() + seconds * 20

        val uuid = spawnCrate(server, pos)
        val marker = spawnMarker(EntityType.SLIME, server.mainWorld(), pos) {
            it.putInt("Size", 0)
        }
        marker.tags.add(CRATE_UUID_TAG)

        COUNTDOWN.countdown(server, seconds)
        COUNTDOWN.bar(server).isVisible = true
        COUNTDOWN.bar(server).color = BossEvent.BossBarColor.YELLOW

        val orders = if (withoutOrders) emptyList() else {
            val items = ORDERS[server].values.map { it.value.values }.flatten().flatten().filterNot { it.isEmpty }
                .map { it.copy() }
            ORDERS[server].values.keys.forEach {
                ORDERS[server][it] = mutableMapOf()
            }
            items
        }

        FillLootCallback.schedule(server, seconds, FillLootCallback(pos, type, orders, uuid))
        CleanCallback.schedule(server, seconds + Config.CONFIG.crate.cleanUpTime, CleanCallback(pos, uuid))

        return time
    }

    fun schedule(server: MinecraftServer, seconds: Int, pos: BlockPos, type: CrateLoot) {
        val time = prepare(server, seconds, pos, type)

        val teams = Teams.ranked(server).reversed()
        teams.forEachIndexed { index, team ->
            scheduleMessage(server, seconds * index / teams.size, pos, time, team)
        }
    }

    fun scheduleMessage(server: MinecraftServer, seconds: Int, pos: BlockPos, time: Long, team: PlayerTeam) {
        MessageCallback.schedule(
            server, seconds, MessageCallback(team.name, pos, time), suffix = team.color.name.lowercase()
        )
    }

}