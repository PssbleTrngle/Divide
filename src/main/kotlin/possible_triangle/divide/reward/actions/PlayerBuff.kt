package possible_triangle.divide.reward.actions

import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.reward.RewardContext

object PlayerBuff : BaseBuff() {

   override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayer> {
        return listOfNotNull(ctx.targetPlayer())
    }

}