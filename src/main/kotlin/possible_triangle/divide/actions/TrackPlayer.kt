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

    private val PEACE =  SimpleCommandExceptionType(TextComponent("Cannot track players during peace time"))
    private val IN_BASE =
        DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" cannot be tracked")) }

    override fun targets(ctx: RewardContext): List<Entity> {
        return listOf(ctx.target)
    }

    override fun visibleTo(ctx: RewardContext, target: Entity): List<ServerPlayer> {
        return Teams.teammates(ctx.player)
    }

    fun checkRequirements(ctx: RewardContext) {
        if (Bases.isInBase(ctx.target)) throw IN_BASE.create(ctx.target.name)
        if(Eras.isPeace(ctx.server))throw PEACE.create()
    }

    override fun onPrepare(ctx: RewardContext) {
        checkRequirements(ctx)

        Chat.subtitle(ctx.target, "You will be tracked in ${ctx.reward.charge}s")
        Teams.teammates(ctx.player).forEach {
            Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
        }
    }

    override fun onStart(ctx: RewardContext) {
        Teams.teammates(ctx.player).forEach {
            Chat.subtitle(
                it,
                (ctx.target.displayName as MutableComponent).append(
                    TextComponent(" is in ").append(
                        TextComponent(ctx.target.level.dimension().location().path).withStyle(
                            ChatFormatting.GOLD
                        )
                    )
                )
            )
        }

        Chat.subtitle(ctx.target, "You are being tracked")
    }

    override fun onStop(ctx: RewardContext) {
        Teams.teammates(ctx.player).forEach {
            Chat.subtitle(it, TextComponent("No longer tracking ").append(ctx.target.name))
        }
        Chat.subtitle(ctx.target, "You are no longer being tracked")
    }

}