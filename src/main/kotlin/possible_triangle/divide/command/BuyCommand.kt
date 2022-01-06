package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.logic.TeamLogic
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

@Mod.EventBusSubscriber
object BuyCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Reward.values.entries.toList().fold(
                literal("buy").requires { TeamLogic.isPlayer(it.playerOrException) }
            ) { node, entry ->
                val base = literal(entry.key.lowercase())
                if (entry.value.requiresTarget) node.then(
                    base.then(
                        argument(
                            "target",
                            EntityArgument.player()
                        ).executes { buyReward(it, entry.value) })
                )
                else node.then(base.executes { buyReward(it, entry.value) })
            }
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>, reward: Reward): Int {

        val target = if (reward.requiresTarget) {
            EntityArgument.getPlayer(ctx, "target")
        } else ctx.source.playerOrException

        if (!reward.buy(
                RewardContext(
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