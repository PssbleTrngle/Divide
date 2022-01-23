package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.command.PointsCommand.NOT_ENOUGH
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Action
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
                when (Reward.getOrThrow(key).action.targets) {
                    Action.Target.PLAYER -> node.then(
                        base.then(
                            argument(
                                "target",
                                EntityArgument.player()
                            ).executes { buyReward(it) { Reward.getOrThrow(key) } })
                    )
                    Action.Target.TEAM -> node.then(
                        base.then(
                            argument("team", TeamArgument.team())
                                .suggests(DivideTeamArgument.suggestions(ignoreOwn = true))
                                .executes { buyReward(it) { Reward.getOrThrow(key) } })
                    )
                    else -> node.then(base.executes { buyReward(it) { Reward.getOrThrow(key) } })
                }
            }
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>, supplier: () -> Reward): Int {
        val reward = supplier()

        val target = when (reward.action.targets) {
            Action.Target.PLAYER -> EntityArgument.getPlayer(ctx, "target")
            Action.Target.TEAM -> {
                val team = DivideTeamArgument.getTeam(ctx, "team", ignoreOwn = true)
                ctx.source.server.playerList.players.find { it.team?.name == team.name }
            }
            else -> ctx.source.playerOrException
        } ?: throw NO_TARGET.create()

        if (!reward.buy(
                RewardContext(
                    Teams.requiredTeam(ctx.source.playerOrException),
                    ctx.source.server,
                    ctx.source.playerOrException,
                    target,
                    reward
                )
            )
        ) throw NOT_ENOUGH.create(reward.price)

        return 1
    }

}