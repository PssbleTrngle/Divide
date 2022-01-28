package possible_triangle.divide.missions

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.events.Countdown
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams
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

    internal data class ActiveMission(val mission: Mission, val teams: MutableList<PlayerTeam>)

    private val COUNTDOWN = Countdown("mission", "Mission")

    private val LOGGER = EventLogger("mission", { Event.serializer() }) { always() }

    fun status(server: MinecraftServer, player: ServerPlayer? = null): MissionStatus? {
        return ACTIVE[server]?.let { active ->
            MissionStatus(
                mission = active.mission,
                secondsLeft = remaining(server),
                done = player != null && active.teams.none { it.name == player.team?.name }
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

    internal fun succeed(server: MinecraftServer, team: PlayerTeam, mission: Mission) {
        LOGGER.log(server, Event(mission, "fulfilled", team = EventTarget.of(team)))
        Teams.players(server, team).forEach {
            Chat.subtitle(it, Chat.apply("Mission fulfilled", ChatFormatting.GREEN))
        }
    }

    internal fun fail(server: MinecraftServer, team: PlayerTeam, mission: Mission) {
        LOGGER.log(server, Event(mission, "failed", team = EventTarget.of(team)))
        val subtract = min(mission.fine, Points.get(server, team))

        Points.modify(server, team, -subtract) { _ ->
            Teams.players(server, team).forEach {
                Chat.title(it, Chat.apply("Mission Failed", ChatFormatting.RED))
                Chat.subtitle(
                    it,
                    Chat.apply("-${subtract} points", ChatFormatting.RED),
                    setTitle = false
                )
            }
        }
    }

    fun fulfill(server: MinecraftServer, team: PlayerTeam, mission: Mission) {
        val active = ACTIVE[server]
        if (active == null || active.mission.id != mission.id) return
        val canFail = mission.type == Mission.Type.FAIL
        if (!active.teams.any { team.name == it.name }) return

        ACTIVE.modify(server) {
            this?.teams?.removeIf { it.name == team.name }
        }

        val bar = COUNTDOWN.bar(server)
        Teams.players(server, team).forEach {
            if (canFail) fail(server, team, mission)
            else succeed(server, team, mission)
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

            val teams = Teams.teams(server).toMutableList()
            val teamPlayers = teams.map { Teams.players(server, it) }.flatten()

            ACTIVE[server] = ActiveMission(mission, teams)
            OCCURRED_MISSIONS.modify(server) { add(mission.id) }
            LOGGER.log(server, Event(mission, "started"))

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

        return Config.CONFIG.missions.pause.value
    }

    private val ACTIVE = object : ModSavedData<ActiveMission?>("active_mission") {
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

    private val OCCURRED_MISSIONS = object : ModSavedData<MutableList<String>>("occurred_missions") {
        override fun save(nbt: CompoundTag, value: MutableList<String>) {
            nbt.put("values", value.mapTo(ListTag()) { StringTag.valueOf(it) })
        }

        override fun load(nbt: CompoundTag, server: MinecraftServer): MutableList<String> {
            return nbt.getList("values", 8).map { it.asString }.toMutableList()
        }

        override fun default(): MutableList<String> {
            return mutableListOf()
        }
    }
}