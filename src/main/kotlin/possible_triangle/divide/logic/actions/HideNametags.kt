package possible_triangle.divide.logic.actions

import net.minecraft.world.scores.Team
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.Action

object HideNametags : Action {

    override fun start(ctx: Reward.Context) {
        ctx.team.nameTagVisibility = Team.Visibility.HIDE_FOR_OTHER_TEAMS
    }

    override fun stop(ctx: Reward.Context) {
        ctx.team.nameTagVisibility = Team.Visibility.ALWAYS
    }

}