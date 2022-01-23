package possible_triangle.divide.actions

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

object TeamBuff : BaseBuff() {

    override fun buffs(ctx: RewardContext): List<ServerPlayer> {
        return Teams.teammates(ctx.player)
    }

    fun isBuffed(server: MinecraftServer, team: Team, reward: Reward): Boolean {
        return reward.action is TeamBuff && Teams.players(server, team).any { isBuffed(it, reward) }
    }

}