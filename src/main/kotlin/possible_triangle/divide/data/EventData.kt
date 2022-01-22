package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

@Serializable
data class EventPlayer(val name: String, val uuid: String, val team: String? = null) {
    companion object {
        fun of(player: Player): EventPlayer {
            return EventPlayer(
                player.scoreboardName,
                player.stringUUID,
                player.team?.name
            )
        }

        fun optional(player: Player?): EventPlayer? {
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