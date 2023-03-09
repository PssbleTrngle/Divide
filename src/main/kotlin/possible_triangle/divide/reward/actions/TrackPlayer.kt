package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.events.Eras
import possible_triangle.divide.extensions.id
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Bases.isInBase
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.reward.RewardContext

object TrackPlayer : DataAction(GLOWING) {

    private val PEACE = SimpleCommandExceptionType(Component.literal("Cannot track players during peace time"))
    private val IN_BASE =
        DynamicCommandExceptionType { (it as MutableComponent).append(Component.literal(" cannot be tracked")) }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer> {
        return (ctx.player ?: return emptyList()).teammates()
    }

    fun <T> checkRequirements(ctx: RewardContext<T>) {
        ctx.targetPlayers().find { target ->
            if (target.isInBase()) throw IN_BASE.create(target.name)
            if (Eras.isPeace(ctx.server)) throw PEACE.create()
            true
        } ?: throw NOT_ONLINE.create()
    }

    override fun <T> onPrepare(ctx: RewardContext<T>) {
        checkRequirements(ctx)

        ctx.notify(
            playerMsg = "Tracking ${ctx.reward.charge}",
            targetMsg = "You will be tracked ${ctx.reward.charge}"
        )
    }

    override fun <T> onStart(ctx: RewardContext<T>) {
        val target = ctx.targetPlayer() ?: return

        ctx.team.participants(ctx.server).forEach {
            Chat.subtitle(
                it,
                (target.displayName as MutableComponent).append(
                    Component.literal(" is in ").append(
                        Component.literal(target.level.id().path).withStyle(
                            ChatFormatting.GOLD
                        )
                    )
                )
            )
        }

        ctx.targetPlayers().forEach {
            Chat.subtitle(it, "You are being tracked")
        }
    }

    override fun <T> onStop(ctx: RewardContext<T>) {
        val target = ctx.targetPlayer() ?: return

        ctx.notify(
            playerMsg = "No longer tracking ${target.scoreboardName}",
            targetMsg = "You are no longer being tracked"
        )
    }

}