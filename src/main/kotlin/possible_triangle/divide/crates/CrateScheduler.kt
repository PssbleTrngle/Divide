package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
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
import possible_triangle.divide.logic.Glowing
import possible_triangle.divide.logic.TeamLogic
import java.util.*
import java.util.stream.Collectors

object CrateScheduler {

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
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crate",
            time,
            FillLootCallback(pos, CrateLoot.random(), listOf())
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