package possible_triangle.divide.reward.actions

import net.minecraft.world.scores.Team
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object ShowNametags : Action() {

    override fun <T> prepare(ctx: RewardContext<T>) {
        ctx.notify(
            targetMsg = "You're nametag will be revealed in ${ctx.reward.charge}s",
            playerMsg = "Revealing nametags in ${ctx.reward.charge}s"
        )
    }

    override fun <T> start(ctx: RewardContext<T>) {
        ctx.notify(targetMsg = "You're nametag has been revealed")
        ctx.targetTeam()?.nameTagVisibility = Team.Visibility.ALWAYS
    }

    override fun <T> stop(ctx: RewardContext<T>) {
        ctx.notify(targetMsg = "You're nametag is no longer visible")
        ctx.targetTeam()?.nameTagVisibility = Team.Visibility.HIDE_FOR_OTHER_TEAMS
    }

}