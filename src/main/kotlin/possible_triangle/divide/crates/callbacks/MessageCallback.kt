package possible_triangle.divide.crates.callbacks

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Glowing
import possible_triangle.divide.logic.Teams

class MessageCallback(val teamName: String, val pos: BlockPos, val time: Long) : TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, now: Long) {
        val players = Teams.players(server).filter { it.team?.name == teamName }

        val marker = CrateScheduler.markersAt(server, pos).firstOrNull()
        if (marker != null) Glowing.addReason(marker, players, 1000000)

        val inSeconds = (time - now) / 20
        players.forEach {
            CrateScheduler.COUNTDOWN.bar(server).addPlayer(it)
            val posComponent = TextComponent("${pos.x}/${pos.y}/${pos.z}").withStyle(ChatFormatting.GOLD)
            Chat.message(
                it, TextComponent(
                    if (inSeconds <= 0)
                        "Loot dropped at "
                    else
                        "Loot will drop in $inSeconds seconds at "
                ).append(posComponent),
                log = true
            )

        }

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