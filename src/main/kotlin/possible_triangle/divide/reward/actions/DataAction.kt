package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

abstract class DataAction(
    private val type: DataHacker.Type,
    private val clearOnDeath: Boolean = true,
    private val clearInBase: Boolean = true,
) :
    Action() {

    companion object {
        private val ALREADY_TARGETED =
            Dynamic2CommandExceptionType { a, b -> Text.literal("$a is already targeted by $b") }
    }

    abstract fun <T> targets(ctx: RewardContext<T>): List<Entity>
    abstract fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayerEntity>

    open fun <T> onStart(ctx: RewardContext<T>) {}
    open fun <T> onStop(ctx: RewardContext<T>) {}
    open fun <T> onPrepare(ctx: RewardContext<T>) {}

    final override fun <T> prepare(ctx: RewardContext<T>) {
        if (isRunning(ctx.server, ctx.reward) { it.target == ctx.target }) {
            throw ALREADY_TARGETED.create(ctx.targetEvent()?.name, ctx.reward.display)
        }
        onPrepare(ctx)
    }

    final override fun <T> start(ctx: RewardContext<T>) {
        onStart(ctx)
        targets(ctx).forEach {
            DataHacker.addReason(type, it, visibleTo(ctx, it), ctx.reward.duration ?: 0, clearOnDeath, clearInBase)
        }
    }

    final override fun <T> stop(ctx: RewardContext<T>) {
        onStop(ctx)
        targets(ctx).forEach {
            //PacketIntercepting.updateData(it, ctx.server)
        }
    }

}