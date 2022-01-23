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
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext
import java.util.*

object TrackPlayer : DataAction<UUID,ServerPlayer>(GLOWING, ActionTarget.PLAYER) {

    private val PEACE = SimpleCommandExceptionType(TextComponent("Cannot track players during peace time"))
    private val IN_BASE =
        DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" cannot be tracked")) }

    override fun targets(ctx: RewardContext<UUID, ServerPlayer>): List<Entity> {
        return listOfNotNull(ctx.target)
    }

    override fun visibleTo(ctx: RewardContext<UUID, ServerPlayer>, target: Entity): List<ServerPlayer> {
        return Teams.teammates(ctx.player ?: return emptyList())
    }

    fun checkRequirements(ctx: RewardContext<UUID, ServerPlayer>) {
        ctx.ifComplete { _, target ->
            if (Bases.isInBase(target)) throw IN_BASE.create(target.name)
            if (Eras.isPeace(ctx.server)) throw PEACE.create()
        } ?: throw NOT_ONLINE.create()
    }

    override fun onPrepare(ctx: RewardContext<UUID, ServerPlayer>) {
        checkRequirements(ctx)

        ctx.ifComplete { player, target ->
            Chat.subtitle(target, "You will be tracked in ${ctx.reward.charge}s")
            Teams.teammates(player).forEach {
                Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
            }
        }
    }

    override fun onStart(ctx: RewardContext<UUID, ServerPlayer>) {
        ctx.ifComplete { player, target ->
            Teams.teammates(player).forEach {
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

            Chat.subtitle(target, "You are being tracked")
        }
    }

    override fun onStop(ctx: RewardContext<UUID, ServerPlayer>) {
        ctx.ifComplete { player, target ->
            Teams.teammates(player).forEach {
                Chat.subtitle(it, TextComponent("No longer tracking ").append(target.name))
            }
            Chat.subtitle(target, "You are no longer being tracked")
        }
    }

}