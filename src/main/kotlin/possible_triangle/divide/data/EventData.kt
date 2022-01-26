package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.logic.Teams

@Serializable
data class EventTarget(
    val name: String,
    val uuid: String? = null,
    val team: EventTarget? = null,
    val id: String? = null,
    val color: Int? = null,
) {
    companion object {
        fun of(player: Player): EventTarget {
            return EventTarget(
                name = player.scoreboardName,
                uuid = player.stringUUID,
                team = Teams.teamOf(player)?.let { of(it) }
            )
        }

        fun of(team: PlayerTeam): EventTarget {
            val name = team.displayName
            val color = team.color.takeIf { it.isColor }?.color
            return EventTarget(if (name is TextComponent) name.text else team.name, id = team.name, color = color)
        }

        fun optional(player: Player?): EventTarget? {
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