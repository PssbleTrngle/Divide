package possible_triangle.divide.reward.actions

import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

object TeamBuff : BaseBuff() {

    override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayerEntity> {
        return ctx.team.participants(ctx.server)
    }

    fun isBuffed(server: MinecraftServer, team: Team, reward: Reward): Boolean {
        return isRunning(server, reward) { ctx ->
            val action = reward.action
            if (action !is TeamBuff) false
            else ctx.targetTeam() == team
        }
    }

}