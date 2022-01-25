package possible_triangle.divide.logging

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logic.Teams
import java.io.File

@Serializable
data class LoggedEvent<T>(val gameTime: Long, val realTime: Long, val type: String, val event: T)

class EventPredicate<T> internal constructor() {
    private val predicates = mutableListOf<(ServerPlayer?, T) -> Boolean>()
    fun isPlayer(player: (T) -> EventTarget) {
        predicates.add { p, e -> p?.let { it.stringUUID == player(e).uuid } ?: false }
    }

    fun inTeam(team: (T) -> EventTarget?) {
        predicates.add { p, e -> p?.let { Teams.teamOf(it) }?.let { it.name == team(e)?.id } ?: false }
    }

    fun isAdmin() {
        predicates.add { p, _ -> p != null && Teams.isAdmin(p) }
    }

    fun always() {
        predicates.add { _, _ -> true }
    }

    fun build(): (T, ServerPlayer?) -> Boolean {
        val copied = predicates.toList()
        return { event: T, player: ServerPlayer? ->
            copied.all { it(player, event) }
        }
    }
}

class EventLogger<T>(
    private val name: String,
    private val serializer: () -> KSerializer<T>,
    private val predicateBuilder: EventPredicate<T>.() -> Unit
) {

    val visibleTo = EventPredicate<T>().let {
        predicateBuilder(it)
        it.build()
    }

    private val events = arrayListOf<LoggedEvent<T>>()

    companion object {
        private val LOGGERS = arrayListOf<EventLogger<*>>()

        private val JSON = Json {
            encodeDefaults = false
        }

        private fun logFile(server: MinecraftServer, name: String): File {
            val logs = File(server.getWorldPath(LevelResource.ROOT).toFile(), "logs")
            logs.mkdirs()
            val logFile = File(logs, "$name.log")
            if (!logFile.exists()) logFile.createNewFile()
            return logFile
        }

        fun lines(player: ServerPlayer?): List<String> {
            return LOGGERS.map { it.lines(player) }
                .flatten()
                .sortedBy { it.first }
                .map { it.second }
        }
    }

    private fun lines(player: ServerPlayer?): List<Pair<Long, String>> {
        val serializer = LoggedEvent.serializer(serializer())
        return events.filter { visibleTo(it.event, player) }
            .map { it.realTime to JSON.encodeToString(serializer, it) }
    }

    init {
        LOGGERS.add(this)
    }

    fun log(server: MinecraftServer, event: T) {
        val line = LoggedEvent(server.overworld().gameTime, System.currentTimeMillis(), name, event)

        events.add(line)

        listOf(name, "full").forEach {
            val logFile = logFile(server, it)
            logFile.appendText(JSON.encodeToString(LoggedEvent.serializer(serializer()), line) + "\n")
        }
    }

}