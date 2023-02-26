package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

abstract class BaseBuff : Action() {

    companion object {
        val ALREADY_BUFFED =
            DynamicCommandExceptionType { Component.literal("You already buffed $it") }

        fun isBuffed(player: ServerPlayer, reward: Reward): Boolean {
            return isRunning(player.server, reward) { ctx ->
                val action = reward.action
                if (action !is BaseBuff) false
                else action.buffs(ctx).any { it.uuid == player.uuid }
            }
        }
    }

    final override fun <T> prepare(ctx: RewardContext<T>) {
        if (buffs(ctx).all {
            isBuffed(it, ctx.reward)
        }) throw ALREADY_BUFFED.create(ctx.reward.display)
    }

    abstract fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayer>

}