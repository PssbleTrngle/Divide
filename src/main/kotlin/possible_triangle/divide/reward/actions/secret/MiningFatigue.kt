package possible_triangle.divide.reward.actions.secret

import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.BaseBuff

object MiningFatigue : BaseBuff() {

    fun modifyBreakSpeed(player: ServerPlayerEntity, original: Float): Float {
        return if (isBuffed(player, Reward.MINING_FATIGUE)) original * 0.7F
        else original
    }

    override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayerEntity> {
        return ctx.targetPlayers()
    }

}