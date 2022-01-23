package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.hacks.PacketIntercepting
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

abstract class DataAction(
    private val type: DataHacker.Type,
    private val clearOnDeath: Boolean = true,
    private val clearInBase: Boolean = true,
    targets: Target? = Target.PLAYER
) :
    Action(targets) {

    companion object {
        private val ALREADY_TARGETED =
            Dynamic2CommandExceptionType { a, b -> (a as BaseComponent).append(TextComponent(" is already targeted by $b")) }
    }

    abstract fun targets(ctx: RewardContext): List<Entity>
    abstract fun visibleTo(ctx: RewardContext, target: Entity): List<ServerPlayer>

    open fun onStart(ctx: RewardContext) {}
    open fun onStop(ctx: RewardContext) {}
    open fun onPrepare(ctx: RewardContext) {}

    final override fun prepare(ctx: RewardContext) {
        if (isRunning(ctx.server, ctx.reward) { it.target == ctx.target }) {
            ALREADY_TARGETED.create(ctx.target.name, ctx.reward.display)
        }
        onPrepare(ctx)
    }

    final override fun start(ctx: RewardContext) {
        onStart(ctx)
        targets(ctx).forEach {
            DataHacker.addReason(type, it, visibleTo(ctx, it), ctx.reward.duration ?: 0, clearOnDeath, clearInBase)
        }
    }

    final override fun stop(ctx: RewardContext) {
        onStop(ctx)
        targets(ctx).forEach {
            PacketIntercepting.updateData(it, ctx.server)
        }
    }

}