package possible_triangle.divide.logic

import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.GameData
import java.util.*

object LoginShield {

    private var players = mapOf<UUID, Int>()

    fun tickLogin() {
        players = players
            .mapValues { it.value - 1 }
            .filterValues { it > 0 }
    }

    fun isProtected(player: ServerPlayerEntity): Boolean {
        return if (GameData.DATA[player.server].paused) true
        else players.containsKey(player.uuid)
    }

}