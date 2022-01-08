package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod

class MessageCallback(val teamName: String, val pos: BlockPos, val time: Long) : TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {

    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, MessageCallback>(
            ResourceLocation(DivideMod.ID, "crate_message"),
            MessageCallback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: MessageCallback) {
            nbt.put("pos", NbtUtils.writeBlockPos(callback.pos))
            nbt.putString("team", callback.teamName)
            nbt.putLong("time", callback.time)
        }

        override fun deserialize(nbt: CompoundTag): MessageCallback {
            val pos = NbtUtils.readBlockPos(nbt.getCompound("pos"))
            val teamName = nbt.getString("team")
            val time = nbt.getLong("time")
            return MessageCallback(teamName, pos, time)
        }
    }

}