package possible_triangle.divide.logging

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.extensions.time
import possible_triangle.divide.logic.Teams.isAdmin
import possible_triangle.divide.logic.Teams.participantTeam
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Serializable
data class LoggedEvent<T>(
    val gameTime: Long,
    val realTime: Long,
    val type: String,
    val event: T,
    val id: String,
)

class EventPredicate<T> internal constructor() {
    private val predicates = mutableListOf<(ServerPlayer?, T, Boolean) -> Boolean>()

    private fun ifRequired(predicate: (ServerPlayer?, T) -> Boolean) {
        predicates.add { p, e, i -> i || predicate(p, e) }
    }

    fun isPlayer(player: (T) -> EventTarget) {
        ifRequired { p, e -> p?.let { it.stringUUID == player(e).uuid } ?: false }
    }

    fun inTeam(team: (T) -> EventTarget?) {
        ifRequired { p, e -> p?.participantTeam()?.let { it.name == team(e)?.id } ?: false }
    }

    fun isAdmin() {
        predicates.add { p, _, i -> i && p != null && p.isAdmin() }
    }

    fun always() {
        predicates.add { _, _, _ -> true }
    }

    fun build(): (T, ServerPlayer?, Boolean) -> Boolean {
        val copied = predicates.toList()
        return { event: T, player: ServerPlayer?, ignorePermission: Boolean ->
            copied.all { it(player, event, ignorePermission) }
        }
    }
}

class EventLogger<T>(
    private val name: String,
    private val serializer: () -> KSerializer<T>,
    private val predicateBuilder: EventPredicate<T>.() -> Unit,
) {

    val visibleTo = EventPredicate<T>().let {
        predicateBuilder(it)
        it.build()
    }

    companion object {
        private val LOGGERS = arrayListOf<EventLogger<*>>()

        private val JSON = Json {
            encodeDefaults = false
        }

        private fun logFile(server: MinecraftServer, name: String, createIfMissing: Boolean = true): File {
            val logs = File(server.getWorldPath(LevelResource.ROOT).toFile(), "logs")
            logs.mkdirs()
            val logFile = File(logs, "$name.log")
            if (createIfMissing && !logFile.exists()) logFile.createNewFile()
            return logFile
        }

        fun archive(server: MinecraftServer): Int {
            val names = LOGGERS.map { it.name } + "full"
            val files = names.distinct()
                .map { logFile(server, it, createIfMissing = false) }
                .filter { it.exists() && it.length() > 0 }

            if (files.isEmpty()) return 0

            val timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSSSS")
                .withZone(ZoneOffset.UTC).format(Instant.now())

            val archive = File(files.first().parentFile, "$timestamp.zip")
            ZipOutputStream(archive.outputStream().buffered()).use { output ->
                files.forEach { file ->
                    file.inputStream().buffered().use { origin ->
                        val entry = ZipEntry(file.name)
                        output.putNextEntry(entry)
                        origin.copyTo(output, 1024)
                    }
                }
            }

            files.forEach {
                it.delete()
            }

            return files.size
        }
    }

    init {
        LOGGERS.add(this)
        DivideMod.LOGGER.info("Registered event logger $name")
    }

    fun log(server: MinecraftServer, event: T) {
        val line = LoggedEvent(
            server.time(),
            System.currentTimeMillis(),
            name,
            event,
            UUID.randomUUID().toString()
        )

        listOf(name, "full").forEach {
            val logFile = logFile(server, it)
            logFile.appendText(JSON.encodeToString(LoggedEvent.serializer(serializer()), line) + "\n")
        }
    }

}