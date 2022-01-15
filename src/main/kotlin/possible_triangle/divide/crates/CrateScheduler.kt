package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
import net.minecraft.ChatFormatting.GREEN
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.phys.AABB
import net.minecraft.world.scores.Team
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateEvents.CRATE_UUID_TAG
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.callbacks.CleanCallback
import possible_triangle.divide.crates.callbacks.FillLootCallback
import possible_triangle.divide.crates.callbacks.MessageCallback
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.PerTeamData
import possible_triangle.divide.events.Countdown
import possible_triangle.divide.logic.Teams
import java.util.*
import java.util.stream.Collectors
import kotlin.random.Random

object CrateScheduler {

    internal val COUNTDOWN = Countdown("loot_crate", "Loot Drop")

    private val ORDERED_MAX =
        Dynamic3CommandExceptionType { item, max, already ->
            TextComponent("You can only order $max of ")
                .append(item as Component)
                .append(TextComponent(", your team already ordered $already"))
        }

    private val ORDERS =
        PerTeamData<MutableMap<Order?, MutableList<ItemStack>>, CompoundTag>("orders", mutableMapOf(), { orders ->
            val tag = CompoundTag()
            orders.filter { it.value.isNotEmpty() }.forEach { (order, stacks) ->
                val list = ListTag()
                stacks.forEach { ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it).get().ifLeft(list::add) }
                tag.put(if (order != null) Order.idOf(order) else "", list)
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

    init {
        TimerCallbacks.SERVER_CALLBACKS.register(FillLootCallback.Serializer)
        TimerCallbacks.SERVER_CALLBACKS.register(MessageCallback.Serializer)
        TimerCallbacks.SERVER_CALLBACKS.register(CleanCallback.Serializer)
    }

    fun markersAt(server: MinecraftServer, pos: BlockPos): List<Entity> {
        return server.overworld().getEntitiesOfClass(Entity::class.java, AABB(pos).inflate(0.5)) {
            it.tags.contains(CRATE_UUID_TAG)
        }
    }

    fun crateAt(
        server: MinecraftServer,
        pos: BlockPos,
        ignoreTag: Boolean = false,
        uuid: UUID? = null
    ): RandomizableContainerBlockEntity? {
        val tile = server.overworld().getBlockEntity(pos)
        if (tile is RandomizableContainerBlockEntity) {
            val isCrate = tile.tileData.hasUUID(CRATE_UUID_TAG)
            if (ignoreTag) return tile
            else if (isCrate && (uuid == null || uuid == tile.tileData.getUUID(CRATE_UUID_TAG))) return tile
        }
        return null
    }

    fun setLock(container: RandomizableContainerBlockEntity, lock: String?) {
        val nbt = container.save(CompoundTag())
        if (lock != null) nbt.putString("Lock", lock)
        else nbt.remove("Lock")
        container.load(nbt)
    }

    fun blocksIn(aabb: AABB): List<BlockPos> {
        return BlockPos.betweenClosedStream(aabb)
            .map { BlockPos(it) }
            .collect(Collectors.toList())
    }

    private fun addOrder(server: MinecraftServer, team: Team? = null, order: Order? = null, stack: ItemStack) {
        val orders = ORDERS[server]
        val stacks = orders[team].getOrPut(order) { mutableListOf() }
        stacks.add(stack.copy())
        orders.setDirty()
    }

    fun saveItem(server: MinecraftServer, stack: ItemStack) {

        val lore = listOf(TextComponent("Saved").withStyle(GREEN))
            .map { Component.Serializer.toJson(it) }
            .mapTo(ListTag()) { StringTag.valueOf(it) }

        stack.orCreateTag.getCompound("display").put("Lore", lore)

        var chance =  Config.CONFIG.crate.itemSaveChance
        if(!stack.isStackable) chance *= 4
        if (Random.nextDouble() <= chance) addOrder(server, stack = stack)
    }

    fun order(server: MinecraftServer, team: Team, stack: ItemStack, order: Order) {
        val stacks = ORDERS[server][team][order]

        val already = stacks?.sumOf { it.count } ?: 0
        if (order.max != null && already + stack.count > order.max) throw ORDERED_MAX.create(
            stack.displayName,
            order.max,
            already
        )

        addOrder(server, team, order, stack)
    }

    fun findInRange(server: MinecraftServer, center: BlockPos, range: Double): BlockPos? {
        val world = server.overworld()
        return blocksIn(AABB(center).inflate(range, 10.0, range))
            .asSequence()
            .filter { world.worldBorder.isWithinBounds(it) }
            .filter { it ->
                val below = it.below()
                world.isStateAtPosition(below) {
                    it.isFaceSturdy(
                        world,
                        below,
                        Direction.UP
                    )
                }
            }
            .filter { pos ->
                val column = (0..3).map { pos.above(it) }
                column.all { world.isStateAtPosition(it) { state -> state.isAir } }
            }.filter { pos ->
                val surrounding = blocksIn(AABB(pos).inflate(7.0, 4.0, 7.0))
                surrounding.count { world.isStateAtPosition(it) { state -> state.isAir } } >= 15
            }
            .sortedBy { it.distManhattan(center) }
            .firstOrNull()
    }

    fun spawnCrate(server: MinecraftServer, pos: BlockPos): UUID {
        val state = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP)
        server.overworld().setBlock(pos, state, 2)

        val crate = crateAt(server, pos, true) ?: throw NullPointerException("crate missing at $pos")
        val uuid =  UUID.randomUUID()
        crate.tileData.putUUID(CRATE_UUID_TAG, uuid)
        crate.tileData.putBoolean(UNBREAKABLE_TAG, true)
        crate.customName = TextComponent("Loot Crate")
        setLock(crate, UUID.randomUUID().toString())
        return uuid
    }

    fun spawnMarker(server: MinecraftServer, pos: BlockPos) {
        val marker = EntityType.SLIME.create(server.overworld()) ?: throw NullPointerException()
        val nbt = CompoundTag()
        nbt.putBoolean("NoAI", true)
        nbt.putInt("Size", 0)
        nbt.putBoolean("Invulnerable", true)

        marker.deserializeNBT(nbt)
        marker.moveTo(pos.x + 0.5, pos.y + 0.25, pos.z + 0.5)
        server.overworld().addFreshEntity(marker)
        marker.tags.add(CRATE_UUID_TAG)
        marker.addEffect(
            MobEffectInstance(
                MobEffects.INVISIBILITY,
                1000000,
                0,
                false,
                false
            )
        )
    }

    fun schedule(server: MinecraftServer, seconds: Int, pos: BlockPos, type: CrateLoot) {
        val world = server.overworld()

        val uuid = spawnCrate(server, pos)
        spawnMarker(server, pos)

        val time = world.gameTime + seconds * 20
        COUNTDOWN.countdown(server, seconds)
        COUNTDOWN.bar(server).isVisible = true
        COUNTDOWN.bar(server).color = BossEvent.BossBarColor.YELLOW

        val orders = ORDERS[server].values.map { it.value.values }
            .flatten().flatten()
            .filterNot { it.isEmpty }
            .map { it.copy() }

        ORDERS[server].values.keys.forEach {
            ORDERS[server][it] = mutableMapOf()
        }

        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crate",
            time,
            FillLootCallback(pos, type, orders, uuid)
        )

        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crate_cleanup",
            time + 20 * Config.CONFIG.crate.cleanUpTime,
            CleanCallback(pos, uuid)
        )

        val teams = Teams.ranked(server).reversed()
        teams.forEachIndexed { index, team ->
            scheduleMessage(server, seconds * index / teams.size, pos, time, team)
        }
    }

    private fun scheduleMessage(server: MinecraftServer, seconds: Int, pos: BlockPos, time: Long, team: Team) {
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crate_message_${team.color.name.lowercase()}",
            server.overworld().gameTime + seconds * 20,
            MessageCallback(team.name, pos, time)
        )
    }

}