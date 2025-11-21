package possible_triangle.divide.reward.actions

import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object GetLevel : Action() {

    override fun <T> start(ctx: RewardContext<T>) {
        ctx.targetPlayers().forEach {
            it.giveExperienceLevels(5)
        }
    }

}