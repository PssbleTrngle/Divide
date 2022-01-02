package possible_triangle.divide.logic

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.scores.Team
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object CashLogic {

    private const val MIN = 0
    private val CASH = PerTeamData("cash", MIN)

    fun get(world: ServerLevel, team: Team): Int {
        return CASH.get(world)[team]
    }

    fun modify(world: ServerLevel, team: Team, amount: Int): Boolean {
        if (amount == 0) return true
        val current = get(world, team)
        return if (current + amount >= 0) {
            CASH.get(world)[team] = current + amount
            true
        } else
            false
    }

    fun set(world: ServerLevel, team: Team, amount: Int) {
        if (amount < 0) throw IllegalArgumentException("Amount must be >= 0")
        CASH.get(world)[team] = amount
    }

}