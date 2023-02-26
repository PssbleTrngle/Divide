package possible_triangle.divide.reward.actions.secret

import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.BaseBuff

object MiningFatigue : BaseBuff() {

    const val MODIFIER = 0.7F

    override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayer> {
        return ctx.targetPlayers()
    }

}