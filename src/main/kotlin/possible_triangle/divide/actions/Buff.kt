package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object Buff : Action {

    private val ALREADY_BUFFED =
        DynamicCommandExceptionType { TextComponent("You already buffed $it") }

    override fun start(ctx: RewardContext) {
        if (Action.isRunning(ctx.server, ctx.reward) { it.target == ctx.target })
            throw ALREADY_BUFFED.create(ctx.reward.display)
    }

    fun isBuffed(player: ServerPlayer, reward: Reward): Boolean {
        return Action.isRunning(player.server, reward) { it.player.uuid == player.uuid }
    }

}