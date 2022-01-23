package possible_triangle.divide.actions.secret

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.actions.DataAction
import possible_triangle.divide.hacks.DataHacker.Type.INVISIBLE
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object BlindTeam : DataAction<String, PlayerTeam>(INVISIBLE, ActionTarget.TEAM) {

    override fun targets(ctx: RewardContext<String, PlayerTeam>): List<Entity> {
        return Teams.players(ctx.server, ctx.target ?: return emptyList())
    }

    override fun visibleTo(ctx: RewardContext<String, PlayerTeam>, target: Entity): List<ServerPlayer> {
        return targets(ctx).filterIsInstance(ServerPlayer::class.java).filter { it != target }
    }

}