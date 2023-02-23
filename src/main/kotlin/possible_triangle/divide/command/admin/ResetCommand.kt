package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.TeamArgumentType
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.command.SellCommand
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.participingTeams

object ResetCommand {

    fun register(base: LiteralArgumentBuilder<ServerCommandSource>): LiteralArgumentBuilder<ServerCommandSource> {
        return base.then(
            literal("reset").then(
                literal("player").then(
                    argument("players", EntityArgumentType.players())
                        .then(literal("inventory").executes(resetPlayers(::resetInventory)))
                        .then(literal("scores").executes(resetPlayers(::resetScores)))
                        .then(literal("advancements").executes(resetPlayers(::resetAdvancements)))
                        .then(literal("hearts").executes(resetPlayers(::resetHearts)))
                        .then(literal("*").executes(resetPlayers(::resetEverything)))
                )
            ).then(
                literal("team").then(
                    argument("team", TeamArgumentType.team())
                        .then(literal("inventory").executes(resetTeamPlayers(::resetInventory)))
                        .then(literal("scores").executes(resetTeamPlayers(::resetScores)))
                        .then(literal("advancements").executes(resetTeamPlayers(::resetAdvancements)))
                        .then(literal("hearts").executes(resetTeamPlayers(::resetHearts)))
                        .then(literal("bounties").executes(resetTeams(::resetBounties)))
                        .then(literal("points").executes(resetTeams(::resetPoints)))
                        .then(literal("base").executes(resetTeams(::resetBase)))
                        .then(literal("*").executes(resetTeams(::resetEverything)))
                )
            ).then(
                literal("all")
                    .then(literal("inventory").executes(resetPlayers(::resetInventory)))
                    .then(literal("scores").executes(resetPlayers(::resetScores)))
                    .then(literal("advancements").executes(resetPlayers(::resetAdvancements)))
                    .then(literal("hearts").executes(resetPlayers(::resetHearts)))
                    .then(literal("bounties").executes(resetTeams(::resetBounties)))
                    .then(literal("points").executes(resetTeams(::resetPoints)))
                    .then(literal("base").executes(resetTeams(::resetBase)))
                    .then(literal("*").executes(resetTeams(::resetEverything)))
            )
        )
    }

    private fun resetTeams(consumer: (Team, MinecraftServer) -> Unit): (ctx: CommandContext<ServerCommandSource>) -> Int {
        return { ctx ->
            val teams = try {
                listOf(TeamArgumentType.getTeam(ctx, "team"))
            } catch (e: IllegalArgumentException) {
                ctx.source.server.participingTeams()
            }
            teams.forEach { consumer(it, ctx.source.server) }
            if (teams.isNotEmpty()) ctx.source.sendFeedback(Text.literal("Reset ${teams.size} teams"), true)
            else ctx.source.sendError(Text.literal("No teams found"))
            1
        }
    }

    private fun resetTeamPlayers(consumer: (ServerPlayerEntity) -> Unit): (ctx: CommandContext<ServerCommandSource>) -> Int {
        return { ctx ->
            val team = TeamArgumentType.getTeam(ctx, "team")
            val players = team.participants(ctx.source.server)
            players.forEach(consumer)
            if (players.isNotEmpty()) ctx.source.sendFeedback(Text.literal("Reset ${players.size} players"), true)
            else ctx.source.sendError(Text.literal("No players found"))
            players.size
        }
    }

    private fun resetPlayers(consumer: (ServerPlayerEntity) -> Unit): (ctx: CommandContext<ServerCommandSource>) -> Int {
        return { ctx ->
            val players = try {
                EntityArgumentType.getPlayers(ctx, "players")
            } catch (e: java.lang.IllegalArgumentException) {
                ctx.source.server.participants()
            }
            players.forEach(consumer)
            if (players.isNotEmpty()) ctx.source.sendFeedback(Text.literal("Reset ${players.size} players"), true)
            else ctx.source.sendError(Text.literal("No players found"))
            players.size
        }
    }

    private fun resetEverything(team: Team, server: MinecraftServer) {
        val players = team.participants(server)
        players.forEach(::resetEverything)
        resetBounties(team, server)
        resetPoints(team, server)
        resetBase(team, server)
    }

    private fun resetEverything(player: ServerPlayerEntity) {
        resetInventory(player)
        resetScores(player)
    }

    private fun resetInventory(player: ServerPlayerEntity) {
        player.inventory.clear()
        player.currentScreenHandler.sendContentUpdates()
        player.playerScreenHandler.onContentChanged(player.inventory)
    }

    private fun resetAdvancements(player: ServerPlayerEntity) {
        player.server.advancementLoader.advancements.forEach { advancement ->
            val progress = player.advancementTracker.getProgress(advancement)
            if (progress.isAnyObtained) progress.obtainedCriteria.forEach {
                player.advancementTracker.revokeCriterion(advancement, it)
            }
        }
    }

    private fun resetHearts(player: ServerPlayerEntity) {
        SellCommand.resetHearts(player)
    }

    private fun resetScores(player: ServerPlayerEntity) {
        player.server.scoreboard.objectives.filter {
            player.server.scoreboard.playerHasObjective(player.entityName, it)
        }.forEach {
            player.server.scoreboard.resetPlayerScore(player.entityName, it)
        }
    }

    private fun resetPoints(team: Team, server: MinecraftServer) {
        Points.reset(server, team)
    }

    private fun resetBounties(team: Team, server: MinecraftServer) {
        Bounty.reset(server, team)
    }

    private fun resetBase(team: Team, server: MinecraftServer) {
        Bases.removeBase(team, server)
    }

}