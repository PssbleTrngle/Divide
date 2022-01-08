package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import net.minecraft.world.phys.AABB
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateEvents.CRATE_TAG
import possible_triangle.divide.crates.CrateEvents.UNBREAKABLE_TAG

class CleanCallback(val pos: BlockPos) : TimerCallback<MinecraftServer> {

    companion object {
        fun cleanMarker(server: MinecraftServer, pos: BlockPos) {
            server.overworld().getEntitiesOfClass(Entity::class.java, AABB(pos).inflate(0.5)) {
                it.tags.contains(CRATE_TAG)
            }.forEach {
                it.remove(Entity.RemovalReason.DISCARDED)
            }
        }
    }

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val crate = CrateScheduler.crateAt(server, pos)

        cleanMarker(server, pos)

        if (crate != null) {
            crate.tileData.putBoolean(UNBREAKABLE_TAG, false)
            if (Config.CONFIG.crate.cleanNonEmpty || crate.isEmpty) crate.level?.setBlock(
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