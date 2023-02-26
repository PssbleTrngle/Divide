package possible_triangle.divide.crates.callbacks

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.Config
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.data.EventPos
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.extensions.putBlockPos
import possible_triangle.divide.extensions.readBlockPos
import possible_triangle.divide.extensions.tileData
import possible_triangle.divide.logging.EventLogger
import java.util.*

class CleanCallback(val pos: BlockPos, val uuid: UUID) : TimerCallback<MinecraftServer> {

    companion object : CallbackHandler<CleanCallback>("crate_cleanup", CleanCallback::class.java) {
        @Serializable
        private data class Event(val pos: EventPos)

        private val LOGGER = EventLogger("loot_crate_cleaned", { Event.serializer() }) { isAdmin() }

        override fun serialize(nbt: CompoundTag, callback: CleanCallback) {
            nbt.putBlockPos("pos", callback.pos)
            nbt.putUUID("uuid", callback.uuid)
        }

        override fun deserialize(nbt: CompoundTag): CleanCallback {
            val pos = nbt.readBlockPos("pos")
            val uuid = nbt.getUUID("uuid")
            return CleanCallback(pos, uuid)
        }

        fun cleanMarker(server: MinecraftServer, pos: BlockPos) {
            CrateScheduler.markersAt(server, pos).forEach {
                it.remove(Entity.RemovalReason.DISCARDED)
            }
        }
    }

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val crate = CrateScheduler.crateAt(server, pos, uuid = uuid) ?: return

        LOGGER.log(server, Event(EventPos.of(pos)))

        cleanMarker(server, pos)

        crate.tileData().putBoolean(UNBREAKABLE_TAG, false)
        if (Config.CONFIG.crate.cleanNonEmpty || crate.isEmpty) {
            if (Config.CONFIG.crate.clearOnCleanup) crate.clearContent()
            crate.level?.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)
        }

    }

}