package possible_triangle.divide.crates

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import possible_triangle.divide.data.DefaultedResource
import kotlin.random.Random

@Serializable
data class CrateLoot(
    @SerialName("item") private val id: String,
    val weight: Int,
    @SerialName("amount") private val amounts: List<Int>?
) {

    @Transient
    lateinit var item: ItemLike

    companion object : DefaultedResource<CrateLoot>("crate_loot", { CrateLoot.serializer() }) {
        init {
            defaulted("diamond") { CrateLoot("diamond", 10, listOf(1, 3)) }
            defaulted("iron_ingot") { CrateLoot("iron_ingot", 30, listOf(2, 6)) }
            defaulted("bread") { CrateLoot("bread", 100, listOf(3, 11)) }
        }

        override fun map(raw: CrateLoot, server: MinecraftServer): CrateLoot {
            val entry = super.map(raw, server)
            val items = server.registryAccess().registryOrThrow(Registry.ITEM_REGISTRY)
            entry.item =
                items[ResourceLocation(entry.id)] ?: throw IllegalArgumentException("Item ${entry.id} does not exists")
            return entry
        }

        fun generate(): List<ItemStack> {
            val amount = Random.nextInt(4, 12)
            val weighted = arrayListOf<ItemStack>()
            values.values.forEach { loot ->
                repeat(loot.weight) {
                    weighted.add(ItemStack(loot.item, loot.amount))
                }
            }
            return weighted.shuffled().take(amount)
        }

    }

    val amount: Int
        get() {
            return if (amounts == null || amounts.isEmpty()) 1
            else if (amounts.size == 1) amounts.first()
            else {
                val (min, max) = amounts
                Random.nextInt(min, max)
            }
        }

}