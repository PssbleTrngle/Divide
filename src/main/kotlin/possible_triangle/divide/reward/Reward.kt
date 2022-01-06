package possible_triangle.divide.reward

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.TextComponent
import possible_triangle.divide.Chat
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.Action
import possible_triangle.divide.logic.CashLogic
import possible_triangle.divide.logic.TeamLogic
import possible_triangle.divide.logic.actions.Buff
import possible_triangle.divide.logic.actions.FindGrave
import possible_triangle.divide.logic.actions.HideNametags
import possible_triangle.divide.logic.actions.TrackPlayer

@Serializable
data class Reward(
    val display: String,
    val price: Int,
    val duration: Int?,
    val requiresTarget: Boolean = false
) {

    companion object : DefaultedResource<Reward>("rewards", { Reward.serializer() }) {

        private val SAME_TEAM =
            DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" is in your own team")) }

        private val NOT_PLAYING =
            DynamicCommandExceptionType { (it as BaseComponent).append(TextComponent(" is not playing")) }

        private val ACTIONS = hashMapOf<String, Action>()

        private fun register(id: String, action: Action, reward: () -> Reward): Delegate {
            ACTIONS[id.lowercase()] = action
            return defaulted(id, reward)
        }

        val TRACK_PLAYER by register("TRACK_PLAYER", TrackPlayer) {
            Reward(
                "Track Player",
                1000,
                duration = 10,
                requiresTarget = true
            )
        }

        val FIND_GRAVE by register("FIND_GRAVE", FindGrave) { Reward("Find Grave", 1000, duration = 10) }

        val HIDE_NAMES by register("HIDE_NAMES", HideNametags) { Reward("Hide Nametags", 500, duration = 10) }

        val BUFF_LOOT by register("BUFF_LOOT", Buff) { Reward("Buff Loot-Chance", 100, duration = 30) }

    }

    val action: Action
        get() = ACTIONS[idOf(this)] ?: throw  NullPointerException("Action for ${idOf(this)} missing")

    fun buy(ctx: RewardContext): Boolean {
        val canBuy = CashLogic.get(ctx.world, ctx.team) >= price
        if (!canBuy) return false

        if (ctx.reward.requiresTarget && ctx.target.team?.name == ctx.player.team?.name) throw SAME_TEAM.create(ctx.target.name)
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

