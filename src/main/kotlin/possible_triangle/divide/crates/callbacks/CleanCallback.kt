package possible_triangle.divide.crates.callbacks

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG
import possible_triangle.divide.crates.CrateScheduler

class CleanCallback(val pos: BlockPos) : TimerCallback<MinecraftServer> {

    companion object {
        fun cleanMarker(server: MinecraftServer, pos: BlockPos) {
            CrateScheduler.markersAt(server, pos).forEach {
                it.remove(Entity.RemovalReason.DISCARDED)
            }
        }
    }

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val crate = CrateScheduler.crateAt(server, pos) ?: return

        cleanMarker(server, pos)

        crate.tileData.putBoolean(UNBREAKABLE_TAG, false)
        if (Config.CONFIG.crate.cleanNonEmpty || crate.isEmpty) {
            if (Config.CONFIG.crate.clearOnCleanup) crate.clearContent()
            crate.level?.setBlock(
                pos,
                Blocks.AIR.defaultBlockState(),
                2
            )
        }

    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, CleanCallback>(
            ResourceLocation(DivideMod.ID, "crate_cleanup"),
            CleanCallback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: CleanCallback) {
            nbt.put("pos", NbtUtils.writeBlockPos(callback.pos))
        }

        override fun deserialize(nbt: CompoundTag): CleanCallback {
            val pos = NbtUtils.readBlockPos(nbt.getCompound("pos"))
            return CleanCallback(pos)
        }
    }

}