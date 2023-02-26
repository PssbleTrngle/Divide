package possible_triangle.divide.info

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.contents.LiteralContents
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ServerScoreboard.Method.CHANGE
import net.minecraft.server.ServerScoreboard.Method.REMOVE
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Score
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.extensions.time
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
    private val IGNORED = setOf("custom:jump", "health")

    private const val SPOOFED = "${DivideMod.ID}_per_player_info"

    private val RANK_COLORS = listOf(GOLD, GRAY, DARK_RED)
    private val EXTRAS = hashMapOf<UUID, ExtraInfo>()

    fun show(player: ServerPlayer, extra: ExtraInfo) {
        EXTRAS[player.uuid] = extra
    }

    internal fun rankString(rank: Int): String {
        val color = RANK_COLORS.getOrElse(rank - 1) { WHITE }
        return apply("#${rank}", UNDERLINE, color)
    }

    fun getRanks(server: MinecraftServer): Map<PlayerTeam, Int> {
        return Teams.ranked(server).mapIndexed { index, team ->
            team to (index + 1)
        }.associate { it }
    }

    fun scoreUpdate(score: Score, server: MinecraftServer) {
        val player = server.playerList.getPlayerByName(score.owner) ?: return
        val objective = score.objective ?: return

        if (!objective.name.startsWith("${DivideMod.ID}_")) return
        if (IGNORED.contains(objective.criteria.name)) return

        val name = objective.displayName.contents.let {
            if (it is LiteralContents) it.text else objective.name
        }
        LOGGER.log(server, Event(EventTarget.of(player), score.score, name))
    }

    fun updateScores(server: MinecraftServer) {
        Teams.ranked(server).forEachIndexed { index, team ->
            team.participants(server).forEach {
                updateForPlayer(it, index + 1)
            }
        }
    }

    private val LAST_LINES = hashMapOf<UUID, List<String>>()

    fun resetSpoof(player: ServerPlayer) {
        LAST_LINES.remove(player.uuid)
    }

    private fun spoof(player: ServerPlayer, lines: List<String>) {
        val formatted = lines.mapIndexed { i, line ->
            line.ifBlank {
                val before = lines.filterIndexed { i2, it -> i2 < i && it.isBlank() }.size
                " ".repeat(before)
            }
        }.reversed()

        val removed = LAST_LINES[player.uuid]?.filterNot { formatted.contains(it) }

        formatted.forEachIndexed { index, it ->
            if (LAST_LINES[player.uuid]?.getOrNull(index) != it) {
                player.connection.send(ClientboundSetScorePacket(CHANGE, SPOOFED, it, index))
            }
        }

        removed?.forEach {
            player.connection.send(ClientboundSetScorePacket(REMOVE, SPOOFED, it, 0))
        }

        LAST_LINES[player.uuid] = formatted
    }

    private fun updateForPlayer(player: ServerPlayer, rank: Int) {

        val lines = mutableListOf<List<String>>()

        val team = player.participantTeam()
        val extra = EXTRAS[player.uuid]
        val now = player.level.time()

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