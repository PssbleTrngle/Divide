package possible_triangle.divide.actions

import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.reward.RewardContext

object PlayerBuff : BaseBuff() {

    override fun buffs(ctx: RewardContext): List<ServerPlayer> {
        return listOf(ctx.player)
    }

}