package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.events.Eras
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.RewardContext

object TrackPlayer : DataAction(GLOWING) {

    private val PEACE = SimpleCommandExceptionType(TextComponent("Cannot track players during peace time"))
    private val IN_BASE =
        DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" cannot be tracked")) }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer> {
        return Teams.teammates(ctx.player ?: return emptyList())
    }

    fun <T> checkRequirements(ctx: RewardContext<T>) {
        ctx.targetPlayers().find { target ->
            if (Bases.isInBase(target)) throw IN_BASE.create(target.name)
            if (Eras.isPeace(ctx.server)) throw PEACE.create()
            true
        } ?: throw NOT_ONLINE.create()
    }

    override fun <T> onPrepare(ctx: RewardContext<T>) {
        checkRequirements(ctx)

        ctx.targetPlayers().forEach {
            Chat.subtitle(it, "You will be tracked in ${ctx.reward.charge}s")
        }

        Teams.players(ctx.server, ctx.team).forEach {
            Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
        }

    }

    override fun <T> onStart(ctx: RewardContext<T>) {
        val target = ctx.targetPlayer() ?: return

        Teams.players(ctx.server, ctx.team).forEach {
            Chat.subtitle(
                it,
                (target.displayName as MutableComponent).append(
                    TextComponent(" is in ").append(
                        TextComponent(target.level.dimension().location().path).withStyle(
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

        Teams.players(ctx.server, ctx.team).forEach {
            Chat.subtitle(it, TextComponent("No longer tracking ").append(target.name))
        }

        ctx.targetPlayers().forEach {
            Chat.subtitle(it, "You are no longer being tracked")
        }
    }

}