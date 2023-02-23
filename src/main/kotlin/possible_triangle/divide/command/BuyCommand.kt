package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.command.arguments.RewardArgument
import possible_triangle.divide.command.arguments.TargetArgument
import possible_triangle.divide.logic.Teams.teamOrThrow
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

object BuyCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("buy").requires { it.isActiveParticipant() }
                .then(
                    argument("reward", StringArgumentType.string()).suggests(RewardArgument.suggestions())
                        .then(
                            argument("target", StringArgumentType.string()).suggests(TargetArgument.suggestions())
                                .executes(::buyReward)
                        ).executes(::buyReward)
                )
        )
    }

    private fun buyReward(ctx: CommandContext<ServerCommandSource>): Int {
        val reward = RewardArgument.getReward(ctx, "reward")

        fun <T> parseFor(targetType: ActionTarget<T>): RewardContext<T> {
            val target = TargetArgument.getTarget(ctx, "target", targetType)
            return RewardContext(
                ctx.source.playerOrThrow.teamOrThrow(),
                ctx.source.server,
                ctx.source.playerOrThrow.uuid,
                target,
                reward,
                targetType,
            )
        }

        if (!Reward.buy(parseFor(reward.target))) throw NOT_ENOUGH.create(reward.price)

        return 1
    }

}