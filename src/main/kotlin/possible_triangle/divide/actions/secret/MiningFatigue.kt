package possible_triangle.divide.actions.secret

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object MiningFatigue : Action<String, PlayerTeam>(ActionTarget.TEAM) {

    override fun start(ctx: RewardContext<String, PlayerTeam>) {
        val effect = MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * (ctx.reward.duration ?: 0), 1, false, false, false)
        Teams.players(ctx.server, ctx.target ?: return).forEach {
            it.addEffect(effect)
        }
    }

}