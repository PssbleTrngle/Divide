package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.command.arguments.RewardArgument
import possible_triangle.divide.command.arguments.TargetArgument
import possible_triangle.divide.command.arguments.TargetTypeArgument
import possible_triangle.divide.logic.Teams.teamOrThrow
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object ActionCommand {

    fun register(base: LiteralArgumentBuilder<ServerCommandSource>): LiteralArgumentBuilder<ServerCommandSource>? {
        return base.then(
            literal("action").then(
                literal("run").then(argument("reward",
                    StringArgumentType.string()).suggests(RewardArgument.suggestions(ignoreVisibility = true))
                    .then(argument("targetType", StringArgumentType.string()).suggests(TargetTypeArgument.suggestions())
                        .then(argument("target", StringArgumentType.string()).suggests(TargetArgument.suggestions())
                            .executes(::runAction)
                        ).executes(::runAction)
                    )
                )
            )
        )
    }

    private fun runAction(ctx: CommandContext<ServerCommandSource>): Int {
        val reward = RewardArgument.getReward(ctx, "reward",  ignoreVisibility = true)
        val type = TargetTypeArgument.getTargetType(ctx, "targetType")

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

        Action.run(parseFor(type))
        ctx.source.sendFeedback(Text.literal("Successfully ran action ${reward.id}"), true)

        return 1
    }

}