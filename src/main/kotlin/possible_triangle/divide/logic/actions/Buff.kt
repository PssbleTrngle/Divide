package possible_triangle.divide.logic.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.Action

object Buff : Action {

    private val ALREADY_BUFFED =
        DynamicCommandExceptionType { TextComponent("You already buffed $it") }

    override fun start(ctx: Reward.Context) {
        if (Action.isRunning(ctx.world, ctx.reward) { it.target == ctx.target })
            throw ALREADY_BUFFED.create(ctx.reward.display)
    }

    fun isBuffed(player: ServerPlayer, reward: Reward): Boolean {
        return Action.isRunning(player.level as ServerLevel, reward) { it.player.uuid == player.uuid }
    }

}