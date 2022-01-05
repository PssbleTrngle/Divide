package possible_triangle.divide.logic.actions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.Action
import possible_triangle.divide.logic.Glowing

abstract class GlowingAction : Action {

    abstract fun targets(ctx: Reward.Context): List<Entity>
    abstract fun visibleTo(ctx: Reward.Context): List<ServerPlayer>

    open fun onStart(ctx: Reward.Context) {}
    open fun onStop(ctx: Reward.Context) {}

    final override fun start(ctx: Reward.Context) {
        onStart(ctx)
        targets(ctx).forEach {
            Glowing.addReason(it, visibleTo(ctx), ctx.reward.duration)
        }
    }

    final override fun stop(ctx: Reward.Context) {
        onStop(ctx)
        targets(ctx).forEach {
            Glowing.updateGlowingData(it, ctx.world)
        }
    }

}