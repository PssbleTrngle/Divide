package possible_triangle.divide.command

import net.minecraft.scoreboard.Team
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.GameData
import possible_triangle.divide.logic.Teams.isAdmin
import possible_triangle.divide.logic.Teams.isParticipant
import possible_triangle.divide.logic.Teams.participantTeam

fun ServerCommandSource.isAdmin(): Boolean {
    val entity = entity
    if (hasPermissionLevel(2)) return true
    return if (entity is ServerPlayerEntity) {
        entity.isAdmin()
    } else {
        false
    }
}

fun ServerCommandSource.optionalPlayer(): ServerPlayerEntity? {
    return if (entity is ServerPlayerEntity) playerOrThrow else null
}

fun ServerCommandSource.optionalTeam(): Team? {
    return player?.participantTeam()
}

fun ServerCommandSource.isParticipant(): Boolean {
    return playerOrThrow.isParticipant()
}

fun ServerCommandSource.isActiveParticipant(): Boolean {
    val data = GameData.DATA[server]
    return isParticipant() && data.started && !data.paused
}
