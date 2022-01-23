package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

@Mod.EventBusSubscriber
object BuyCommand {

    private val NO_TARGET = SimpleCommandExceptionType(TextComponent("Target not found"))

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Reward.keys.toList().fold(
                literal("buy").requires(Requirements::isPlayerInGame)
            ) { node, key ->
                val base = literal(key)
                val reward = Reward.getOrThrow(key)
                val argument = reward.action.target.argument()
                if (argument != null) {
                    node.then(base.then(argument.executes { buyReward(it) { Reward.getOrThrow(key) } }))
                } else {
                    node.then(base.executes { buyReward(it) { Reward.getOrThrow(key) } })
                }
            }
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>, supplier: () -> Reward): Int {
        val reward = supplier()

        val target = reward.action.target.fromContext(ctx) ?: throw NO_TARGET.create()

        if (!Reward.buy(
                RewardContext<Any, Any>(
                    Teams.requiredTeam(ctx.source.playerOrException),
                    ctx.source.server,
                    ctx.source.playerOrException.uuid,
                    target,
                    reward
                )
            )
        ) throw NOT_ENOUGH.create(reward.price)

        return 1
    }

}