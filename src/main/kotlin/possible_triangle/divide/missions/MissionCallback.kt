package possible_triangle.divide.missions

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams

class MissionCallback : TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val (mission, teams) = MissionEvent.active(server) ?: return

        val succeededTeams = Teams.teams(server).filter {
            teams.contains(it) == (mission.type === Mission.Type.FAIL)
        }

        succeededTeams.firstOrNull()?.takeIf { succeededTeams.size == 1 && Config.CONFIG.missions.singleBonus }?.apply {
            val points = mission.fine / 2
            Points.modify(server, succeededTeams.first(), points) {
                Teams.players(server, this).forEach {
                    Chat.subtitle(it, Chat.apply("+${points} points"))
                }
            }
        }

        teams.forEach { team ->
            if (mission.type === Mission.Type.FAIL) MissionEvent.succeed(server, team, mission)
            else MissionEvent.fail(server, team, mission)
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