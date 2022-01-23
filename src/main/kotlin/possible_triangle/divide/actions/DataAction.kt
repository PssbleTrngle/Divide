package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.hacks.PacketIntercepting
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

abstract class DataAction<Raw,Target>(
    private val type: DataHacker.Type,
    targets: ActionTarget<Raw,Target>,
    private val clearOnDeath: Boolean = true,
    private val clearInBase: Boolean = true,
) :
    Action<Raw,Target>(targets) {

    companion object {
        private val ALREADY_TARGETED =
            Dynamic2CommandExceptionType { a, b -> TextComponent("$a is already targeted by $b") }
    }

    abstract fun targets(ctx: RewardContext<Raw, Target>): List<Entity>
    abstract fun visibleTo(ctx: RewardContext<Raw, Target>, target: Entity): List<ServerPlayer>

    open fun onStart(ctx: RewardContext<Raw, Target>) {}
    open fun onStop(ctx: RewardContext<Raw, Target>) {}
    open fun onPrepare(ctx: RewardContext<Raw, Target>) {}

    final override fun prepare(ctx: RewardContext<Raw, Target>) {
        if (isRunning(ctx.server, this) { it.target == ctx.target }) {
            ALREADY_TARGETED.create(ctx.targetEvent()?.name, ctx.reward.display)
        }
        onPrepare(ctx)
    }

    final override fun start(ctx: RewardContext<Raw, Target>) {
        onStart(ctx)
        targets(ctx).forEach {
            DataHacker.addReason(type, it, visibleTo(ctx, it), ctx.reward.duration ?: 0, clearOnDeath, clearInBase)
        }
    }

    final override fun stop(ctx: RewardContext<Raw, Target>) {
        onStop(ctx)
        targets(ctx).forEach {
            PacketIntercepting.updateData(it, ctx.server)
        }
    }

}