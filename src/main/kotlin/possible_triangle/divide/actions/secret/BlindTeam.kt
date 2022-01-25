package possible_triangle.divide.actions.secret

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.actions.DataAction
import possible_triangle.divide.hacks.DataHacker.Type.INVISIBLE
import possible_triangle.divide.reward.RewardContext

object BlindTeam : DataAction(INVISIBLE) {

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer> {
        return targets(ctx).filterIsInstance(ServerPlayer::class.java).filter { it != target }
    }

}