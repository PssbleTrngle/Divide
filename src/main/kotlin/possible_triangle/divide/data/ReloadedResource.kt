package possible_triangle.divide.data

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.io.File
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.attribute.BasicFileAttributes


abstract class ReloadedResource<Entry>(
    protected val dir: String,
    protected val serializer: () -> KSerializer<Entry>
) {

    open fun config(): YamlConfiguration {
        return CONFIG
    }

    @Mod.EventBusSubscriber
    companion object {

        val CONFIG = YamlConfiguration(encodeDefaults = false)

        private val WATCHERS = arrayListOf<Pair<ReloadedResource<*>, WatchService>>()
        private var IS_LOADING = false

        @SubscribeEvent
        fun onTick(event: TickEvent.WorldTickEvent) {
            val server = event.world.server ?: return

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

        fun register(resource: ReloadedResource<*>) {
            FORGE_BUS.addListener(resource::setup)
            DivideMod.LOGGER.info("registered resource ${resource.dir}")
        }
    }

    fun idOf(entry: Entry): String {
        return registry.entries.find { it.value == entry }?.key ?: throw NullPointerException("ID missing for $dir")
    }

    operator fun get(id: String): Entry? {
        return registry[id]
    }

    fun getOrThrow(id: String): Entry {
        return registry[id] ?: throw  NullPointerException("$dir with id $id missing")
    }

    protected val folder
        get() = File("config/divide/$dir")

    open fun populate(entry: Entry, server: MinecraftServer) {}

    private fun registerRecursive(root: Path, watchService: WatchService) {
        root.toFile().mkdirs()
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
                return FileVisitResult.CONTINUE
            }
        })
    }

    @SubscribeEvent
    fun setup(event: ServerAboutToStartEvent) {
        load(event.server)
        val watcher = FileSystems.getDefault().newWatchService()
        registerRecursive(folder.toPath(), watcher)
        WATCHERS.add(this to watcher)
    }

    protected var registry = mutableMapOf<String, Entry>()

    val keys
        get() = registry.keys.toList()

    val values
        get() = registry.values.toList()

    val entries
        get() = registry.toMap()

    open fun afterLoad(server: MinecraftServer) {}

    open fun onError(id: String): Entry? {
        return null
    }

    private fun load(server: MinecraftServer) {
        IS_LOADING = true

        val children = folder.list { _, name -> name.endsWith(".yml") } ?: return
        val serializer = serializer()

        val raw = children.associate {
            val stream = File(folder.path, it).inputStream()
            val id = File(it).nameWithoutExtension.lowercase()

            val parsed = try {
                Yaml(configuration = CONFIG).decodeFromStream(serializer, stream)
            } catch (e: SerializationException) {
                DivideMod.LOGGER.warn("an error occurred loading $dir '$id'")
                null
            }

            id to parsed
        }

        registry = raw
            .mapValues { (id, value) -> value ?: onError(id) }
            .mapValues { (_, value) ->
                if (value != null) populate(value, server)
                value
            }
            .filterValues { it != null }
            .mapValues { it.value as Entry }
            .toMutableMap()

        afterLoad(server)

        DivideMod.LOGGER.info("Reloaded $dir with ${registry.size} values")

        IS_LOADING = false
    }

}