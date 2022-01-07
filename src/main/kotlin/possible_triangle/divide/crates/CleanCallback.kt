package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import net.minecraft.world.phys.AABB
import net.minecraftforge.common.Tags
import possible_triangle.divide.DivideMod

class CleanCallback(val pos: BlockPos) : TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val crate = CrateScheduler.crateAt(server, pos)

        if (crate != null) {
            crate.tileData.putBoolean("${DivideMod.ID}:unbreakable", false)
            if (crate.isEmpty) crate.level?.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)
        }

        server.overworld().getEntitiesOfClass(Entity::class.java, AABB(pos).inflate(2.0)) {
            it.tags.contains("${DivideMod.ID}:crate_marker")
        }.forEach {
            it.remove(Entity.RemovalReason.DISCARDED)
        }
    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, CleanCallback>(
            ResourceLocation(DivideMod.ID, "crates"),
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