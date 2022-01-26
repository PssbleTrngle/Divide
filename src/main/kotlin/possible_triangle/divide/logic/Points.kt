package possible_triangle.divide.logic

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.PerTeamIntData
import possible_triangle.divide.logging.EventLogger

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object Points {

    @Serializable
    private data class Event(val type: String, val team: EventTarget, val before: Int, val now: Int)

    private val LOGGER = EventLogger("points", { Event.serializer() }) { inTeam { it.team } }

    private val CASH = PerTeamIntData("cash", Config.CONFIG.starterCash)
    private val TOTAL = PerTeamIntData("total_cash", Config.CONFIG.starterCash)

    fun getTotal(server: MinecraftServer, team: Team): Int {
        return TOTAL[server][team]
    }

    fun get(server: MinecraftServer, team: Team): Int {
        return CASH[server][team]
    }

    fun modify(
        server: MinecraftServer,
        team: PlayerTeam,
        amount: Int,
        runnable: (pointsAfter: Int) -> Unit = {}
    ): Boolean {
        if (amount == 0) return true
        val current = get(server, team)
        return if (current + amount >= 0) {
            runnable(current + amount)
            val teamTarget = EventTarget.of(team)

            if (amount > 0) {
                val total = TOTAL[server][team]
                LOGGER.log(server, Event("total", teamTarget, before = total, now = total + amount))
                TOTAL[server][team] += amount
            }

            LOGGER.log(server, Event("current", teamTarget, before = current, now = current + amount))
            CASH[server][team] = current + amount

            true
        } else
            false
    }

    fun set(server: MinecraftServer, team: PlayerTeam, amount: Int) {
        if (amount < 0) throw IllegalArgumentException("Amount must be >= 0")
        val total = TOTAL[server][team]

        val teamTarget = EventTarget.of(team)

        LOGGER.log(server, Event("current", teamTarget, before = CASH[server][team], now = amount))

        CASH[server][team] = amount
        if (amount > total) {
            TOTAL[server][team] = amount

            LOGGER.log(server, Event("total", teamTarget, before = total, now = amount))
        }
    }

    fun reset(server: MinecraftServer, team: Team) {
        CASH[server][team] = Config.CONFIG.starterCash
        TOTAL[server][team] = Config.CONFIG.starterCash
    }

}