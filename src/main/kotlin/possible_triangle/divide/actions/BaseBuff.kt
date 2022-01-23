package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

abstract class BaseBuff : Action() {

    companion object {
        private val ALREADY_BUFFED =
            DynamicCommandExceptionType { TextComponent("You already buffed $it") }


        fun isBuffed(player: ServerPlayer, reward: Reward): Boolean {
            return Action.isRunning(player.server, reward) { ctx ->
                val action = ctx.reward.action
                if (action !is BaseBuff) false
                else action.buffs(ctx).any { it.uuid == player.uuid }
            }
        }
    }

    final override fun start(ctx: RewardContext) {
        if (isBuffed(ctx.player, ctx.reward)) throw ALREADY_BUFFED.create(ctx.reward.display)
    }

    abstract fun buffs(ctx: RewardContext): List<ServerPlayer>

}