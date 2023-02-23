package possible_triangle.divide.reward

import kotlinx.serialization.Serializable
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logging.EventLogger
import java.util.*

data class RewardContext<Target>(
    val team: Team,
    val server: MinecraftServer,
    internal val rawPlayer: UUID,
    internal val target: Target,
    val reward: Reward,
    val targetType: ActionTarget<Target>,
) {

    companion object {
        @Serializable
        private data class Event(
            val action: String,
            val reward: String,
            val boughtBy: EventTarget?,
            val target: EventTarget? = null,
        )

        private val LOGGER = EventLogger("action", { Event.serializer() }) { inTeam { it.boughtBy?.team } }
    }

    val player
        get() = server.playerManager.getPlayer(rawPlayer)

    fun targetEvent(): EventTarget? {
        return target?.let { targetType.toEvent(it, server) }
    }

    fun targetPlayers(): List<ServerPlayerEntity> {
        return targetType.players(this)
    }

    fun targetTeam(): Team? {
        return targetType.team(this)
    }

    fun targetPlayer(): ServerPlayerEntity? {
        return targetPlayers().randomOrNull()
    }

    fun tick() {
        reward.action.tick(this)
    }

    fun start() {
        LOGGER.log(
            server, Event(
                "started",
                reward.id,
                EventTarget.optional(player),
                targetEvent()
            )
        )
        reward.action.start(this)
    }

    fun prepare() {
        if (reward.charge != null) LOGGER.log(
            server, Event(
                "preparing",
                reward.id,
                EventTarget.optional(player),
                targetEvent()
            )
        )
        reward.action.prepare(this)
    }

    fun stop() {
        LOGGER.log(
            server, Event(
                "ended",
                reward.id,
                EventTarget.optional(player),
                targetEvent()
            )
        )
        reward.action.stop(this)
    }

}