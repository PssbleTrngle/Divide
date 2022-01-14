package possible_triangle.divide.reward

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.TextComponent
import possible_triangle.divide.actions.Buff
import possible_triangle.divide.actions.FindGrave
import possible_triangle.divide.actions.HideNametags
import possible_triangle.divide.actions.TrackPlayer
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.EventPlayer
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams

@Serializable
data class Reward(
    val display: String,
    val price: Int,
    val duration: Int?,
    val requiresTarget: Boolean = false
) {

    @Serializable
    data class Event(
        val reward: String,
        val boughtBy: EventPlayer,
        val target: EventPlayer? = null,
        val pointsPaid: Int? = null,
        val pointsNow: Int? = null,
    )

    companion object : DefaultedResource<Reward>("rewards", { Reward.serializer() }) {

        private val LOGGER = EventLogger("reward") { Event.serializer() }

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
        return Points.modify(ctx.server, ctx.team, -price) { pointsNow ->

            if (ctx.reward.requiresTarget && ctx.target.team?.name == ctx.player.team?.name) throw SAME_TEAM.create(ctx.target.name)
            if (!Teams.isPlayer(ctx.target)) throw NOT_PLAYING.create(ctx.target.name)

            Action.run(action, ctx, duration)

            LOGGER.log(
                ctx.server,
                Event(
                    idOf(this),
                    EventPlayer.of(ctx.player),
                    if (requiresTarget) EventPlayer.of(ctx.target) else null,
                    price,
                    pointsNow,
                )
            )

            Teams.teammates(ctx.player).forEach {
                Chat.message(
                    it, TextComponent("Bought ${ctx.reward.display} for ").append(
                        TextComponent("${ctx.reward.price}").withStyle(
                            ChatFormatting.LIGHT_PURPLE
                        )
                    )
                )
            }
        }
    }


}

