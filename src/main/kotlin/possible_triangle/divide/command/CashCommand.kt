package possible_triangle.divide.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.logic.CashLogic
import possible_triangle.divide.logic.TeamLogic

@Mod.EventBusSubscriber
object CashCommand {

    val NOT_ENOUGH = SimpleCommandExceptionType(TextComponent("Not enough cash"))

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("cash")
                .executes(::getCash)
                .then(literal("add").then(argument("amount", IntegerArgumentType.integer(1)).executes(::addCash)))
                .then(literal("remove").then(argument("amount", IntegerArgumentType.integer(1)).executes(::removeCash)))
                .then(literal("set").then(argument("amount", IntegerArgumentType.integer(0)).executes(::setCash)))
        )
    }

    private fun getCash(ctx: CommandContext<CommandSourceStack>): Int {
        val team = TeamLogic.teamOf(ctx)
        val amount = CashLogic.get(ctx.source.level, team)
        ctx.source.sendSuccess(TextComponent("Your team has $amount"), false)
        return amount
    }

    private fun addCash(ctx: CommandContext<CommandSourceStack>): Int {
        val team = TeamLogic.teamOf(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        if (CashLogic.modify(ctx.source.level, team, amount))
            ctx.source.sendSuccess(TextComponent("You added $amount to ${team.name}"), false)
        else
            throw NOT_ENOUGH.create()
        return amount
    }

    private fun removeCash(ctx: CommandContext<CommandSourceStack>): Int {
        val team = TeamLogic.teamOf(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        if (CashLogic.modify(ctx.source.level, team, -amount))
            ctx.source.sendSuccess(TextComponent("You removed $amount from ${team.name}"), false)
        else
            throw NOT_ENOUGH.create()
        return amount
    }

    private fun setCash(ctx: CommandContext<CommandSourceStack>): Int {
        val team = TeamLogic.teamOf(ctx)
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        CashLogic.set(ctx.source.level, team, amount)
        ctx.source.sendSuccess(TextComponent("You set ${team.name} to $amount"), false)
        return amount
    }

}