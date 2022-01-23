package possible_triangle.divide.logic

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Util
import java.util.*
import kotlin.math.max

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object Teams {

    private val NOT_PLAYING = SimpleCommandExceptionType(TextComponent("You are not playing"))
    const val TEAM_PREFIX = "${DivideMod.ID}_team_"
    private const val ADMIN_TAG = "${DivideMod.ID}_admin"

    fun score(server: MinecraftServer, team: Team): Int {
        val total = Points.getTotal(server, team)
        val players = players(server, team)
        val killObjective = server.scoreboard.getObjective("playerKills") ?: return 0
        val deathObjective = server.scoreboard.getObjective("deaths") ?: return 0
        val kills = players.map { server.scoreboard.getOrCreatePlayerScore(it.scoreboardName, killObjective) }
            .sumOf { it.score }
        val deaths = players.map { server.scoreboard.getOrCreatePlayerScore(it.scoreboardName, deathObjective) }
            .sumOf { it.score }
        return max(0, total - deaths * 10 + kills * 50)
    }

    fun isPlayingTeam(team: String): Boolean {
        return team.startsWith(TEAM_PREFIX)
    }

    fun isPlayingTeam(team: Team): Boolean {
        return team.color != ChatFormatting.RESET && isPlayingTeam(team.name)
    }

    fun teamOf(player: Player): PlayerTeam? {
        if (!isPlayer(player)) return null
        val team = player.team
        return if (team is PlayerTeam) team
        else null
    }

    fun requiredTeam(player: Player): PlayerTeam {
        return teamOf(player) ?: throw NOT_PLAYING.create()
    }

    fun teammates(player: ServerPlayer, includeSelf: Boolean = true): List<ServerPlayer> {
        val team = teamOf(player) ?: return listOf()
        return player.getLevel().players()
            .filter { it.team?.name == team.name }
            .filter { includeSelf || it.uuid != player.uuid }
    }

    fun isAdmin(player: ServerPlayer): Boolean {
        return Config.CONFIG.admins.any {
            try {
                UUID.fromString(it) == player.uuid
            } catch (e: IllegalArgumentException) {
                false
            }
        } || Config.CONFIG.admins.contains(player.scoreboardName)
                || player.tags.contains(ADMIN_TAG)
                || player.hasPermissions(2)
    }

    fun isSpectator(player: Player): Boolean {
        return player.tags.contains("spectator")
    }

    fun isPlayer(player: Player): Boolean {
        return !isSpectator(player)
    }

    fun spectators(server: MinecraftServer): List<ServerPlayer> {
        return server.playerList.players.filter { isSpectator(it) }
    }

    fun players(server: MinecraftServer): List<ServerPlayer> {
        return server.playerList.players.filter { isPlayer(it) }
    }

    fun players(server: MinecraftServer, team: Team): List<ServerPlayer> {
        return players(server).filter { it.team != null && it.team?.name == team.name }
    }

    fun teams(server: MinecraftServer): List<PlayerTeam> {
        return server.scoreboard.playerTeams.toList()
            .filter { isPlayingTeam(it) }
    }

    fun ranked(server: MinecraftServer): List<PlayerTeam> {
        return teams(server).sortedBy { -score(server, it) }
    }

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        if (Util.shouldSkip(event, { it.world })) return

        val server = event.world.server ?: return
        spectators(server).forEach {
            if (it.team != null) server.overworld().scoreboard.removePlayerFromTeam(it.scoreboardName)
            it.setGameMode(GameType.SPECTATOR)
        }
    }

}