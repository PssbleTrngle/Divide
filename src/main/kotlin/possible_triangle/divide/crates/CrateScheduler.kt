package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
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
import possible_triangle.divide.crates.CrateEvents.CRATE_TAG
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.callbacks.CleanCallback
import possible_triangle.divide.crates.callbacks.FillLootCallback
import possible_triangle.divide.crates.callbacks.MessageCallback
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.PerTeamData
import possible_triangle.divide.logic.Glowing
import possible_triangle.divide.logic.TeamLogic
import java.util.*
import java.util.stream.Collectors
import kotlin.random.Random

object CrateScheduler {

    private val ORDERED_MAX =
        Dynamic3CommandExceptionType { item, max, already ->
            TextComponent("You can only order $max of ")
                .append(item as Component)
                .append(TextComponent(", your team already ordered $already"))
        }

    private val ORDERS =
        PerTeamData<MutableMap<Order?, MutableList<ItemStack>>, CompoundTag>("orders", mutableMapOf(), { orders ->
            val tag = CompoundTag()
            orders.forEach { (order, stacks) ->
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

    fun crateAt(server: MinecraftServer, pos: BlockPos, ignoreTag: Boolean = false): RandomizableContainerBlockEntity? {
        val tile = server.overworld().getBlockEntity(pos)
        if (tile is RandomizableContainerBlockEntity) {
            if (ignoreTag) return tile
            else if (tile.tileData.getBoolean((CRATE_TAG))) return tile
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
        if (Random.nextDouble() <= Config.CONFIG.crate.itemSaveChance) addOrder(server, stack = stack)
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

    fun spawnCrate(server: MinecraftServer, pos: BlockPos) {
        val state = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP)
        server.overworld().setBlock(pos, state, 2)

        val crate = crateAt(server, pos, true) ?: throw NullPointerException("crate missing at $pos")
        crate.tileData.putBoolean(CRATE_TAG, true)
        crate.tileData.putBoolean(UNBREAKABLE_TAG, true)
        setLock(crate, UUID.randomUUID().toString())
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
        marker.tags.add(CRATE_TAG)
        marker.addEffect(
            MobEffectInstance(
                MobEffects.INVISIBILITY,
                1000000,
                0,
                false,
                false
            )
        )

        Glowing.addReason(marker, server.playerList.players, 1000000)
    }

    fun schedule(server: MinecraftServer, seconds: Int, pos: BlockPos) {
        val world = server.overworld()

        spawnCrate(server, pos)
        spawnMarker(server, pos)

        val time = world.gameTime + seconds * 20

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
            FillLootCallback(pos, CrateLoot.random(), orders)
        )

        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crate_cleanup",
            time + 20 * Config.CONFIG.crate.cleanUpTime,
            CleanCallback(pos)
        )

        val teams = TeamLogic.ranked(server).reversed()
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