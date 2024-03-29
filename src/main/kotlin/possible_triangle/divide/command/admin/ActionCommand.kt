package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import possible_triangle.divide.command.arguments.RewardArgument
import possible_triangle.divide.command.arguments.TargetArgument
import possible_triangle.divide.command.arguments.TargetTypeArgument
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object ActionCommand {

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack>? {
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

    private fun runAction(ctx: CommandContext<CommandSourceStack>): Int {
        val reward = RewardArgument.getReward(ctx, "reward",  ignoreVisibility = true)
        val type = TargetTypeArgument.getTargetType(ctx, "targetType")

        fun <T> parseFor(targetType: ActionTarget<T>): RewardContext<T> {
            val target = TargetArgument.getTarget(ctx, "target", targetType)
            return RewardContext(
                Teams.requiredTeam(ctx.source.playerOrException),
                ctx.source.server,
                ctx.source.playerOrException.uuid,
                target,
                reward,
                targetType,
            )
        }

        Action.run(parseFor(type))
        ctx.source.sendSuccess(TextComponent("Successfully ran action ${reward.id}"), true)

        return 1
    }

}