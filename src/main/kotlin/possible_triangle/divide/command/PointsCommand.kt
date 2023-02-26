package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.GameData
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.teamOrThrow

object PointsCommand {

    val NOT_ENOUGH = DynamicCommandExceptionType { Component.literal("You need at least $it points") }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
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

    private fun getTeam(ctx: CommandContext<CommandSourceStack>): PlayerTeam {
        return try {
            DivideTeamArgument.getTeam(ctx, "team")
        } catch (e: IllegalArgumentException) {
            ctx.source.playerOrException.teamOrThrow()
        }
    }

    private fun getPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = getTeam(ctx)
        val amount = Points.get(ctx.source.server, team)
        ctx.source.sendSuccess(Component.literal("Your team has $amount"), false)
        return amount
    }

    private fun addPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = getTeam(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        Points.modify(ctx.source.server, team, amount)
        ctx.source.sendSuccess(Component.literal("You added $amount points to ${team.name}"), false)
        if (GameData.DATA[ctx.source.server].started) ctx.source.server.participants().forEach {
            Chat.message(it,
                Chat.apply("${ctx.source.textName} added  $amount points to ${team.name}", ChatFormatting.AQUA),
                log = true)
        }
        return amount
    }

    private fun removePoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = getTeam(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        if (Points.modify(ctx.source.server, team, -amount)) {
            ctx.source.sendSuccess(Component.literal("You removed $amount points from ${team.name}"), false)
            if (GameData.DATA[ctx.source.server].started) ctx.source.server.participants().forEach {
                Chat.message(it,
                    Chat.apply("${ctx.source.textName} removed  $amount points from ${team.name}", ChatFormatting.AQUA),
                    log = true)
            }
        } else
            throw NOT_ENOUGH.create(amount)
        return amount
    }

}