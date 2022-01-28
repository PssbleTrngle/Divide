package possible_triangle.divide.crates.callbacks

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.Config
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.data.EventPos
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.Util
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams

class MessageCallback(val teamName: String, val pos: BlockPos, val time: Long) : TimerCallback<MinecraftServer> {

    companion object : CallbackHandler<MessageCallback>("crate_message", MessageCallback::class.java) {
        @Serializable
        private data class Event(val pos: EventPos, val team: EventTarget)

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

        private val LOGGER = EventLogger("loot_crate_notify", { Event.serializer() }) { inTeam { it.team } }
    }

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, now: Long) {
        val team = server.scoreboard.getPlayerTeam(teamName) ?: return
        val players = Teams.players(server, team)

        LOGGER.log(server, Event(EventPos.of(pos), EventTarget.of(team)))

        val marker = CrateScheduler.markersAt(server, pos).firstOrNull()
        val inSeconds = (time - now) / 20
        if (marker != null) DataHacker.addReason(
            GLOWING,
            marker,
            players,
            inSeconds.toInt() + Config.CONFIG.crate.cleanUpTime
        )

        players.forEach {
            CrateScheduler.COUNTDOWN.bar(server).addPlayer(it)
            Chat.message(
                it, TextComponent(
                    if (inSeconds <= 0)
                        "Loot dropped at "
                    else
                        "Loot will drop in $inSeconds seconds at "
                ).append(Util.encodePos(pos, it)),
                log = true
            )

        }

    }

}