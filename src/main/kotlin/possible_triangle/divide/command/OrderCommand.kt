package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.crates.Order

object OrderCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            Order.keys.toList().fold(
                literal("order").requires { it.isActiveParticipant() }
            ) { node, key ->
                node.then(literal(key)
                    .executes { orderItem(it) { Order.getOrThrow(key) } }
                    .then(argument("amount", IntegerArgumentType.integer(1))
                        .executes { orderItem(it) { Order.getOrThrow(key) } }
                    )
                )
            }
        )
    }

    private fun orderItem(ctx: CommandContext<ServerCommandSource>, supplier: () -> Order): Int {
        val order = supplier()

        val amount = try {
            IntegerArgumentType.getInteger(ctx, "amount")
        } catch (e: IllegalArgumentException) {
            1
        }

        if (!order.order(ctx.source.playerOrThrow, amount)) {
            throw NOT_ENOUGH.create(amount * order.cost)
        }

        return amount
    }

}