package possible_triangle.divide.reward.actions

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

object TeamBuff : BaseBuff() {

    override fun <T> buffs(ctx: RewardContext<T>): List<ServerPlayer> {
        return Teams.players(ctx.server, ctx.team)
    }

    fun isBuffed(server: MinecraftServer, team: Team, reward: Reward): Boolean {
        return isRunning(server, reward) { ctx ->
            val action = reward.action
            if (action !is TeamBuff) false
            else ctx.targetTeam() == team
        }
    }

}