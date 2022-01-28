package possible_triangle.divide.reward.actions.secret

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.BaseBuff

@Mod.EventBusSubscriber
object MiningFatigue : BaseBuff() {

    @SubscribeEvent
    fun miningSpeed(event: PlayerEvent.BreakSpeed) {
        val player = event.player
        if (player !is ServerPlayer) return
        if (isBuffed(player, Reward.MINING_FATIGUE)) event.newSpeed *= 0.7F
    }

    override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayer> {
        return ctx.targetPlayers()
    }

}