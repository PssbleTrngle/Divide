package possible_triangle.divide.reward.actions.secret

import io.github.fabricators_of_create.porting_lib.event.common.PlayerBreakSpeedCallback
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.BaseBuff

object MiningFatigue : BaseBuff() {

    init {
        PlayerBreakSpeedCallback.EVENT.register { event ->
            val player = event.player
            if (player !is ServerPlayerEntity) return@register
            if (isBuffed(player, Reward.MINING_FATIGUE)) event.newSpeed *= 0.7F
        }
    }

    override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayerEntity> {
        return ctx.targetPlayers()
    }

}