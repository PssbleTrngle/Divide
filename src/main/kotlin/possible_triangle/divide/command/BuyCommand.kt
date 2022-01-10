package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

@Mod.EventBusSubscriber
object BuyCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Reward.keys.toList().fold(
                literal("buy").requires { Teams.isPlayer(it.playerOrException) }
            ) { node, key ->
                val base = literal(key)
                if (Reward.getOrThrow(key).requiresTarget) node.then(
                    base.then(
                        argument(
                            "target",
                            EntityArgument.player()
                        ).executes { buyReward(it) { Reward.getOrThrow(key) } })
                )
                else node.then(base.executes { buyReward(it){ Reward.getOrThrow(key) } })
            }
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>, supplier: () -> Reward): Int {
        val reward = supplier()

        val target = if (reward.requiresTarget) {
            EntityArgument.getPlayer(ctx, "target")
        } else ctx.source.playerOrException

        if (!reward.buy(
                RewardContext(
                    Teams.teamOf(ctx),
                    ctx.source.server,
                    ctx.source.playerOrException,
                    target,
                    reward
                )
            )
        ) throw CashCommand.NOT_ENOUGH.create(reward.price)

        return 1
    }

}