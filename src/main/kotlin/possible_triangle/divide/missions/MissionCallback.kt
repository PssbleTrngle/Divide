package possible_triangle.divide.missions

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.timer.Timer
import net.minecraft.world.timer.TimerCallback
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.participingTeams

class MissionCallback : TimerCallback<MinecraftServer> {

    override fun call(server: MinecraftServer, queue: Timer<MinecraftServer>, time: Long) {
        val (mission, teams) = MissionEvent.active(server) ?: return

        val succeededTeams = server.participingTeams().filter {
            teams.contains(it) == (mission.type === Mission.Type.FAIL)
        }

        succeededTeams.firstOrNull()?.takeIf { succeededTeams.size == 1 && Config.CONFIG.missions.singleBonus }?.apply {
            val points = mission.fine / 2
            Points.modify(server, succeededTeams.first(), points) {
                this.participants(server).forEach {
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
        override fun serialize(nbt: NbtCompound, callback: MissionCallback) {
            DivideMod.LOGGER.info("encoding $id")
        }

        override fun deserialize(nbt: NbtCompound): MissionCallback {
            DivideMod.LOGGER.info("decoding $id")
            return MissionCallback()
        }
    }

}