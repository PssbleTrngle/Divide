package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.scores.Team
import possible_triangle.divide.logic.CashLogic

@Serializable
enum class Reward(val display: String, val price: Int) {

    TRACK_PLAYER("Track Player", 1000),
    PEACE_TIME("Peace Time", 500);

    fun buy(world: ServerLevel, team: Team): Boolean {
        return CashLogic.modify(world, team, -price)
    }

}

