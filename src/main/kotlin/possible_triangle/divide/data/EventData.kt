package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.contents.LiteralContents
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.logic.Teams.participantTeam

@Serializable
data class EventTarget(
    val name: String,
    val uuid: String? = null,
    val team: EventTarget? = null,
    val id: String? = null,
    val color: Int? = null,
) {
    companion object {
        fun of(player: ServerPlayer): EventTarget {
            return EventTarget(
                name = player.scoreboardName,
                uuid = player.stringUUID,
                team = player.participantTeam()?.let { of(it) }
            )
        }

        fun of(team: PlayerTeam): EventTarget {
            val name = team.displayName.contents
            val color = team.color.takeIf { it.isColor }?.color
            return EventTarget(if (name is LiteralContents) name.text else team.name, id = team.name, color = color)
        }

        fun optional(player: ServerPlayer?): EventTarget? {
            return if (player == null) null else of(player)
        }
    }
}

@Serializable
data class EventPos(val x: Int, val y: Int, val z: Int, val dimension: String) {
    companion object {
        fun of(pos: BlockPos, dimension: ResourceKey<Level>? = null): EventPos {
            return EventPos(pos.x, pos.y, pos.z, dimension?.location()?.path ?: "overworld")
        }
    }
}