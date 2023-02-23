package possible_triangle.divide.info

import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket
import net.minecraft.scoreboard.ScoreboardPlayerScore
import net.minecraft.scoreboard.ServerScoreboard.UpdateMode.CHANGE
import net.minecraft.scoreboard.ServerScoreboard.UpdateMode.REMOVE
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralTextContent
import net.minecraft.util.Formatting.*
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Bases.isInBase
import possible_triangle.divide.logic.Chat.apply
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.missions.Mission
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.actions.TeamBuff
import java.util.*

object Scores {

    @Serializable
    private data class Event(val player: EventTarget, val score: Int, val objective: String)

    private val LOGGER = EventLogger("score", { Event.serializer() }) { isPlayer { it.player } }

    private const val SPOOFED = "${DivideMod.ID}_per_player_info"

    private val RANK_COLORS = listOf(GOLD, GRAY, DARK_RED)
    private val EXTRAS = hashMapOf<UUID, ExtraInfo>()

    fun show(player: ServerPlayerEntity, extra: ExtraInfo) {
        EXTRAS[player.uuid] = extra
    }

    internal fun rankString(rank: Int): String {
        val color = RANK_COLORS.getOrElse(rank - 1) { WHITE }
        return apply("#${rank}", UNDERLINE, color)
    }

    fun getRanks(server: MinecraftServer): Map<Team, Int> {
        return Teams.ranked(server).mapIndexed { index, team ->
            team to (index + 1)
        }.associate { it }
    }

    fun scoreUpdate(score: ScoreboardPlayerScore, server: MinecraftServer) {
        val player = server.playerManager.getPlayer(score.playerName) ?: return
        val objective = score.objective ?: return
        if (objective.criterion.isReadOnly) return
        val name = objective.displayName.content.let {
            if (it is LiteralTextContent) it.string else objective.name
        }
        LOGGER.log(server, Event(EventTarget.of(player), score.score, name))
    }

    init {
        ServerTickEvents.END_WORLD_TICK.register { world ->
            if (world.time % 5 == 0L) return@register

            Teams.ranked(world.server).forEachIndexed { index, team ->
                team.participants(world.server).forEach {
                    updateForPlayer(it, index + 1)
                }
            }
        }
    }

    init {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            LAST_LINES.remove(handler.player.uuid)
        }
    }

    private val LAST_LINES = hashMapOf<UUID, List<String>>()

    private fun spoof(player: ServerPlayerEntity, lines: List<String>) {
        val formatted = lines.mapIndexed { i, line ->
            line.ifBlank {
                val before = lines.filterIndexed { i2, it -> i2 < i && it.isBlank() }.size
                " ".repeat(before)
            }
        }.reversed()

        val removed = LAST_LINES[player.uuid]?.filterNot { formatted.contains(it) }

        formatted.forEachIndexed { index, it ->
            if (LAST_LINES[player.uuid]?.getOrNull(index) != it) {
                player.networkHandler.sendPacket(ScoreboardPlayerUpdateS2CPacket(CHANGE, SPOOFED, it, index))
            }
        }

        removed?.forEach {
            player.networkHandler.sendPacket(ScoreboardPlayerUpdateS2CPacket(REMOVE, SPOOFED, it, 0))
        }

        LAST_LINES[player.uuid] = formatted
    }

    private fun updateForPlayer(player: ServerPlayerEntity, rank: Int) {

        val lines = mutableListOf<List<String>>()

        val team = player.participantTeam()
        val extra = EXTRAS[player.uuid]
        val now = player.world.time

        if (team != null) {

            lines.add(mapOf(
                "Rank" to rankString(rank),
                "Points" to Points.get(player.server, team),
            ).map { "${it.key}: ${apply(it.value, GREEN)}" })

            val buffs = Action.running(player.server).map { it.ctx.reward }
                .filter { TeamBuff.isBuffed(player.server, team, it) }
                .map { it.display }

            if (buffs.isNotEmpty()) {
                lines.add(listOf(apply("Team Buffs", LIGHT_PURPLE)) + buffs)
            }

            lines.add(
                listOfNotNull(
                    player.isInBase(useTag = true).takeIf { it }?.let { apply("In Base", GREEN) },
                    PlayerBountyEvent.getBounty(player)?.takeIf { it.until >= now }
                        ?.let { apply("Bounty on your Head until", RED) },
                    Action.isRunning(player.server, Reward.TRACK_PLAYER, ifCharged = true) {
                        it.targetPlayers().contains(player)
                    }
                        .takeIf { it }
                        ?.let { apply("You are being tracked", YELLOW) },
                    MissionEvent.status(player.server, player)?.let {
                        apply(
                            "Mission: ${it.mission.description}",
                            if (it.done) when (it.mission.type) {
                                Mission.Type.SUCCEED -> GREEN
                                Mission.Type.FAIL -> RED
                            } else YELLOW
                        )
                    },
                )
            )

        } else if (extra == null) {
            lines.add(ExtraInfo.RANKS.lines(player.server, null))
        }

        if (extra != null) lines.add(extra.lines(player.server, team))

        lines.add(listOf(apply("use /show ...", GRAY)))

        spoof(player, lines.filterNot { it.isEmpty() }.map { listOf("") + it }.flatten())

    }

}