package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.argument.ColorArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.TeamArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.logic.Teams.participingTeams

object TeamCommand {

    private val TEAM_ALREADY_EXISTS =
        SimpleCommandExceptionType(Text.translatable("commands.team.add.duplicate"))

    fun register(node: LiteralArgumentBuilder<ServerCommandSource>): LiteralArgumentBuilder<ServerCommandSource> {
        return node.then(
            literal("team")
                .then(
                    literal("create").then(
                        argument("name", StringArgumentType.string()).then(
                            argument("color", ColorArgumentType.color()).executes(::createTeam)
                        )
                    )
                )
                .then(
                    literal("join").then(
                        argument("team", TeamArgumentType.team())
                            .suggests(DivideTeamArgument.suggestions())
                            .then(argument("players", EntityArgumentType.players()).executes(::addToTeam))
                    )
                )
                .then(
                    literal("remove").then(
                        argument("team", TeamArgumentType.team())
                            .suggests(DivideTeamArgument.suggestions())
                            .executes(::removeTeam)
                    )
                )
        )
    }

    private fun addToTeam(ctx: CommandContext<ServerCommandSource>): Int {
        val team = DivideTeamArgument.getTeam(ctx, "team")
        val players = EntityArgumentType.getPlayers(ctx, "players")
        players.forEach {
            it.scoreboardTags.remove("spectator")
            ctx.source.server.scoreboard.addPlayerToTeam(it.entityName, team)
        }

        ctx.source.sendFeedback(Text.literal("added ${players.size} players to team"), true)

        return players.size
    }

    private fun removeTeam(ctx: CommandContext<ServerCommandSource>): Int {
        val team = DivideTeamArgument.getTeam(ctx, "team")

        ctx.source.server.scoreboard.removeTeam(team)
        ctx.source.sendFeedback(Text.literal("removed team"), true)

        return 1
    }

    private fun createTeam(ctx: CommandContext<ServerCommandSource>): Int {
        val scoreboard = ctx.source.server.scoreboard

        val name = StringArgumentType.getString(ctx, "name")
        val color = ColorArgumentType.getColor(ctx, "color")
        val id = Teams.TEAM_PREFIX + color.name.lowercase()

        if (scoreboard.getPlayerTeam(id) != null) throw TEAM_ALREADY_EXISTS.create()
        if (ctx.source.server.participingTeams().any { it.color == color }) throw TEAM_ALREADY_EXISTS.create()

        val team = scoreboard.addTeam(id)
        team.displayName = Text.literal(name)
        team.isFriendlyFireAllowed = false
        team.setShowFriendlyInvisibles(true)
        team.color = color

        ctx.source.sendFeedback(Text.translatable("commands.team.add.success", team.formattedName), true)
        return scoreboard.teams.size
    }

}