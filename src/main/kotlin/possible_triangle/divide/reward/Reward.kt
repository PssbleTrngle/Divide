package possible_triangle.divide.reward

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.ChatFormatting
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
        val boughtBy: EventPlayer? = null,
        val target: EventPlayer? = null,
        val pointsPaid: Int? = null,
        val pointsNow: Int? = null,
    )

    companion object :
        DefaultedResource<Reward>("rewards", { Reward.serializer() }) {

        private val LOGGER = EventLogger("reward") { Event.serializer() }

        private val ACTIONS = hashMapOf<String, Action<*, *>>()

        private fun <R, T> register(id: String, action: Action<R, T>, reward: () -> Reward): Delegate {
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


        fun <R, T> buy(ctx: RewardContext<R, T>): Boolean {
            return ctx.ifComplete { player, _ ->
                with(ctx.reward) {
                    Points.modify(ctx.server, ctx.team, -price) { pointsNow ->

                        LOGGER.log(
                            ctx.server,
                            Event(
                                id,
                                EventPlayer.optional(ctx.player),
                                ctx.targetEvent(),
                                price,
                                pointsNow,
                            )
                        )

                        Teams.teammates(player).forEach {
                            Chat.message(
                                it, TextComponent("Bought ${ctx.reward.display} for ").append(
                                    TextComponent("${ctx.reward.price}").withStyle(
                                        ChatFormatting.LIGHT_PURPLE
                                    )
                                )
                            )
                        }

                        Action.run(ctx, duration)

                    }
                }
            } ?: throw Action.NOT_ONLINE.create()
        }

    }

    @Suppress("UNCHECKED_CAST")
    val action: Action<*, *>
        get() = (ACTIONS[id] ?: throw NullPointerException("Action for $id missing"))

}

