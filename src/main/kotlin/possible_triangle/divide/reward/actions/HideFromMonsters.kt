package possible_triangle.divide.reward.actions

import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object HideFromMonster : Action() {

    override fun <T> start(ctx: RewardContext<T>) {
        ctx.notify(targetMsg = "You're hidden from monsters again")
    }

    override fun <T> stop(ctx: RewardContext<T>) {
        ctx.notify(targetMsg = "You're visible to monsters again")
    }

}