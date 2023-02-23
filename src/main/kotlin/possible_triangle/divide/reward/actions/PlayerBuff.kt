package possible_triangle.divide.reward.actions

import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.reward.RewardContext

object PlayerBuff : BaseBuff() {

   override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayerEntity> {
        return listOfNotNull(ctx.targetPlayer())
    }

}