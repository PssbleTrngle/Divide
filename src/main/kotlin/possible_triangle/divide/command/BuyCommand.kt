package possible_triangle.divide.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.command.arguments.RewardArgument
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

@Mod.EventBusSubscriber
object BuyCommand {

    private val NO_TARGET = SimpleCommandExceptionType(TextComponent("Target not found"))

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            ActionTarget.values.fold(
                literal("buy").requires(Requirements::isPlayerInGame)
            ) { node, target ->

                val argument = target.argument()
                val base = argument("reward", StringArgumentType.string()).suggests(RewardArgument.suggestions(target))
                val command = { ctx: CommandContext<CommandSourceStack> ->
                    buyReward(ctx) { RewardArgument.getReward(ctx, "reward", target) }
                }

                node.then(if (argument == null) base.executes(command) else base.then(argument.executes(command)))

            }
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>, supplier: () -> Reward): Int {
        val reward = supplier()

        fun <T> parseFor(targetType: ActionTarget<T>): RewardContext<T> {
            val target = targetType.fromContext(ctx) ?: throw NO_TARGET.create()
            return RewardContext(
                Teams.requiredTeam(ctx.source.playerOrException),
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