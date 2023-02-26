package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.command.SellCommand
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.participingTeams

object ResetCommand {

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack> {
        return base.then(
            literal("reset").then(
                literal("player").then(
                    argument("players", EntityArgument.players())
                        .then(literal("inventory").executes(resetPlayers(::resetInventory)))
                        .then(literal("scores").executes(resetPlayers(::resetScores)))
                        .then(literal("advancements").executes(resetPlayers(::resetAdvancements)))
                        .then(literal("hearts").executes(resetPlayers(::resetHearts)))
                        .then(literal("*").executes(resetPlayers(::resetEverything)))
                )
            ).then(
                literal("team").then(
                    argument("team", TeamArgument.team())
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

    private fun resetTeams(consumer: (PlayerTeam, MinecraftServer) -> Unit): (ctx: CommandContext<CommandSourceStack>) -> Int {
        return { ctx ->
            val teams = try {
                listOf(TeamArgument.getTeam(ctx, "team"))
            } catch (e: IllegalArgumentException) {
                ctx.source.server.participingTeams()
            }
            teams.forEach { consumer(it, ctx.source.server) }
            if (teams.isNotEmpty()) ctx.source.sendSuccess(Component.literal("Reset ${teams.size} teams"), true)
            else ctx.source.sendFailure(Component.literal("No teams found"))
            1
        }
    }

    private fun resetTeamPlayers(consumer: (ServerPlayer) -> Unit): (ctx: CommandContext<CommandSourceStack>) -> Int {
        return { ctx ->
            val team = TeamArgument.getTeam(ctx, "team")
            val players = team.participants(ctx.source.server)
            players.forEach(consumer)
            if (players.isNotEmpty()) ctx.source.sendSuccess(Component.literal("Reset ${players.size} players"), true)
            else ctx.source.sendFailure(Component.literal("No players found"))
            players.size
        }
    }

    private fun resetPlayers(consumer: (ServerPlayer) -> Unit): (ctx: CommandContext<CommandSourceStack>) -> Int {
        return { ctx ->
            val players = try {
                EntityArgument.getPlayers(ctx, "players")
            } catch (e: java.lang.IllegalArgumentException) {
                ctx.source.server.participants()
            }
            players.forEach(consumer)
            if (players.isNotEmpty()) ctx.source.sendSuccess(Component.literal("Reset ${players.size} players"), true)
            else ctx.source.sendFailure(Component.literal("No players found"))
            players.size
        }
    }

    private fun resetEverything(team: PlayerTeam, server: MinecraftServer) {
        val players = team.participants(server)
        players.forEach(::resetEverything)
        resetBounties(team, server)
        resetPoints(team, server)
        resetBase(team, server)
    }

    private fun resetEverything(player: ServerPlayer) {
        resetInventory(player)
        resetScores(player)
    }

    private fun resetInventory(player: ServerPlayer) {
        player.inventory.clearContent()
        player.containerMenu.broadcastChanges()
        player.inventoryMenu.slotsChanged(player.inventory)
    }

    private fun resetAdvancements(player: ServerPlayer) {
        player.server.advancements.allAdvancements.forEach { advancement ->
            val progress = player.advancements.getOrStartProgress(advancement)
            if (progress.hasProgress()) progress.completedCriteria.forEach {
                player.advancements.revoke(advancement, it)
            }
        }
    }

    private fun resetHearts(player: ServerPlayer) {
        SellCommand.resetHearts(player)
    }

    private fun resetScores(player: ServerPlayer) {
        player.server.scoreboard.objectives.filter {
            player.server.scoreboard.hasPlayerScore(player.scoreboardName, it)
        }.forEach {
            player.server.scoreboard.resetPlayerScore(player.scoreboardName, it)
        }
    }

    private fun resetPoints(team: PlayerTeam, server: MinecraftServer) {
        Points.reset(server, team)
    }

    private fun resetBounties(team: PlayerTeam, server: MinecraftServer) {
        Bounty.reset(server, team)
    }

    private fun resetBase(team: PlayerTeam, server: MinecraftServer) {
        Bases.removeBase(team, server)
    }

}