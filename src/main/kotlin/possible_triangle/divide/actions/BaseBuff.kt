package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

abstract class BaseBuff : Action<Unit, Unit>(ActionTarget.NONE) {

    companion object {
        private val ALREADY_BUFFED =
            DynamicCommandExceptionType { TextComponent("You already buffed $it") }

        fun isBuffed(player: ServerPlayer, reward: Reward): Boolean {
            return isRunning(player.server, reward.action) { ctx ->
                val action = ctx.action
                if (action !is BaseBuff) false
                else action.buffs(ctx as RewardContext<Unit, Unit>).any { it.uuid == player.uuid }
            }
        }
    }

    final override fun prepare(ctx: RewardContext<Unit, Unit>) {
        ctx.ifComplete { player, _ ->
            if (isBuffed(player, ctx.reward)) throw ALREADY_BUFFED.create(ctx.reward.display)
        }
    }

    abstract fun buffs(ctx: RewardContext<Unit, Unit>): List<ServerPlayer>

}