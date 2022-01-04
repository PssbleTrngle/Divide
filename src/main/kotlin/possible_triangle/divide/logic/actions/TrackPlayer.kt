package possible_triangle.divide.logic.actions

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import possible_triangle.divide.Chat
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.Action

object TrackPlayer : Action {

    private val ALREADY_TRACKED =
        DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" is already being tracked")) }

    override fun start(ctx: Reward.Context) {
        if (Action.isRunning(ctx.world, ctx.reward) { it.target == ctx.target })
            throw ALREADY_TRACKED.create(ctx.target.name)

        with(ctx) {
            if (target.level.dimension() != player.level.dimension()) Chat.subtitle(
                player,
                TextComponent("Target is in ").append(
                    TextComponent(target.level.dimension().location().path).setStyle(
                        Style.EMPTY.withColor(ChatFormatting.GOLD)
                    )
                )
            )

            Chat.subtitle(target, "You are being tracked")
        }
    }

    override fun tick(ctx: Reward.Context) {
        ctx.target.addEffect(MobEffectInstance(MobEffects.GLOWING, 2, 0, false, false))
    }

    override fun stop(ctx: Reward.Context) {
        Chat.subtitle(ctx.player, TextComponent("No longer tracking ").append(ctx.target.name))
        Chat.subtitle(ctx.target, "You are no longer being tracked")
    }

}