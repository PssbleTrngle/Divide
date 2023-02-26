package possible_triangle.divide.data

import kotlinx.serialization.KSerializer
import net.minecraft.server.MinecraftServer
import java.io.File
import kotlin.reflect.KProperty

abstract class DefaultedResource<Entry>(
    dir: String,
    serializer: () -> KSerializer<Entry>,
    id: String = dir,
) :
    ReloadedResource<Entry>(dir, serializer, id) {

    private val defaultsMap = hashMapOf<String, () -> Entry>()

    val defaults
        get() = defaultsMap.toMap()

    fun defaulted(id: String, supplier: () -> Entry): Delegate {
        val lower = key(id)
        if (defaultsMap.containsKey(lower)) throw IllegalArgumentException("Duplicate ID $lower for $dir")
        defaultsMap[lower] = supplier
        with(supplier()) {
            populate(this, null, lower)
            registry[lower] = this
            save(lower, this, false)
        }

        return Delegate(lower, supplier)
    }

    fun save(id: String, entry: Entry, overwrite: Boolean = true) {
        val encoded = createYaml().encodeToString(serializer(), entry)
        val file = File(folder, "$id.yml")
        folder.mkdirs()
        if (!file.exists()) file.createNewFile()
        else if (!overwrite) return
        val writer = file.writer()
        writer.write(encoded)
        writer.close()
    }

    final override fun onError(id: String): Entry? {
        val default = defaultsMap[id] ?: return null
        save(id, default())
        return default()
    }

    final override fun afterLoad(server: MinecraftServer) {
        defaultsMap
            .filterNot { registry.containsKey(it.key) }
            .forEach { (id, entry) -> save(id, entry()) }
    }

    inner class Delegate(private val id: String, private val supplier: () -> Entry) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>?): Entry {
            return registry[id] ?: supplier()
        }
    }

}