package possible_triangle.divide.data

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.DivideMod
import java.io.File
import kotlin.reflect.KProperty

abstract class DefaultedResource<Entry>(
    dir: String,
    private val serializer: () -> KSerializer<Entry>
) :
    ReloadedResource<Entry, Entry>(dir, serializer) {

    private val defaults = hashMapOf<String, () -> Entry>()

    fun defaulted(id: String, supplier: () -> Entry): Delegate {
        defaults[id.lowercase()] = supplier
        return Delegate(id.lowercase(), supplier)
    }

    final override fun map(raw: Entry, server: MinecraftServer): Entry {
        return raw
    }

    fun save(id: String, entry: Entry) {
        val encoded = Yaml.default.encodeToString(serializer(), entry)
        val file = File(folder, "$id.yml")
        if (!file.exists()) file.createNewFile()
        val writer = file.writer()
        writer.write(encoded)
        writer.close()
    }

    override fun onError(id: String): Entry? {
        val default = defaults[id] ?: return null
        save(id, default())
        return default()
    }

    override fun afterLoad(server: MinecraftServer) {
        defaults
            .filterNot { values.containsKey(it.key) }
            .forEach { (id, entry) -> save(id, entry()) }
    }

    inner class Delegate(private val id: String, private val supplier: () -> Entry) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Entry {
            DivideMod.LOGGER.info("Delegated ${property.name}")
            return values[id] ?: supplier()
        }
    }

}