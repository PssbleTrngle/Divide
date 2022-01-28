package possible_triangle.divide.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.scores.PlayerTeam
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.GameData
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object PointsCommand {

    val NOT_ENOUGH = DynamicCommandExceptionType { TextComponent("You need at least $it points") }

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("points")
                .executes(::getPoints)
                .then(literal("add").requires(Requirements::isAdmin)
                    .then(argument("amount", IntegerArgumentType.integer(1))
                        .then(argument("team", StringArgumentType.string()).suggests(DivideTeamArgument.suggestions())
                            .executes(::addPoints)
                        ).executes(::addPoints)))
                .then(literal("remove").requires(Requirements::isAdmin)
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
            Teams.requiredTeam(ctx.source.playerOrException)
        }
    }

    private fun getPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = getTeam(ctx)
        val amount = Points.get(ctx.source.server, team)
        ctx.source.sendSuccess(TextComponent("Your team has $amount"), false)
        return amount
    }

    private fun addPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = getTeam(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        Points.modify(ctx.source.server, team, amount)
        ctx.source.sendSuccess(TextComponent("You added $amount points to ${team.name}"), false)
        if (GameData.DATA[ctx.source.server].started) Teams.players(ctx.source.server).forEach {
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
            ctx.source.sendSuccess(TextComponent("You removed $amount points from ${team.name}"), false)
            if (GameData.DATA[ctx.source.server].started) Teams.players(ctx.source.server).forEach {
                Chat.message(it,
                    Chat.apply("${ctx.source.textName} removed  $amount points from ${team.name}", ChatFormatting.AQUA),
                    log = true)
            }
        } else
            throw NOT_ENOUGH.create(amount)
        return amount
    }

}