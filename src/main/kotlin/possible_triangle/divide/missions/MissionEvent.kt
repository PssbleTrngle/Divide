package possible_triangle.divide.missions

import kotlinx.serialization.Serializable
import net.minecraft.entity.boss.BossBar
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.GameData
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.events.Countdown
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.participingTeams
import kotlin.math.min

object MissionEvent : CycleEvent("missions") {

    override val enabled: Boolean
        get() = Config.CONFIG.missions.enabled

    override val startsAfter: Int
        get() = Config.CONFIG.missions.startAfter

    @Serializable
    private data class Event(val mission: Mission, val action: String, val team: EventTarget? = null)

    @Serializable
    data class MissionStatus(val mission: Mission, val secondsLeft: Int, val done: Boolean = false)

    internal data class ActiveMission(val mission: Mission, val teams: MutableList<Team>)

    private val COUNTDOWN = Countdown("mission", "Mission")

    private val LOGGER = EventLogger("mission", { Event.serializer() }) { always() }

    fun status(server: MinecraftServer, player: ServerPlayerEntity? = null): MissionStatus? {
        return ACTIVE[server]?.let { active ->
            MissionStatus(
                mission = active.mission,
                secondsLeft = COUNTDOWN.remaining(server),
                done = player != null && active.teams.none { it.name == player.participantTeam()?.name }
            )
        }
    }

    internal fun active(server: MinecraftServer): ActiveMission? {
        return ACTIVE[server]
    }

    fun clear(server: MinecraftServer) {
        active(server)?.apply {
            LOGGER.log(server, Event(mission, "ended"))
        }
        ACTIVE[server] = null
        COUNTDOWN.bar(server).isVisible = false
    }

    override fun onStop(server: MinecraftServer) {
        MissionCallback.cancel(server)
    }

    internal fun succeed(server: MinecraftServer, team: Team, mission: Mission) {
        LOGGER.log(server, Event(mission, "fulfilled", team = EventTarget.of(team)))
        team.participants(server).forEach {
            Chat.subtitle(it, Chat.apply("Mission fulfilled", Formatting.GREEN))
        }
    }

    internal fun fail(server: MinecraftServer, team: Team, mission: Mission) {
        LOGGER.log(server, Event(mission, "failed", team = EventTarget.of(team)))
        val subtract = min(mission.fine, Points.get(server, team))

        Points.modify(server, team, -subtract) { _ ->
            team.participants(server).forEach {
                Chat.title(it, Chat.apply("Mission Failed", Formatting.GRAY))
                Chat.subtitle(
                    it,
                    Chat.apply("-${subtract} points", Formatting.RED),
                    setTitle = false
                )
            }
        }
    }

    fun fulfill(server: MinecraftServer, team: Team, mission: Mission) {
        val active = ACTIVE[server]
        if (active == null || active.mission.id != mission.id) return
        val canFail = mission.type == Mission.Type.FAIL
        if (!active.teams.any { team.name == it.name }) return

        val ranFor = mission.time - COUNTDOWN.remaining(server)
        if (canFail && (ranFor <= Config.CONFIG.missions.safeTime || GameData.DATA[server].paused)) return

        ACTIVE.modify(server) {
            this?.teams?.removeIf { it.name == team.name }
        }

        val bar = COUNTDOWN.bar(server)
        if (canFail) fail(server, team, mission)
        else succeed(server, team, mission)
        team.participants(server).forEach {
            bar.removePlayer(it)
        }
    }

    override fun handle(server: MinecraftServer, index: Int): Int {

        val occurred = OCCURRED_MISSIONS[server]
        val availableMissions = Mission.values.filterNot { occurred.contains(it.id) }.let {
            it.ifEmpty {
                OCCURRED_MISSIONS[server] = mutableListOf()
                Mission.values
            }
        }

        val mission = availableMissions.randomOrNull()

        if (mission != null) {

            val teams = server.participingTeams().toMutableList()
            val teamPlayers = teams.map { it.participants(server) }.flatten()

            ACTIVE[server] = ActiveMission(mission, teams)
            OCCURRED_MISSIONS.modify(server) { add(mission.id) }
            LOGGER.log(server, Event(mission, "started"))

            teamPlayers.forEach {
                Chat.title(it, "New Mission")
                Chat.subtitle(it, "${mission.description}. You've got ${mission.time}s", setTitle = false)
            }

            COUNTDOWN.countdown(server, mission.time) {
                addPlayers(teamPlayers)
                isVisible = true
                name = Text.literal("Mission: ${mission.description}")
                color = BossBar.Color.YELLOW
            }

            MissionCallback.schedule(server, mission.time, MissionCallback())

        } else {
            DivideMod.LOGGER.warn("No missions found")
        }

        return Config.CONFIG.missions.pause.value
    }

    private val ACTIVE = object : ModSavedData<ActiveMission?>("active_mission") {
        override fun save(nbt: NbtCompound, value: ActiveMission?) {
            if (value == null) return
            nbt.putString("mission", value.mission.id)
            nbt.put("teams", value.teams.mapTo(NbtList()) { NbtString.of(it.name) })
        }

        override fun load(nbt: NbtCompound, server: MinecraftServer): ActiveMission? {
            val mission = Mission[nbt.getString("mission")] ?: return null
            val teams = nbt.getList("teams", 8)
                .mapNotNull { server.scoreboard.getPlayerTeam(it.asString()) }
                .toMutableList()
            return ActiveMission(mission, teams)
        }

        override fun default(): ActiveMission? {
            return null
        }
    }

    private val OCCURRED_MISSIONS = object : ModSavedData<MutableList<String>>("occurred_missions") {
        override fun save(nbt: NbtCompound, value: MutableList<String>) {
            nbt.put("values", value.mapTo(NbtList()) { NbtString.of(it) })
        }

        override fun load(nbt: NbtCompound, server: MinecraftServer): MutableList<String> {
            return nbt.getList("values", 8).map { it.asString() }.toMutableList()
        }

        override fun default(): MutableList<String> {
            return mutableListOf()
        }
    }
}