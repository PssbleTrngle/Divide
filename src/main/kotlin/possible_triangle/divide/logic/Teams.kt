package possible_triangle.divide.logic

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.extensions.getScore
import possible_triangle.divide.extensions.isTeammate
import possible_triangle.divide.extensions.players
import java.util.*
import kotlin.math.max

object Teams {

    private val NOT_PLAYING = SimpleCommandExceptionType(Component.literal("You are not playing"))
    const val TEAM_PREFIX = "${DivideMod.ID}_team_"
    private const val ADMIN_TAG = "${DivideMod.ID}_admin"

    fun score(server: MinecraftServer, team: PlayerTeam): Int {
        val total = Points.getTotal(server, team)
        val players = team.participants(server)
        val killObjective = server.scoreboard.getObjective("playerKills") ?: return 0
        val deathObjective = server.scoreboard.getObjective("deaths") ?: return 0
        val kills = players.map { it.getScore(killObjective) }.sumOf { it.score }
        val deaths = players.map { it.getScore(deathObjective) }.sumOf { it.score }
        return max(0, total - deaths * 10 + kills * 50)
    }

    fun isParticipantTeam(team: String): Boolean {
        return team.startsWith(TEAM_PREFIX)
    }

    fun PlayerTeam.isParticipantTeam(): Boolean {
        return color != ChatFormatting.RESET && isParticipantTeam(name)
    }

    fun Player.participantTeam(): PlayerTeam? {
        return team?.takeIf { it is PlayerTeam && it.isParticipantTeam() } as PlayerTeam?
    }

    fun Player.teamOrThrow(): PlayerTeam {
        return participantTeam() ?: throw NOT_PLAYING.create()
    }

    fun Player.teammates(includeSelf: Boolean = true): List<ServerPlayer> {
        if (!isParticipant()) return emptyList()
        return level.players()
            .filterIsInstance<ServerPlayer>()
            .filter { it.isTeammate(this) }
            .filter { includeSelf || it.uuid != uuid }
    }

    fun Player.isAdmin(): Boolean {
        return Config.CONFIG.admins.any {
            try {
                UUID.fromString(it) == uuid
            } catch (e: IllegalArgumentException) {
                false
            }
        } || Config.CONFIG.admins.contains(scoreboardName)
                || tags.contains(ADMIN_TAG)
                || hasPermissions(2)
    }

    fun Player.isGameSpectator(): Boolean {
        return tags.contains("spectator")
    }

    fun Player.isParticipant(): Boolean {
        return !isGameSpectator()
    }

    fun MinecraftServer.gameSpectators(): List<ServerPlayer> {
        return this.players().filter { it.isGameSpectator() }
    }

    fun MinecraftServer.participants(): List<ServerPlayer> {
        return this.players().filter { it.isParticipant() }
    }

    fun PlayerTeam.participants(server: MinecraftServer): List<ServerPlayer> {
        return server.participants().filter { it.team == this }
    }

    fun MinecraftServer.participingTeams(): List<PlayerTeam> {
        return scoreboard.playerTeams.filter { it.isParticipantTeam() }
    }

    fun ranked(server: MinecraftServer): List<PlayerTeam> {
        return server.participingTeams().sortedBy { -score(server, it) }
    }

    fun updateSpectators(server: MinecraftServer) {
        server.gameSpectators().forEach { player ->
            player.participantTeam().also {
                server.scoreboard.removePlayerFromTeam(player.scoreboardName, it)
            }
            player.setGameMode(GameType.SPECTATOR)
        }
    }

}