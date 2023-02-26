package possible_triangle.divide.data

import com.charleskorn.kaml.YamlInput
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.ItemLike
import possible_triangle.divide.extensions.id

fun createContext(server: MinecraftServer? = null) = SerializersModule {
    contextual(ItemLike::class, ItemSerializer(server?.registryAccess()))
}

class ItemSerializer(registryAccess: RegistryAccess?) : KSerializer<ItemLike> {
    private val items = registryAccess?.registryOrThrow(Registries.ITEM)

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemLike", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ItemLike {
        if(items == null) throw NullPointerException("Cannot decode items without a RegistryAccess")
        val structure = decoder.beginStructure(descriptor) as YamlInput
        val id = ResourceLocation(structure.decodeString())
        return items.get(id) ?: throw NullPointerException("Unknown item '$id'")
    }

    override fun serialize(encoder: Encoder, value: ItemLike) {
        val id = items?.getKey(value.asItem()) ?: value.id()
        encoder.encodeString(id.toString())
    }
}