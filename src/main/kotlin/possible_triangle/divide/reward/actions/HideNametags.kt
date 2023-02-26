package possible_triangle.divide.reward.actions

import net.minecraft.world.scores.Team
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object HideNametags : Action() {

   override fun <T> start(ctx: RewardContext<T>) {
        ctx.targetTeam()?.nameTagVisibility = Team.Visibility.HIDE_FOR_OTHER_TEAMS
    }

   override fun <T> stop(ctx: RewardContext<T>) {
       ctx.targetTeam()?.nameTagVisibility = Team.Visibility.ALWAYS
    }

}