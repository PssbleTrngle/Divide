package possible_triangle.divide.info

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ServerScoreboard.Method.CHANGE
import net.minecraft.server.ServerScoreboard.Method.REMOVE
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Score
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.actions.TeamBuff
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.Util
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Chat.apply
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.Reward
import java.util.*

@Mod.EventBusSubscriber
object Scores {

    @Serializable
    private data class Event(val player: EventTarget, val score: Int, val objective: String)

    private val LOGGER = EventLogger("score", { Event.serializer() }) { isPlayer { it.player } }

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
        if (objective.criteria.isReadOnly) return
        val name = objective.displayName.let {
            if (it is TextComponent) it.text else objective.name
        }
        LOGGER.log(server, Event(EventTarget.of(player), score.score, name))
    }

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        if (Util.shouldSkip(event, { it.world }, ticks = 5)) return

        val server = event.world.server ?: return

        Teams.ranked(server).forEachIndexed { index, team ->
            Teams.players(server, team).forEach {
                updateForPlayer(it, index + 1)
            }
        }
    }

    @SubscribeEvent
    fun logout(event: PlayerEvent.PlayerLoggedOutEvent) {
        LAST_LINES.remove(event.player.uuid)
    }

    private val LAST_LINES = hashMapOf<UUID, List<String>>()

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

        val team = Teams.teamOf(player)
        val extra = EXTRAS[player.uuid]

        if (team != null) {

            lines.add(mapOf(
                "Rank" to rankString(rank),
                "Points" to Points.get(player.server, team),
            ).map { "${it.key}: ${apply(it.value, GREEN)}" })

            val buffs = Action.running(player.server).map { it.ctx.reward }
                .filter { TeamBuff.isBuffed(player.server, team, it) }
                .map { it.display }

            if (buffs.isNotEmpty()) {
                lines.add(listOf("Team Buffs") + buffs)
            }

            lines.add(
                listOfNotNull(
                    Bases.isInBase(player, useTag = true).takeIf { it }?.let { apply("In Base", GREEN) },
                    Action.isRunning(player.server, Reward.TRACK_PLAYER) { it.target == player }.takeIf { it }
                        ?.let { "You are being tracked" },
                    MissionEvent.status(player.server, player)?.let {
                        apply(
                            "Mission: ${it.mission.description}",
                            if (it.done) GREEN else YELLOW
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