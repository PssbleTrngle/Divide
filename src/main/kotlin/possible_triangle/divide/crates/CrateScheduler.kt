package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
import net.minecraft.block.BarrelBlock
import net.minecraft.block.Blocks
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.boss.BossBar
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import possible_triangle.divide.Config
import possible_triangle.divide.crates.CrateEvents.CRATE_UUID_TAG
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.callbacks.CleanCallback
import possible_triangle.divide.crates.callbacks.FillLootCallback
import possible_triangle.divide.crates.callbacks.MessageCallback
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.PerTeamData
import possible_triangle.divide.data.Util.blocksIn
import possible_triangle.divide.data.Util.spawnMarker
import possible_triangle.divide.events.Countdown
import possible_triangle.divide.extensions.tileData
import possible_triangle.divide.logic.Teams
import java.util.*
import kotlin.random.Random

object CrateScheduler {

    internal val COUNTDOWN = Countdown("loot_crate", "Loot Drop")

    private val ORDERED_MAX = Dynamic3CommandExceptionType { item, max, already ->
        Text.literal("You can only order $max of ").append(item as Text).append(", your team already ordered $already")
    }

    private val ORDERS =
        PerTeamData<MutableMap<Order?, MutableList<ItemStack>>, NbtCompound>("orders", mutableMapOf(), { orders ->
            val tag = NbtCompound()
            orders.filter { it.value.isNotEmpty() }.forEach { (order, stacks) ->
                val list = NbtList()
                stacks.forEach { ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it).get().ifLeft(list::add) }
                tag.put(order?.id ?: "", list)
            }
            tag
        }, { tag ->
            tag.keys.associate { key ->
                val list = tag.getList(key, 10)
                val order = Order[key]
                val stacks =
                    list.map { ItemStack.CODEC.parse(NbtOps.INSTANCE, it).get().left().orElse(ItemStack.EMPTY) }
                        .filterNot { it.isEmpty }.toMutableList()
                order to stacks
            }.toMutableMap()
        })

    fun markersAt(server: MinecraftServer, pos: BlockPos): List<Entity> {
        return server.overworld.getEntitiesByClass(Entity::class.java, Box(pos).expand(0.5)) {
            it.scoreboardTags.contains(CRATE_UUID_TAG)
        }
    }

    fun crateAt(
        server: MinecraftServer,
        pos: BlockPos,
        ignoreTag: Boolean = false,
        uuid: UUID? = null,
    ): LootableContainerBlockEntity? {
        val tile = server.overworld.getBlockEntity(pos)
        if (tile is LootableContainerBlockEntity) {
            val isCrate = tile.tileData().containsUuid(CRATE_UUID_TAG)
            if (ignoreTag) return tile
            else if (isCrate && (uuid == null || uuid == tile.tileData().getUuid(CRATE_UUID_TAG))) return tile
        }
        return null
    }

    fun setLock(container: LootableContainerBlockEntity, lock: String?) {
        val nbt = container.createNbt()
        if (lock != null) nbt.putString("Lock", lock)
        else nbt.remove("Lock")
        container.readNbt(nbt)
    }

    fun getOrders(server: MinecraftServer, team: Team): Map<Order, Int> {
        return ORDERS[server][team].filterKeys { it != null }.mapKeys { it.key as Order }
            .mapValues { it.value.sumOf { stack -> stack.count } }.filter { it.value > 0 }
    }

    private fun addOrder(server: MinecraftServer, team: Team? = null, order: Order? = null, stack: ItemStack) {
        val orders = ORDERS[server]
        val stacks = orders[team].getOrPut(order) { mutableListOf() }
        stacks.add(stack.copy())
        orders.isDirty = true
    }

    fun saveItem(server: MinecraftServer, stack: ItemStack) {

        val lore = listOf(Text.literal("Saved").formatted(Formatting.GREEN)).map { Text.Serializer.toJson(it) }
            .mapTo(NbtList()) { NbtString.of(it) }

        stack.orCreateNbt.getCompound("display").put("Lore", lore)

        var chance = Config.CONFIG.crate.itemSaveChance
        if (!stack.isStackable) chance *= 4
        if (Random.nextDouble() <= chance) addOrder(server, stack = stack)
    }

    fun order(server: MinecraftServer, team: Team, stack: ItemStack, order: Order) {
        val stacks = ORDERS[server][team][order]

        val already = stacks?.sumOf { it.count } ?: 0
        if (order.max != null && already + stack.count > order.max) throw ORDERED_MAX.create(
            stack.name, order.max, already
        )

        addOrder(server, team, order, stack)
    }

    fun findInRange(server: MinecraftServer, center: BlockPos, range: Double): BlockPos? {
        val world = server.overworld
        return blocksIn(Box(center).expand(range, 10.0, range)).asSequence().filter { world.worldBorder.contains(it) }
            .filter {
                val below = it.down()
                world.getBlockState(below).hasSolidTopSurface(world, below, null)
            }.filter { pos ->
                val column = (0..3).map { pos.up(it) }
                column.all { world.isAir(it) }
            }.filter { pos ->
                val surrounding = blocksIn(Box(pos).expand(7.0, 4.0, 7.0))
                surrounding.count { world.isAir(it) } >= 15
            }.sortedBy { it.getManhattanDistance(center) }.firstOrNull()
    }

    private fun spawnCrate(server: MinecraftServer, pos: BlockPos): UUID {
        val state = Blocks.BARREL.defaultState.with(BarrelBlock.FACING, Direction.UP)
        server.overworld.setBlockState(pos, state, 2)

        val crate = crateAt(server, pos, true) ?: throw NullPointerException("crate missing at $pos")
        val uuid = UUID.randomUUID()
        crate.tileData().putUuid(CRATE_UUID_TAG, uuid)
        crate.tileData().putBoolean(UNBREAKABLE_TAG, true)
        crate.customName = Text.literal("Loot Crate")
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
        val world = server.overworld
        val time = world.time + seconds * 20

        val uuid = spawnCrate(server, pos)
        val marker = spawnMarker(EntityType.SLIME, world, pos) {
            it.putInt("Size", 0)
        }
        marker.scoreboardTags.add(CRATE_UUID_TAG)

        COUNTDOWN.countdown(server, seconds)
        COUNTDOWN.bar(server).isVisible = true
        COUNTDOWN.bar(server).color = BossBar.Color.YELLOW

        val orders = if (withoutOrders) listOf() else {
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

    fun scheduleMessage(server: MinecraftServer, seconds: Int, pos: BlockPos, time: Long, team: Team) {
        MessageCallback.schedule(
            server, seconds, MessageCallback(team.name, pos, time), suffix = team.color.name.lowercase()
        )
    }

}