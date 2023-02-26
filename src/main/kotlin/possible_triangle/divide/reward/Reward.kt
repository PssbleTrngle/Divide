package possible_triangle.divide.reward

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.m
import possible_triangle.divide.reward.actions.*
import possible_triangle.divide.reward.actions.secret.BlindTeam
import possible_triangle.divide.reward.actions.secret.MiningFatigue
import possible_triangle.divide.reward.actions.secret.ScarePlayer

@Serializable
data class Reward(
    val display: String,
    val price: Int,
    val duration: Int? = null,
    val charge: Int? = null,
    val secret: Boolean = false,
    @Serializable(with = ActionTarget.Serializer::class) @SerialName("target") private val targetType: String = ActionTarget.NONE.id,
) {

    @Transient
    lateinit var id: String
        private set

    @Serializable
    data class Event(
        val reward: String,
        val boughtBy: EventTarget,
        val target: EventTarget? = null,
        val pointsPaid: Int,
        val pointsNow: Int,
    )

    val target
        get() = ActionTarget[targetType] ?: throw IllegalArgumentException("Unknown ActionTarget $targetType")

    companion object : DefaultedResource<Reward>("rewards", { Reward.serializer() }) {

        private val LOGGER = EventLogger("reward", { Event.serializer() }) { inTeam { it.boughtBy.team } }

        private val ACTIONS = hashMapOf<String, Action>()

        override fun isVisible(entry: Reward, team: PlayerTeam?, server: MinecraftServer): Boolean {
            return !entry.secret || SecretRewards.isVisible(server, team ?: return false, entry)
        }

        private fun register(id: String, action: Action, reward: () -> Reward): Delegate {
            ACTIONS[id.lowercase()] = action
            return defaulted(id, reward)
        }

        override fun populate(entry: Reward, server: MinecraftServer?, id: String) {
            entry.id = id
        }

        val TRACK_PLAYER by register("track_player", TrackPlayer) {
            Reward(
                "Track Player",
                1000,
                duration = 2.m,
                charge = 1.m,
                targetType = ActionTarget.PLAYER.id,
            )
        }
        val TRACK_PLAYER_WEAK by register("track_player_weak", TrackPlayerWeak) {
            Reward(
                "Track Player (Weak)",
                500,
                duration = 5.m,
                charge = 30,
                targetType = ActionTarget.PLAYER.id,
            )
        }

        val FIND_GRAVE by register("find_grave", FindGrave) { Reward("Find Grave", 50, duration = 10.m) }

        val HIDE_NAMES by register("hide_names", HideNametags) { Reward("Hide Nametags", 100, duration = 20) }

        val BUFF_LOOT by register("buff_loot", PlayerBuff) { Reward("Buff Loot-Chance", 100, duration = 1.m) }

        val BUFF_CROPS by register("boost_crops", TeamBuff) { Reward("Boost Crop-Growth", 200, duration = 10.m) }

        val LOOT_CRATE by register("loot_crate", OrderLootCrate) { Reward("Order a Loot-Crate", 800, charge = 5.m) }

        val BLIND_TEAM by register("blind_team", BlindTeam) {
            Reward(
                "Blind a Team",
                200,
                duration = 1.m,
                secret = true,
                targetType = ActionTarget.TEAM.id,
            )
        }

        val MINING_FATIGUE by register("slow_minespeed", MiningFatigue) {
            Reward(
                "Give a Team Mining-Fatigue",
                300,
                duration = 4.m,
                secret = true,
                targetType = ActionTarget.TEAM.id,
            )
        }

        val SCARE_PLAYER by register("scare_player", ScarePlayer) {
            Reward(
                "Scares a player by playing sounds near him",
                100,
                duration = 1.m,
                secret = true,
                targetType = ActionTarget.PLAYER.id,
            )
        }

        fun <T> buy(ctx: RewardContext<T>): Boolean {
            return ctx.player?.let { player ->
                ctx.targetType.validate(ctx.target, player)
                with(ctx.reward) {
                    Points.modify(ctx.server, ctx.team, -price) { pointsNow ->

                        Action.run(ctx, duration)

                        LOGGER.log(
                            ctx.server, Event(
                                id,
                                EventTarget.of(player),
                                ctx.targetEvent(),
                                price,
                                pointsNow,
                            )
                        )

                        player.teammates().forEach {
                            Chat.message(
                                it, Component.literal("Bought ${ctx.reward.display} for ").append(
                                    Component.literal("${ctx.reward.price}").withStyle(
                                        ChatFormatting.LIGHT_PURPLE
                                    )
                                )
                            )
                        }

                    }
                }
            } ?: throw Action.NOT_ONLINE.create()
        }

    }

    @Suppress("UNCHECKED_CAST")
    val action: Action
        get() = (ACTIONS[id] ?: throw NullPointerException("Action for $id missing"))

}

