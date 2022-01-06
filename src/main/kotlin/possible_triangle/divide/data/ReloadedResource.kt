package possible_triangle.divide.data

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.DeserializationStrategy
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.io.File
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.attribute.BasicFileAttributes


abstract class ReloadedResource<Raw, Entry>(
    private val dir: String,
    private val serializer: () -> DeserializationStrategy<Raw>
) {

    @Mod.EventBusSubscriber
    companion object {
        private val WATCHERS = arrayListOf<Pair<ReloadedResource<*, *>, WatchService>>()

        @SubscribeEvent
        fun onTick(event: TickEvent.WorldTickEvent) {
            val server = event.world.server ?: return

            WATCHERS.removeIf { (resource, watcher) ->
                val key = watcher.poll() ?: return@removeIf false
                if (key.pollEvents().isNotEmpty()) {
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

        fun register(resource: ReloadedResource<*, *>) {
            FORGE_BUS.addListener(resource::setup)
            DivideMod.LOGGER.info("registered resource ${resource.dir}")
        }
    }

    private val folder
        get() = File("config/divide/$dir")

    abstract fun map(raw: Raw, server: MinecraftServer): Entry

    private fun registerRecursive(root: Path, watchService: WatchService) {
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
                return FileVisitResult.CONTINUE
            }
        })
    }

    @SubscribeEvent
    fun setup(event: ServerStartedEvent) {
        load(event.server)
        val watcher = FileSystems.getDefault().newWatchService()
        registerRecursive(folder.toPath(), watcher)
        WATCHERS.add(this to watcher)
    }

    var values: List<Entry> = listOf()
        private set

    private fun load(server: MinecraftServer) {
        val children = folder.list { _, name -> name.endsWith(".yml") } ?: return

        val raw = children.map {
            val stream = File(folder.path, it).inputStream()
            Yaml.default.decodeFromStream(serializer(), stream)
        }

        values = raw.map { map(it, server) }
        DivideMod.LOGGER.info("Reloaded $dir with ${values.size} values")

    }

}