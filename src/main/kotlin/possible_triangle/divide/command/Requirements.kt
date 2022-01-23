package possible_triangle.divide.command

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.GameData
import possible_triangle.divide.logic.Teams

object Requirements {

    fun isAdmin(source: CommandSourceStack): Boolean {
        val entity = source.entity
        if (source.hasPermission(2)) return true
        return if (entity is ServerPlayer) {
            Teams.isAdmin(entity)
        } else {
            false
        }
    }

    fun optionalPlayer(source: CommandSourceStack): ServerPlayer? {
        return if (source.entity is ServerPlayer) source.playerOrException else null
    }

    fun optionalTeam(source: CommandSourceStack): PlayerTeam? {
        return optionalPlayer(source)?.let { Teams.teamOf(it) }
    }

    fun isPlayer(source: CommandSourceStack): Boolean {
        return Teams.isPlayer(source.playerOrException)
    }

    fun isPlayerInGame(source: CommandSourceStack): Boolean {
        val data = GameData.DATA[source.server]
        return isPlayer(source) && data.started && !data.paused
    }

}
