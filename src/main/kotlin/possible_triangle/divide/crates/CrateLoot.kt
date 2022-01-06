package possible_triangle.divide.crates

import kotlinx.serialization.Serializable
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.ItemLike
import possible_triangle.divide.data.ReloadedResource
import kotlin.random.Random

object CrateLoot : ReloadedResource<CrateLoot.Raw, CrateLoot.Entry>("crate_loot", { Raw.serializer() }) {

    @Serializable
    data class Raw(val item: String, val weight: Int, val amount: List<Int>?)
    data class Entry(val item: ItemLike, val weight: Int, val amount: () -> Int)

    override fun map(raw: Raw, server: MinecraftServer): Entry {
        val items = server.registryAccess().registryOrThrow(Registry.ITEM_REGISTRY)
        val item =
            items[ResourceLocation(raw.item)] ?: throw IllegalArgumentException("Item ${raw.item} does not exists")
        return Entry(item, raw.weight) {
            if (raw.amount == null || raw.amount.isEmpty()) 1
            else if (raw.amount.size == 1) raw.amount.first()
            else {
                val (min, max) = raw.amount
                Random.nextInt(min, max)
            }
        }
    }
}