package possible_triangle.divide.reward

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

data class RewardContext(
    val team: PlayerTeam,
    val world: ServerLevel,
    val player: ServerPlayer,
    val target: ServerPlayer,
    val reward: Reward
)