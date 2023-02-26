package possible_triangle.divide.command

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.GameData
import possible_triangle.divide.logic.Teams.isAdmin
import possible_triangle.divide.logic.Teams.isParticipant
import possible_triangle.divide.logic.Teams.participantTeam

fun CommandSourceStack.isAdmin(): Boolean {
    val entity = entity
    if (hasPermission(2)) return true
    return if (entity is ServerPlayer) {
        entity.isAdmin()
    } else {
        false
    }
}

fun CommandSourceStack.optionalPlayer(): ServerPlayer? {
    return if (entity is ServerPlayer) playerOrException else null
}

fun CommandSourceStack.optionalTeam(): PlayerTeam? {
    return player?.participantTeam()
}

fun CommandSourceStack.isParticipant(): Boolean {
    return playerOrException.isParticipant()
}

fun CommandSourceStack.isActiveParticipant(): Boolean {
    val data = GameData.DATA[server]
    return isParticipant() && data.started && !data.paused
}
