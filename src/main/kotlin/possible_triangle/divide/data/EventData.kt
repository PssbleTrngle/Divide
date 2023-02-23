package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKey
import net.minecraft.scoreboard.Team
import net.minecraft.text.LiteralTextContent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
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
        fun of(player: PlayerEntity): EventTarget {
            return EventTarget(
                name = player.entityName,
                uuid = player.uuidAsString,
                team = player.participantTeam()?.let { of(it) }
            )
        }

        fun of(team: Team): EventTarget {
            val name = team.displayName.content
            val color = team.color.takeIf { it.isColor }?.colorValue
            return EventTarget(if (name is LiteralTextContent) name.string else team.name, id = team.name, color = color)
        }

        fun optional(player: PlayerEntity?): EventTarget? {
            return if (player == null) null else of(player)
        }
    }
}

@Serializable
data class EventPos(val x: Int, val y: Int, val z: Int, val dimension: String) {
    companion object {
        fun of(pos: BlockPos, dimension: RegistryKey<World>? = null): EventPos {
            return EventPos(pos.x, pos.y, pos.z, dimension?.value?.path ?: "overworld")
        }
    }
}