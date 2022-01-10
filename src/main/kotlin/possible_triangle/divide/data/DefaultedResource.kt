package possible_triangle.divide.data

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.KSerializer
import net.minecraft.server.MinecraftServer
import java.io.File
import kotlin.reflect.KProperty

abstract class DefaultedResource<Entry>(
    dir: String,
    serializer: () -> KSerializer<Entry>
) :
    ReloadedResource<Entry>(dir, serializer) {

    private val defaults = hashMapOf<String, () -> Entry>()

    fun defaulted(id: String, supplier: () -> Entry): Delegate {
        val lower = id.lowercase()
        if (defaults.containsKey(lower)) throw IllegalArgumentException("Duplicate ID $lower for $dir")
        defaults[lower] = supplier
        registry[lower] = supplier()
        return Delegate(lower, supplier)
    }

    private fun save(id: String, entry: Entry) {
        val encoded = Yaml(configuration = config()).encodeToString(serializer(), entry)
        val file = File(folder, "$id.yml")
        if (!file.exists()) file.createNewFile()
        val writer = file.writer()
        writer.write(encoded)
        writer.close()
    }

    final override fun onError(id: String): Entry? {
        val default = defaults[id] ?: return null
        save(id, default())
        return default()
    }

    final override fun afterLoad(server: MinecraftServer) {
        defaults
            .filterNot { registry.containsKey(it.key) }
            .forEach { (id, entry) -> save(id, entry()) }
    }

    inner class Delegate(private val id: String, private val supplier: () -> Entry) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Entry {
            return registry[id] ?: supplier()
        }
    }

}