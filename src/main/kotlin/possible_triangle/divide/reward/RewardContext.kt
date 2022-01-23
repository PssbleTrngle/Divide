package possible_triangle.divide.reward

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.data.EventPlayer
import java.util.*

data class RewardContext<Raw, Target>(
    val team: PlayerTeam,
    val server: MinecraftServer,
    internal val rawPlayer: UUID,
    internal val rawTarget: Raw,
    val reward: Reward
) {

    val target
        get() = action.target.fetch(rawTarget, server)

    val player
        get() = server.playerList.getPlayer(rawPlayer)

    fun <Out> ifComplete(consumer: RewardContext<Raw, Target>.(ServerPlayer, Target) -> Out): Out? {
        val target = target
        val player = player
        return if(target != null && player != null)  consumer(player, target)
        else null
    }

    fun targetEvent(): EventPlayer? {
        return target?.let { action.target.toEvent(it) }
    }

    fun tick() {
        action.tick(this)
    }

    fun start() {
        action.start(this)
    }

    fun prepare() {
        action.prepare(this)
    }

    fun stop() {
        action.stop(this)
    }

    val action
        get() = reward.action as Action<Raw, Target>

}