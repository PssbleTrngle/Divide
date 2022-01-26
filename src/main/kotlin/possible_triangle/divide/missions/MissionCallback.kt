package possible_triangle.divide.missions

import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams
import kotlin.math.min

class MissionCallback : TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val mission = MissionEvent.active(server) ?: return

        mission.teams.forEach { team ->
            Teams.players(server, team).forEach {
                Chat.title(it, Chat.apply("Mission Failed", ChatFormatting.RED))
                val subtract = min(mission.mission.fine, Points.get(server, team))
                Points.modify(server, team, -subtract) { _ ->
                    Chat.subtitle(
                        it,
                        Chat.apply("-${subtract} points", ChatFormatting.RED),
                        setTitle = false
                    )
                }
            }
        }

        MissionEvent.clear(server)
    }

    companion object : CallbackHandler<MissionCallback>("mission_check", MissionCallback::class.java) {
        override fun serialize(nbt: CompoundTag, callback: MissionCallback) {
            DivideMod.LOGGER.info("encoding $id")
        }

        override fun deserialize(nbt: CompoundTag): MissionCallback {
            DivideMod.LOGGER.info("decoding $id")
            return MissionCallback()
        }
    }

}