package possible_triangle.divide.missions

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.events.Countdown
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams

object MissionEvent : CycleEvent("missions") {

    internal data class ActiveMission(val mission: Mission, val teams: MutableList<PlayerTeam>)

    internal val COUNTDOWN = Countdown("mission", "Mission")

    internal val ACTIVE = object : ModSavedData<ActiveMission?>("active_mission") {
        override fun save(nbt: CompoundTag, value: ActiveMission?) {
            if (value == null) return
            nbt.putString("mission", value.mission.id)
            nbt.put("teams", value.teams.mapTo(ListTag()) { StringTag.valueOf(it.name) })
        }

        override fun load(nbt: CompoundTag, server: MinecraftServer): ActiveMission? {
            val mission = Mission[nbt.getString("mission")] ?: return null
            val teams = nbt.getList("teams", 8)
                .mapNotNull { server.scoreboard.getPlayerTeam(it.asString) }
                .toMutableList()
            return ActiveMission(mission, teams)
        }

        override fun default(): ActiveMission? {
            return null
        }
    }

    override fun onStop(server: MinecraftServer) {
        MissionCallback.cancel(server)
    }

    fun fulfill(server: MinecraftServer, team: Team, mission: Mission) {
        val active = ACTIVE[server]
        if (active == null || active.mission.id != mission.id) return
        if (!active.teams.any { team.name == it.name }) return

        ACTIVE.modify(server) {
            this?.teams?.removeIf { it.name == team.name }
        }

        val bar = COUNTDOWN.bar(server)
        Teams.players(server, team).forEach {
            Chat.subtitle(it, "Mission fulfilled")
            bar.removePlayer(it)
        }
    }

    override fun handle(server: MinecraftServer, index: Int): Int {

        if (index >= Config.CONFIG.missions.startAt) {
            val mission = Mission.values.randomOrNull()
            if (mission != null) {

                val teams = Teams.teams(server).toMutableList()
                val teamPlayers = teams.map { Teams.players(server, it) }.flatten()

                ACTIVE[server] = ActiveMission(mission, teams)

                teamPlayers.forEach {
                    Chat.title(it, "New Mission")
                    Chat.subtitle(it, "${mission.description}. You've got ${mission.time}s", setTitle = false)
                }

                COUNTDOWN.countdown(server, mission.time) {
                    players = teamPlayers
                    isVisible = true
                    name = TextComponent("Mission: ${mission.description}")
                    color = BossEvent.BossBarColor.YELLOW
                }

                MissionCallback.schedule(server, mission.time, MissionCallback())

            } else {
                DivideMod.LOGGER.warn("No missions found")
            }
        }

        return Config.CONFIG.missions.pause.value
    }

    override fun isEnabled(server: MinecraftServer): Boolean {
        return Config.CONFIG.missions.enabled
    }
}