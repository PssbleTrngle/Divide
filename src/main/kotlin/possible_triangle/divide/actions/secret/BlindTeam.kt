package possible_triangle.divide.actions.secret

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.actions.DataAction
import possible_triangle.divide.hacks.DataHacker.Type.INVISIBLE
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.RewardContext

object BlindTeam : DataAction(INVISIBLE, targets = Target.TEAM) {

    override fun targets(ctx: RewardContext): List<Entity> {
        return Teams.teammates(ctx.target)
    }

    override fun visibleTo(ctx: RewardContext, target: Entity): List<ServerPlayer> {
        return Teams.teammates(ctx.target).filter { it != target }
    }

}