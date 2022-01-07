package possible_triangle.divide.logic

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.scores.Team
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object CashLogic {

    private val CASH = PerTeamData("cash", Config.CONFIG.starterCash)
    private val TOTAL = PerTeamData("total_cash", Config.CONFIG.starterCash)

    fun getTotal(server: MinecraftServer, team: Team): Int {
        return TOTAL[server][team]
    }

    fun get(server: MinecraftServer, team: Team): Int {
        return CASH[server][team]
    }

    fun modify(server: MinecraftServer, team: Team, amount: Int): Boolean {
        if (amount == 0) return true
        val current = get(server, team)
        return if (current + amount >= 0) {
            if(amount > 0) TOTAL[server][team] += amount
            CASH[server][team] = current + amount
            true
        } else
            false
    }

    fun set(server: MinecraftServer, team: Team, amount: Int) {
        if (amount < 0) throw IllegalArgumentException("Amount must be >= 0")
        CASH[server][team] = amount
        if(amount > TOTAL[server][team]) {
            TOTAL[server][team] = amount
        }
    }

}