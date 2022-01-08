package possible_triangle.divide.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.crates.Order
import possible_triangle.divide.logic.TeamLogic

@Mod.EventBusSubscriber
object OrderCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Order.values.entries.toList().fold(
                literal("order").requires { TeamLogic.isPlayer(it.playerOrException) }
            ) { node, entry ->
                node.then(literal(entry.key)
                    .executes { order(it, entry.value) }
                    .then(argument("amount", IntegerArgumentType.integer(1))
                        .executes { order(it, entry.value) }
                    )
                )
            }
        )
    }

    private fun order(ctx: CommandContext<CommandSourceStack>, order: Order): Int {

        val team = TeamLogic.teamOf(ctx)

        val amount = try {
            IntegerArgumentType.getInteger(ctx, "amount")
        } catch (e: IllegalArgumentException) {
            1
        }

        if (!order.order(ctx.source.server, team, amount)) throw CashCommand.NOT_ENOUGH.create(amount * order.cost)

        return amount
    }

}