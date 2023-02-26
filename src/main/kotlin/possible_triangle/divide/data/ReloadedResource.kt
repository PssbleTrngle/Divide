package possible_triangle.divide.data

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.DivideMod
import possible_triangle.divide.extensions.time
import java.io.File
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.attribute.BasicFileAttributes


abstract class ReloadedResource<Entry>(
    val dir: String,
    val serializer: () -> KSerializer<Entry>,
    val resourceID: String = dir,
) {

    protected fun key(id: String): String {
        return id.lowercase().replace("\\s+".toRegex(), "_")
    }

    open fun config(): YamlConfiguration {
        return CONFIG
    }

    companion object {

        val CONFIG = YamlConfiguration(encodeDefaults = false)

        private val WATCHERS = arrayListOf<Pair<ReloadedResource<*>, WatchService>>()
        private var IS_LOADING = false

        fun tickWatchers(server: MinecraftServer) {
            if (server.time() % 10 != 0L) return

            WATCHERS.removeIf { (resource, watcher) ->
                val key = watcher.poll() ?: return@removeIf false
                if (key.pollEvents().isNotEmpty() && !IS_LOADING) {
                    DivideMod.LOGGER.info("Detected chances for ${resource.dir}")
                    resource.load(server)
                }

                if (!key.reset()) {
                    DivideMod.LOGGER.warn("Closing ${resource.dir}")
                    key.cancel()
                    watcher.close()
                    true
                } else {
                    false
                }
            }
        }

        private val RESOURCES = hashMapOf<String, ReloadedResource<*>>()

        operator fun get(id: String): ReloadedResource<*>? {
            return RESOURCES[id]
        }

        val values
            get() = RESOURCES.values.toList()

        fun register(resource: ReloadedResource<*>) {
            ServerLifecycleEvents.SERVER_STARTING.register(resource::setup)
            RESOURCES[resource.resourceID] = resource
            resource.preLoad()
        }
    }

    fun idOf(entry: Entry): String {
        return registry.entries.find { it.value == entry }?.key
            ?: throw NullPointerException("Key missing for $resourceID")
    }

    operator fun get(id: String): Entry? {
        return registry[id]
    }

    fun getOrThrow(id: String): Entry {
        return registry[id] ?: throw NullPointerException("$resourceID with id $resourceID missing")
    }

    protected val folder
        get() = File("config/divide/$dir")

    open fun populate(entry: Entry, server: MinecraftServer?, id: String) {}

    private fun registerRecursive(root: Path, watchService: WatchService) {
        root.toFile().mkdirs()
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
                return FileVisitResult.CONTINUE
            }
        })
    }

    fun setup(server: MinecraftServer) {
        load(server)
        val watcher = FileSystems.getDefault().newWatchService()
        registerRecursive(folder.toPath(), watcher)
        WATCHERS.add(this to watcher)
    }

    protected var registry = mutableMapOf<String, Entry>()

    private var preloadedKeys = listOf<String>()

    val keys
        get() = (registry.keys.toList() + preloadedKeys).distinct()

    val values
        get() = registry.values.toList()

    val entries
        get() = registry.toMap()

    open fun isVisible(entry: Entry, team: PlayerTeam?, server: MinecraftServer): Boolean {
        return true
    }

    open fun afterLoad(server: MinecraftServer) {}

    open fun onError(id: String): Entry? {
        return null
    }

    private fun files(): Map<String, File> {
        folder.mkdirs()
        val children = folder.list { _, name -> name.endsWith(".yml") } ?: return mapOf()
        return children.associate {
            val file = File(folder.path, it)
            File(it).nameWithoutExtension.lowercase() to file
        }
    }

    private fun preLoad() {
        preloadedKeys = files().keys.toList()
    }

    private fun load(server: MinecraftServer) {
        IS_LOADING = true

        val serializer = serializer()

        val raw = files().mapValues { (id, file) ->
            val stream = file.inputStream()

            val parsed = try {
                Yaml(configuration = CONFIG).decodeFromStream(serializer, stream)
            } catch (e: SerializationException) {
                DivideMod.LOGGER.warn("an error occurred loading $resourceID '$id'")
                null
            }

            parsed
        }

        preloadedKeys = listOf()
        registry = raw
            .mapValues { (id, value) -> value ?: onError(id) }
            .mapValues { (id, value) ->
                if (value != null) populate(value, server, id)
                value
            }
            .filterValues { it != null }
            .mapValues { it.value as Entry }
            .toMutableMap()

        afterLoad(server)

        DivideMod.LOGGER.info("Reloaded $resourceID with ${registry.size} values")

        IS_LOADING = false
    }

}