package possible_triangle.divide.reward.actions.secret

import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.hacks.DataHacker.Type.INVISIBLE
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.DataAction

object BlindTeam : DataAction(INVISIBLE) {

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayerEntity> {
        return targets(ctx).filterIsInstance<ServerPlayerEntity>().filter { it != target }
    }

}