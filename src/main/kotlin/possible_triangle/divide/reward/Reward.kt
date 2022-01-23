package possible_triangle.divide.reward

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.BaseComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.actions.*
import possible_triangle.divide.actions.secret.BlindTeam
import possible_triangle.divide.actions.secret.MiningFatigue
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
    val duration: Int? = null,
    val charge: Int? = null,
    val secret: Boolean = false,
) {

    @Transient
    lateinit var id: String
        private set

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

        override fun populate(entry: Reward, server: MinecraftServer?, id: String) {
            entry.id = id
        }

        val TRACK_PLAYER by register("track_player", TrackPlayer) { Reward("Track Player", 1000, duration = 60 * 5) }
        val TRACK_PLAYER_WEAK by register("track_player_weak", TrackPlayerWeak) {
            Reward(
                "Track Players",
                500,
                duration = 60 * 5
            )
        }

        val FIND_GRAVE by register("find_grave", FindGrave) { Reward("Find Grave", 50, duration = 60 * 10) }

        val HIDE_NAMES by register("hide_names", HideNametags) { Reward("Hide Nametags", 50, duration = 10) }

        val BUFF_LOOT by register("buff_loot", PlayerBuff) { Reward("Buff Loot-Chance", 100, duration = 60 * 1) }

        val BUFF_CROPS by register("boost_crops", TeamBuff) { Reward("Boost Crop Growth", 100, duration = 60 * 5) }

        val LOOT_CRATE by register("loot_crate", OrderLootCrate) { Reward("Order a loot crate", 800, charge = 60 * 5) }

        val BLIND_TEAM by register("blind_team", BlindTeam) {
            Reward(
                "Order a loot crate",
                800,
                duration = 60 * 5,
                secret = true
            )
        }

        val MINING_FATIGUE by register("slow_minespeed", MiningFatigue) {
            Reward(
                "Give a team mining fatigue",
                800,
                duration = 60 * 5,
                secret = true
            )
        }

    }

    val action: Action
        get() = ACTIONS[id] ?: throw NullPointerException("Action for $id missing")

    fun buy(ctx: RewardContext): Boolean {
        return Points.modify(ctx.server, ctx.team, -price) { pointsNow ->

            if (ctx.reward.action.targets != null && ctx.target.team?.name == ctx.player.team?.name) throw SAME_TEAM.create(
                ctx.target.name
            )
            if (!Teams.isPlayer(ctx.target)) throw NOT_PLAYING.create(ctx.target.name)

            Action.run(action, ctx, duration)

            LOGGER.log(
                ctx.server,
                Event(
                    id,
                    EventPlayer.of(ctx.player),
                    EventPlayer.of(ctx.target).takeIf { ctx.target != ctx.player },
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

