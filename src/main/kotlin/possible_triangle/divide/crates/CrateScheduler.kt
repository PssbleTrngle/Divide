package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import net.minecraft.world.phys.AABB
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.Glowing
import possible_triangle.divide.logic.TeamLogic
import java.util.*
import java.util.stream.Collectors

@Mod.EventBusSubscriber
object CrateScheduler {

    init {
        TimerCallbacks.SERVER_CALLBACKS.register(Serializer)
    }

    @SubscribeEvent
    fun preventBreaking(event: PlayerEvent.BreakSpeed) {
        val crate = crateAt(event.entity.server ?: return, event.pos) ?: return
        val unbreakableUntil = crate.tileData.getLong("${DivideMod.ID}:unbreakable_until")
        if (event.entity.level.gameTime <= unbreakableUntil) event.newSpeed = 0F
    }

    fun crateAt(server: MinecraftServer, pos: BlockPos): RandomizableContainerBlockEntity? {
        val tile = server.overworld().getBlockEntity(pos)
        if (tile is RandomizableContainerBlockEntity) return tile
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

    fun schedule(server: MinecraftServer, seconds: Int, pos: BlockPos) {
        val world = server.overworld()

        val state = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP)
        world.setBlock(pos, state, 2)

        val crate = crateAt(server, pos) ?: throw NullPointerException("crate missing at $pos")
        crate.tileData.putBoolean("${DivideMod.ID}:crate_marker", true)
        crate.tileData.putLong("${DivideMod.ID}:unbreakable_until", world.gameTime + seconds.plus(10) * 20)
        setLock(crate, UUID.randomUUID().toString())

        val marker = EntityType.SLIME.create(world) ?: throw NullPointerException()
        val nbt = CompoundTag()
        nbt.putBoolean("NoAI", true)
        nbt.putInt("Size", 0)
        nbt.putBoolean("Invulnerable", true)

        marker.deserializeNBT(nbt)
        marker.moveTo(pos.x + 0.5, pos.y + 0.25, pos.z + 0.5)
        server.overworld().addFreshEntity(marker)
        marker.tags.add("${DivideMod.ID}:crate_marker")
        marker.addEffect(
            MobEffectInstance(
                MobEffects.INVISIBILITY,
                seconds.plus(10) * 20,
                0,
                false,
                false
            )
        )


        Glowing.addReason(marker, server.playerList.players, seconds)

        val time = world.gameTime + seconds * 20
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crates",
            time,
            Callback(pos, listOf())
        )

        val teams = TeamLogic.ranked(server).reversed()
        teams.forEachIndexed { index, team ->
            CrateMessages.scheduleMessage(server, seconds * index / teams.size, pos, time, team)
        }
    }


    class Callback(val pos: BlockPos, val orders: List<ItemStack>) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            val loot = CrateLoot.generate()
            val crate = crateAt(server, pos)

            if (crate != null) {
                setLock(crate, null)
                val slots = (0 until crate.containerSize).toList().shuffled()
                slots.forEachIndexed { i, slot ->
                    crate.setItem(slot, loot.getOrElse(i) { ItemStack.EMPTY })
                }

                server.playerList.players.forEach {
                    server.overworld()
                        .sendParticles(
                            it, ParticleTypes.FIREWORK, false,
                            pos.x + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5,
                            20, 0.5, 0.5, 0.5, 0.1
                        )
                }
            }

            server.overworld().getEntitiesOfClass(Entity::class.java, AABB(pos).inflate(2.0)) {
                it.tags.contains("${DivideMod.ID}:crate_marker")
            }.forEach {
                it.remove(Entity.RemovalReason.DISCARDED)
            }
        }

    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, Callback>(
            ResourceLocation(DivideMod.ID, "crates"),
            Callback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: Callback) {
            nbt.put("pos", NbtUtils.writeBlockPos(callback.pos))
            val list = ListTag()
            callback.orders.forEach {
                val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it)
                encoded.get().ifLeft(list::add)
            }
            nbt.put("orders", list)
        }

        override fun deserialize(nbt: CompoundTag): Callback {
            val pos = NbtUtils.readBlockPos(nbt.getCompound("pos"))
            val list = nbt.getList("orders", 10)
            val orders = list
                .map { ItemStack.CODEC.parse(NbtOps.INSTANCE, it) }
                .map { it.get().left() }
                .filter { it.isPresent }
                .map { it.get() }

            return Callback(pos, orders)
        }
    }

}