package possible_triangle.divide.logic.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.Chat
import possible_triangle.divide.logic.Action
import possible_triangle.divide.logic.TeamLogic
import possible_triangle.divide.reward.RewardContext

object TrackPlayer : GlowingAction() {

    private val ALREADY_TRACKED =
        DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" is already being tracked")) }

    override fun targets(ctx: RewardContext): List<Entity> {
        return listOf(ctx.target)
    }

    override fun visibleTo(ctx: RewardContext): List<ServerPlayer> {
        return TeamLogic.teammates(ctx.player)
    }

    override fun onStart(ctx: RewardContext) {
        if (Action.isRunning(ctx.world, ctx.reward) { it.target == ctx.target })
            throw ALREADY_TRACKED.create(ctx.target.name)

        with(ctx) {
            if (target.level.dimension() != player.level.dimension())
                TeamLogic.teammates(ctx.player).forEach {
                    Chat.subtitle(
                        it,
                        TextComponent("Target is in ").append(
                            TextComponent(target.level.dimension().location().path).withStyle(
                                ChatFormatting.GOLD
                            )
                        )
                    )
                }

            Chat.subtitle(target, "You are being tracked")
        }
    }

    override fun onStop(ctx: RewardContext) {
        TeamLogic.teammates(ctx.player).forEach {
            Chat.subtitle(it, TextComponent("No longer tracking ").append(ctx.target.name))
        }
        Chat.subtitle(ctx.target, "You are no longer being tracked")
    }

}