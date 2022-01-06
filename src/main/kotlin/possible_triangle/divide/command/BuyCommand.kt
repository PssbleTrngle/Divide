package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.TeamLogic

@Mod.EventBusSubscriber
object BuyCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Reward.values().fold(
                literal("buy").requires { TeamLogic.isPlayer(it.playerOrException) }
            ) { node, reward ->
                val base = literal(reward.name.lowercase())
                if (reward.requiresTarget) node.then(
                    base.then(
                        argument(
                            "target",
                            EntityArgument.player()
                        ).executes { buyReward(it, reward) })
                )
                else node.then(base.executes { buyReward(it, reward) })
            }
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>, reward: Reward): Int {

        val target = if (reward.requiresTarget) {
            EntityArgument.getPlayer(ctx, "target")
        } else ctx.source.playerOrException

        if (!reward.buy(
                Reward.Context(
                    TeamLogic.teamOf(ctx),
                    ctx.source.level,
                    ctx.source.playerOrException,
                    target,
                    reward
                )
            )
        ) throw CashCommand.NOT_ENOUGH.create()

        return 1
    }

}