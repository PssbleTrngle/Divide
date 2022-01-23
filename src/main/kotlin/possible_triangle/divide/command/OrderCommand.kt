package possible_triangle.divide.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.crates.Order
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object OrderCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Order.keys.toList().fold(
                literal("order").requires(Requirements::isPlayerInGame)
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

    private fun orderItem(ctx: CommandContext<CommandSourceStack>, supplier: () -> Order): Int {
        val order = supplier()

        val team = Teams.requiredTeam(ctx.source.playerOrException)

        val amount = try {
            IntegerArgumentType.getInteger(ctx, "amount")
        } catch (e: IllegalArgumentException) {
            1
        }

        if (!order.order(ctx.source.playerOrException, team, amount)) {
            throw NOT_ENOUGH.create(amount * order.cost)
        }

        return amount
    }

}