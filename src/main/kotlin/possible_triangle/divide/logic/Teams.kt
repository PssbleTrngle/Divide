package possible_triangle.divide.logic

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import java.util.*
import kotlin.math.max

object Teams {

    private val NOT_PLAYING = SimpleCommandExceptionType(Text.literal("You are not playing"))
    const val TEAM_PREFIX = "${DivideMod.ID}_team_"
    private const val ADMIN_TAG = "${DivideMod.ID}_admin"

    fun score(server: MinecraftServer, team: Team): Int {
        val total = Points.getTotal(server, team)
        val players = team.participants(server)
        val killObjective = server.scoreboard.getObjective("playerKills") ?: return 0
        val deathObjective = server.scoreboard.getObjective("deaths") ?: return 0
        val kills = players.map { server.scoreboard.getPlayerScore(it.entityName, killObjective) }
            .sumOf { it.score }
        val deaths = players.map { server.scoreboard.getPlayerScore(it.entityName, deathObjective) }
            .sumOf { it.score }
        return max(0, total - deaths * 10 + kills * 50)
    }

    fun isParticipantTeam(team: String): Boolean {
        return team.startsWith(TEAM_PREFIX)
    }

    fun Team.isParticipantTeam(): Boolean {
        return color != Formatting.RESET && isParticipantTeam(name)
    }

    fun PlayerEntity.participantTeam(): Team? {
        if (!this.isParticipant()) return null
        val team = scoreboardTeam
        return if (team is Team) team
        else null
    }

    fun PlayerEntity.teamOrThrow(): Team {
        return participantTeam() ?: throw NOT_PLAYING.create()
    }

    fun ServerPlayerEntity.teammates(includeSelf: Boolean = true): List<ServerPlayerEntity> {
        if (!isParticipant()) return emptyList()
        return world.players
            .filterIsInstance<ServerPlayerEntity>()
            .filter { it.isTeammate(this) }
            .filter { includeSelf || it.uuid != uuid }
    }

    fun ServerPlayerEntity.isAdmin(): Boolean {
        return Config.CONFIG.admins.any {
            try {
                UUID.fromString(it) == uuid
            } catch (e: IllegalArgumentException) {
                false
            }
        } || Config.CONFIG.admins.contains(entityName)
                || scoreboardTags.contains(ADMIN_TAG)
                || hasPermissionLevel(2)
    }

    fun PlayerEntity.isGameSpectator(): Boolean {
        return scoreboardTags.contains("spectator")
    }

    fun PlayerEntity.isParticipant(): Boolean {
        return !isGameSpectator()
    }

    fun MinecraftServer.gameSpectators(): List<ServerPlayerEntity> {
        return this.playerManager.playerList.filter { it.isGameSpectator() }
    }

    fun MinecraftServer.participants(): List<ServerPlayerEntity> {
        return this.playerManager.playerList.filter { it.isParticipant() }
    }

    fun Team.participants(server: MinecraftServer): List<ServerPlayerEntity> {
        return server.participants().filter { it.scoreboardTeam == this }
    }

    fun MinecraftServer.participingTeams(): List<Team> {
        return scoreboard.teams.filter { it.isParticipantTeam() }
    }

    fun ranked(server: MinecraftServer): List<Team> {
        return server.participingTeams().sortedBy { -score(server, it) }
    }

    init {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (server.overworld.time % 20 != 0L) return@register

            server.gameSpectators().forEach { player ->
                player.participantTeam().also {
                    server.overworld.scoreboard.removePlayerFromTeam(player.entityName, it)
                }
                player.changeGameMode(GameMode.SPECTATOR)
            }
        }
    }

}