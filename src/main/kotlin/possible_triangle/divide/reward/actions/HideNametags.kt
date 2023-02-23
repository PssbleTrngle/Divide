package possible_triangle.divide.reward.actions

import net.minecraft.scoreboard.AbstractTeam.VisibilityRule
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object HideNametags : Action() {

   override fun <T> start(ctx: RewardContext<T>) {
        ctx.targetTeam()?.nameTagVisibilityRule = VisibilityRule.HIDE_FOR_OTHER_TEAMS
    }

   override fun <T> stop(ctx: RewardContext<T>) {
       ctx.targetTeam()?.nameTagVisibilityRule = VisibilityRule.ALWAYS
    }

}