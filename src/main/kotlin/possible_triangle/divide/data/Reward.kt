package possible_triangle.divide.data

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.Chat
import possible_triangle.divide.logic.Action
import possible_triangle.divide.logic.CashLogic
import possible_triangle.divide.logic.TeamLogic
import possible_triangle.divide.logic.actions.Buff
import possible_triangle.divide.logic.actions.FindGrave
import possible_triangle.divide.logic.actions.HideNametags
import possible_triangle.divide.logic.actions.TrackPlayer

@Serializable
enum class Reward(
    val display: String,
    val price: Int,
    val action: Action,
    val duration: Int = 0,
    val requiresTarget: Boolean = false
) {

    TRACK_PLAYER("Track Player", 1000, TrackPlayer, duration = 10, requiresTarget = true),
    FIND_GRAVE("Find Grave", 1000, FindGrave, duration = 10),
    //HIDE("Hide from Tracking", 3000, {}, requiresTarget = true),

    HIDE_NAMES("Hide Nametags", 500, HideNametags, duration = 10),

    BUFF_LOOT("Buff Loot-Chance", 100, Buff, duration = 30);

    data class Context(
        val team: PlayerTeam,
        val world: ServerLevel,
        val player: ServerPlayer,
        val target: ServerPlayer,
        val reward: Reward
    )

    companion object {
        private val SAME_TEAM =
            DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" is in your own team")) }

        private val NOT_PLAYING =
            DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" is not playing")) }
    }

    fun buy(ctx: Context): Boolean {
        val canBuy = CashLogic.get(ctx.world, ctx.team) >= price
        if (!canBuy) return false

        if (ctx.target.team?.name == ctx.player.team?.name) throw SAME_TEAM.create(ctx.target.name)
        if (!TeamLogic.isPlayer(ctx.target)) throw NOT_PLAYING.create(ctx.target.name)

        Action.run(action, ctx, duration)
        val bought = CashLogic.modify(ctx.world, ctx.team, -price)
        if (bought) TeamLogic.teammates(ctx.player).forEach {
            Chat.message(
                it, TextComponent("Bought ${ctx.reward.display} for ").append(
                    TextComponent("${ctx.reward.price}").withStyle(
                        ChatFormatting.LIGHT_PURPLE
                    )
                )
            )
        }
        return bought
    }


}

