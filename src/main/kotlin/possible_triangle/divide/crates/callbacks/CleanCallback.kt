package possible_triangle.divide.crates.callbacks

import kotlinx.serialization.Serializable
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.timer.Timer
import net.minecraft.world.timer.TimerCallback
import possible_triangle.divide.Config
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.data.EventPos
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.extensions.tileData
import possible_triangle.divide.logging.EventLogger
import java.util.*

class CleanCallback(val pos: BlockPos, val uuid: UUID) : TimerCallback<MinecraftServer> {

    companion object : CallbackHandler<CleanCallback>("crate_cleanup", CleanCallback::class.java) {
        @Serializable
        private data class Event(val pos: EventPos)

        private val LOGGER = EventLogger("loot_crate_cleaned", { Event.serializer() }) { isAdmin() }

        override fun serialize(nbt: NbtCompound, callback: CleanCallback) {
            nbt.put("pos", NbtHelper.fromBlockPos(callback.pos))
            nbt.putUuid("uuid", callback.uuid)
        }

        override fun deserialize(nbt: NbtCompound): CleanCallback {
            val pos = NbtHelper.toBlockPos(nbt.getCompound("pos"))
            val uuid = nbt.getUuid("uuid")
            return CleanCallback(pos, uuid)
        }

        fun cleanMarker(server: MinecraftServer, pos: BlockPos) {
            CrateScheduler.markersAt(server, pos).forEach {
                it.remove(Entity.RemovalReason.DISCARDED)
            }
        }
    }

    override fun call(server: MinecraftServer, queue: Timer<MinecraftServer>, time: Long) {
        val crate = CrateScheduler.crateAt(server, pos, uuid = uuid) ?: return

        LOGGER.log(server, Event(EventPos.of(pos)))

        cleanMarker(server, pos)

        crate.tileData().putBoolean(UNBREAKABLE_TAG, false)
        if (Config.CONFIG.crate.cleanNonEmpty || crate.isEmpty) {
            if (Config.CONFIG.crate.clearOnCleanup) crate.clear()
            crate.world?.setBlockState(pos, Blocks.AIR.defaultState, 2)
        }

    }

}