package possible_triangle.divide.missions

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.Teams

@Serializable
data class Mission(val description: String, val fine: Int, val time: Int) {

    @Transient
    lateinit var id: String
        private set

    companion object : DefaultedResource<Mission>("missions", { Mission.serializer() }) {

        val FIND_DIAMOND by defaulted("find_diamond") { Mission("Mine a diamond ore", 100, 10) }
        val KILL_PLAYER by defaulted("kill_player") { Mission("Kill another player", 100, 10) }
        val DROWN by defaulted("drown") { Mission("Drown", 100, 10) }
        val EXPLODE by defaulted("explode") { Mission("Explode", 100, 10) }
        val EAT_CARROT by defaulted("eat_carrot") { Mission("eat a carrot", 100, 10) }

        override fun populate(entry: Mission, server: MinecraftServer?, id: String) {
            entry.id = id
        }

    }

    fun fulfill(by: ServerPlayer) {
        MissionEvent.fulfill(by.server, Teams.teamOf(by) ?: return, this)
    }

}
