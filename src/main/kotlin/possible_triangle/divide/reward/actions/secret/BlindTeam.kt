package possible_triangle.divide.reward.actions.secret

import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.hacks.DataHacker.Type.INVISIBLE
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.DataAction

object BlindTeam : DataAction(INVISIBLE) {

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer> {
        return targets(ctx).filterIsInstance<ServerPlayer>().filter { it != target }
    }

}