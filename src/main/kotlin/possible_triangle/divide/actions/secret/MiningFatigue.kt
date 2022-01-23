package possible_triangle.divide.actions.secret

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object MiningFatigue : Action(targets = Target.TEAM) {

    override fun start(ctx: RewardContext) {
        val effect = MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * (ctx.reward.duration ?: 0), 1, false, false, false)
        Teams.teammates(ctx.target).forEach {
            it.addEffect(effect)
        }
    }

}