package possible_triangle.divide.logic

import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.Team
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.PerTeamIntData

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object Points {

    private val CASH = PerTeamIntData("cash", Config.CONFIG.starterCash)
    private val TOTAL = PerTeamIntData("total_cash", Config.CONFIG.starterCash)

    fun getTotal(server: MinecraftServer, team: Team): Int {
        return TOTAL[server][team]
    }

    fun get(server: MinecraftServer, team: Team): Int {
        return CASH[server][team]
    }

    fun modify(server: MinecraftServer, team: Team, amount: Int, runnable: (pointsAfter: Int) -> Unit = {}): Boolean {
        if (amount == 0) return true
        val current = get(server, team)
        return if (current + amount >= 0) {
            runnable(current + amount)
            if (amount > 0) TOTAL[server][team] += amount
            CASH[server][team] = current + amount
            true
        } else
            false
    }

    fun set(server: MinecraftServer, team: Team, amount: Int) {
        if (amount < 0) throw IllegalArgumentException("Amount must be >= 0")
        CASH[server][team] = amount
        if (amount > TOTAL[server][team]) {
            TOTAL[server][team] = amount
        }
    }

    fun reset(server: MinecraftServer, team: Team) {
        CASH[server][team] = Config.CONFIG.starterCash
        TOTAL[server][team] = Config.CONFIG.starterCash
    }

}