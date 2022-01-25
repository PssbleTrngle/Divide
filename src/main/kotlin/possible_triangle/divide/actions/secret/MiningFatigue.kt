package possible_triangle.divide.actions.secret

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object MiningFatigue : Action() {

    override fun <T> start(ctx: RewardContext<T>) {
        val effect = MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * (ctx.reward.duration ?: 0), 1, false, false, false)
        ctx.targetPlayers().forEach {
            it.addEffect(effect)
        }
    }

}