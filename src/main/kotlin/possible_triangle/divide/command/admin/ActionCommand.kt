package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.network.chat.Component
import possible_triangle.divide.command.arguments.RewardArgument
import possible_triangle.divide.command.arguments.TargetArgument
import possible_triangle.divide.command.arguments.TargetTypeArgument
import possible_triangle.divide.logic.Teams.teamOrThrow
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object ActionCommand {

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack> {
        val options = argument("duration", TimeArgument.time())
            .then(
                argument("charge", TimeArgument.time())
                    .executes(::runAction)
            )
            .executes(::runAction)

        return base.then(
            literal("action").then(
                literal("run").then(
                    argument(
                        "reward",
                        StringArgumentType.string()
                    ).suggests(RewardArgument.suggestions(ignoreVisibility = true))
                        .then(
                            argument(
                                "targetType",
                                StringArgumentType.string()
                            ).suggests(TargetTypeArgument.suggestions())
                                .then(
                                    argument(
                                        "target",
                                        StringArgumentType.string()
                                    ).suggests(TargetArgument.suggestions())
                                        .then(options)
                                        .executes(::runAction)
                                ).then(options).executes(::runAction)
                        )
                )
            )
        )
    }

    private fun runAction(ctx: CommandContext<CommandSourceStack>): Int {
        val reward = RewardArgument.getReward(ctx, "reward", ignoreVisibility = true)
        val type = TargetTypeArgument.getTargetType(ctx, "targetType")

        val duration = try {
            IntegerArgumentType.getInteger(ctx, "duration") / 20
        } catch (e: IllegalArgumentException) {
            reward.duration
        }

        val charge = try {
            IntegerArgumentType.getInteger(ctx, "charge") / 20
        } catch (e: IllegalArgumentException) {
            reward.charge
        }

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

        Action.run(parseFor(type), duration, charge)
        ctx.source.sendSuccess(Component.literal("Successfully ran action ${reward.id}"), true)

        return 1
    }

}