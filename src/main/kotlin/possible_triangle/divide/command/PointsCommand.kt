package possible_triangle.divide.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
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
                .then(literal("add").requires { it.hasPermission(2) }
                    .then(argument("amount", IntegerArgumentType.integer(1)).executes(::addPoints)))
                .then(literal("remove").requires { it.hasPermission(2) }
                    .then(argument("amount", IntegerArgumentType.integer(1)).executes(::removePoints)))
                .then(literal("set").requires { it.hasPermission(2) }
                    .then(argument("amount", IntegerArgumentType.integer(0)).executes(::setPoints)))
        )
    }

    private fun getPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = Teams.requiredTeam(ctx.source.playerOrException)
        val amount = Points.get(ctx.source.server, team)
        ctx.source.sendSuccess(TextComponent("Your team has $amount"), false)
        return amount
    }

    private fun addPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = Teams.requiredTeam(ctx.source.playerOrException)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        Points.modify(ctx.source.server, team, amount)
        ctx.source.sendSuccess(TextComponent("You added $amount to ${team.name}"), false)
        return amount
    }

    private fun removePoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = Teams.requiredTeam(ctx.source.playerOrException)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        if (Points.modify(ctx.source.server, team, -amount))
            ctx.source.sendSuccess(TextComponent("You removed $amount from ${team.name}"), false)
        else
            throw NOT_ENOUGH.create(amount)
        return amount
    }

    private fun setPoints(ctx: CommandContext<CommandSourceStack>): Int {
        val team = Teams.requiredTeam(ctx.source.playerOrException)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        Points.set(ctx.source.server, team, amount)
        ctx.source.sendSuccess(TextComponent("You set ${team.name} to $amount"), false)
        return amount
    }

}