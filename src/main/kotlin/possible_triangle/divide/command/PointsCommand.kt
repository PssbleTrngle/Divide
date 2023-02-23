package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.scoreboard.Team
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import possible_triangle.divide.GameData
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.teamOrThrow

object PointsCommand {

    val NOT_ENOUGH = DynamicCommandExceptionType { Text.literal("You need at least $it points") }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("points")
                .executes(::getPoints)
                .then(literal("add").requires { it.isActiveParticipant() }
                    .then(argument("amount", IntegerArgumentType.integer(1))
                        .then(argument("team", StringArgumentType.string()).suggests(DivideTeamArgument.suggestions())
                            .executes(::addPoints)
                        ).executes(::addPoints)))
                .then(literal("remove").requires { it.isActiveParticipant() }
                    .then(argument("amount", IntegerArgumentType.integer(1))
                        .then(argument("team", StringArgumentType.string()).suggests(DivideTeamArgument.suggestions())
                            .executes(::removePoints)
                        ).executes(::removePoints)))
        )
    }

    private fun getTeam(ctx: CommandContext<ServerCommandSource>): Team {
        return try {
            DivideTeamArgument.getTeam(ctx, "team")
        } catch (e: IllegalArgumentException) {
            ctx.source.playerOrThrow.teamOrThrow()
        }
    }

    private fun getPoints(ctx: CommandContext<ServerCommandSource>): Int {
        val team = getTeam(ctx)
        val amount = Points.get(ctx.source.server, team)
        ctx.source.sendFeedback(Text.literal("Your team has $amount"), false)
        return amount
    }

    private fun addPoints(ctx: CommandContext<ServerCommandSource>): Int {
        val team = getTeam(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        Points.modify(ctx.source.server, team, amount)
        ctx.source.sendFeedback(Text.literal("You added $amount points to ${team.name}"), false)
        if (GameData.DATA[ctx.source.server].started) ctx.source.server.participants().forEach {
            Chat.message(it,
                Chat.apply("${ctx.source.name} added  $amount points to ${team.name}", Formatting.AQUA),
                log = true)
        }
        return amount
    }

    private fun removePoints(ctx: CommandContext<ServerCommandSource>): Int {
        val team = getTeam(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        if (Points.modify(ctx.source.server, team, -amount)) {
            ctx.source.sendFeedback(Text.literal("You removed $amount points from ${team.name}"), false)
            if (GameData.DATA[ctx.source.server].started) ctx.source.server.participants().forEach {
                Chat.message(it,
                    Chat.apply("${ctx.source.name} removed  $amount points from ${team.name}", Formatting.AQUA),
                    log = true)
            }
        } else
            throw NOT_ENOUGH.create(amount)
        return amount
    }

}