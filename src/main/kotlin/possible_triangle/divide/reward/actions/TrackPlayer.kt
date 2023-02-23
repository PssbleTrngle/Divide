package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import possible_triangle.divide.events.Eras
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Bases.isInBase
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.reward.RewardContext

object TrackPlayer : DataAction(GLOWING) {

    private val PEACE = SimpleCommandExceptionType(Text.literal("Cannot track players during peace time"))
    private val IN_BASE =
        DynamicCommandExceptionType { (it as MutableText).append(Text.literal(" cannot be tracked")) }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayerEntity> {
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

        ctx.targetPlayers().forEach {
            Chat.subtitle(it, "You will be tracked in ${ctx.reward.charge}s")
        }

        ctx.team.participants(ctx.server).forEach {
            Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
        }

    }

    override fun <T> onStart(ctx: RewardContext<T>) {
        val target = ctx.targetPlayer() ?: return

        ctx.team.participants(ctx.server).forEach {
            Chat.subtitle(
                it,
                (target.displayName as MutableText).append(
                    Text.literal(" is in ").append(
                        Text.literal(target.world.dimensionKey.value.path).formatted(
                            Formatting.GOLD
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

        ctx.team.participants(ctx.server).forEach {
            Chat.subtitle(it, Text.literal("No longer tracking ").append(target.name))
        }

        ctx.targetPlayers().forEach {
            Chat.subtitle(it, "You are no longer being tracked")
        }
    }

}