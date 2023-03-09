package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.hacks.PacketIntercepting
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

abstract class DataAction(
    private val type: DataHacker.Type,
    private val clearOnDeath: Boolean = true,
    private val clearInBase: Boolean = false,
) :
    Action() {

    companion object {
        private val ALREADY_TARGETED =
            Dynamic2CommandExceptionType { a, b -> Component.literal("$a is already targeted by $b") }
    }

    abstract fun <T> targets(ctx: RewardContext<T>): List<Entity>
    abstract fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer>

    open fun <T> onStart(ctx: RewardContext<T>) {}
    open fun <T> onStop(ctx: RewardContext<T>) {}
    open fun <T> onPrepare(ctx: RewardContext<T>) {}

    final override fun <T> prepare(ctx: RewardContext<T>) {
        val worthless = ctx.targetPlayers().all { target -> isRunning(ctx.server, ctx.reward) { it.target == target } }
        if (worthless) {
            throw ALREADY_TARGETED.create(ctx.targetEvent()?.name, ctx.reward.display)
        }
        onPrepare(ctx)
    }

    final override fun <T> start(ctx: RewardContext<T>) {
        onStart(ctx)
        if (ctx.reward.duration != null)
            targets(ctx).forEach {
                DataHacker.addReason(
                    type,
                    it,
                    visibleTo(ctx, it),
                    ctx.reward.duration,
                    clearOnDeath,
                    clearInBase
                )
            }
    }

    final override fun <T> stop(ctx: RewardContext<T>) {
        onStop(ctx)
        targets(ctx).forEach {
            PacketIntercepting.updateData(it, ctx.server)
        }
    }

}