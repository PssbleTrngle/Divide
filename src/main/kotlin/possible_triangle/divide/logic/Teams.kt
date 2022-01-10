package possible_triangle.divide.logic

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
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
import possible_triangle.divide.DivideMod
import kotlin.math.max

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object Teams {

    private val NOT_PLAYING = SimpleCommandExceptionType(TextComponent("You are not playing"))

    val ADMIN_TAG = "${DivideMod.ID}_admin"

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

    fun teamOf(player: Player): PlayerTeam? {
        if (!isPlayer(player)) return null
        val team = player.team
        return if (team is PlayerTeam) team
        else null
    }

    fun teamOf(ctx: CommandContext<CommandSourceStack>): PlayerTeam {
        return teamOf(ctx.source.playerOrException) ?: throw NOT_PLAYING.create()
    }

    fun teammates(player: ServerPlayer, includeSelf: Boolean = true): List<ServerPlayer> {
        val team = teamOf(player) ?: return listOf()
        return player.getLevel().players()
            .filter { it.team?.name == team.name }
            .filter { includeSelf || it.uuid != player.uuid }
    }

    fun isAdmin(source: CommandSourceStack): Boolean {
        val entity = source.entity
        return source.hasPermission(2) || entity is ServerPlayer && entity.tags.contains(ADMIN_TAG)
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
        //val players = players(server)
        //return players.mapNotNull { it.team }.distinctBy { it.name }.filterIsInstance(PlayerTeam::class.java)
    }

    fun ranked(server: MinecraftServer): List<PlayerTeam> {
        return teams(server).sortedBy { -score(server, it) }
    }

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        val server = event.world.server ?: return
        spectators(server).forEach {
            if (it.team != null) server.overworld().scoreboard.removePlayerFromTeam(it.scoreboardName)
            it.setGameMode(GameType.SPECTATOR)
        }
    }

}