package possible_triangle.divide.logic.actions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.logic.Action
import possible_triangle.divide.logic.Glowing
import possible_triangle.divide.reward.RewardContext

abstract class GlowingAction : Action {

    abstract fun targets(ctx: RewardContext): List<Entity>
    abstract fun visibleTo(ctx: RewardContext): List<ServerPlayer>

    open fun onStart(ctx: RewardContext) {}
    open fun onStop(ctx: RewardContext) {}

    final override fun start(ctx: RewardContext) {
        onStart(ctx)
        targets(ctx).forEach {
            Glowing.addReason(it, visibleTo(ctx), ctx.reward.duration ?: 0)
        }
    }

    final override fun stop(ctx: RewardContext) {
        onStop(ctx)
        targets(ctx).forEach {
            Glowing.updateGlowingData(it, ctx.world)
        }
    }

}