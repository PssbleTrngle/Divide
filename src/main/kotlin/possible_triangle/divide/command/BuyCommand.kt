package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.command.arguments.RewardArgument
import possible_triangle.divide.command.arguments.TargetArgument
import possible_triangle.divide.gui.openRewardGui
import possible_triangle.divide.logic.Teams.teamOrThrow
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

object BuyCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("buy").requires { it.isActiveParticipant() }
                .then(
                    argument("reward", StringArgumentType.string()).suggests(RewardArgument.suggestions())
                        .then(
                            argument("target", StringArgumentType.string()).suggests(TargetArgument.suggestions())
                                .executes(::buyReward)
                        ).executes(::buyReward)
                ).executes(::openGui)
        )
    }

    private fun openGui(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.source.playerOrException.openRewardGui()
        return 1
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>): Int {
        val reward = RewardArgument.getReward(ctx, "reward")

        fun <T> parseFor(targetType: ActionTarget<T>): RewardContext<T> {
            val target = TargetArgument.getTarget(ctx, "target", targetType)
            return RewardContext(
                ctx.source.playerOrException.teamOrThrow(),
                ctx.source.server,
                ctx.source.playerOrException.uuid,
                target,
                reward,
                targetType,
            )
        }

        if (!Reward.buy(parseFor(reward.target))) throw NOT_ENOUGH.create(reward.price)

        return 1
    }

}