package possible_triangle.divide.logging

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.io.File

@Serializable
data class LoggedEvent<T>(val gameTime: Long, val realTime: Long, val type: String, val event: T)

data class LinePair<T>(val event: LoggedEvent<T>, val serializer: KSerializer<T>)

class EventLogger<T>(private val name: String, private val serializer: () -> KSerializer<T>) {

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

        fun lines(server: MinecraftServer): List<String> {
            return logFile(server, "full").readLines()
        }
    }

    init {
        LOGGERS.add(this)
    }

    fun log(server: MinecraftServer, event: T) {
        val line = LoggedEvent(server.overworld().gameTime, System.currentTimeMillis(), name, event)

        listOf(name, "full").forEach {
            val logFile = logFile(server, it)

            logFile.appendText(JSON.encodeToString(LoggedEvent.serializer(serializer()), line) + "\n")
        }
    }

}