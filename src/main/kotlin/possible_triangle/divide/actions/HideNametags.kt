package possible_triangle.divide.actions

import net.minecraft.world.scores.Team
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object HideNametags : Action<Unit,Unit>(ActionTarget.NONE) {

    override fun start(ctx: RewardContext<Unit, Unit>) {
        ctx.team.nameTagVisibility = Team.Visibility.HIDE_FOR_OTHER_TEAMS
    }

    override fun stop(ctx: RewardContext<Unit, Unit>) {
        ctx.team.nameTagVisibility = Team.Visibility.ALWAYS
    }

}