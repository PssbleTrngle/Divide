package possible_triangle.divide.crates

import kotlinx.serialization.Serializable
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import possible_triangle.divide.data.DefaultedResource
import kotlin.random.Random


@Serializable
data class CrateLoot(val weight: Int, val entries: List<LootEntry>) {

    companion object : DefaultedResource<CrateLoot>("crate_loot", { CrateLoot.serializer() }) {
        init {
            defaulted("common") {
                CrateLoot(
                    10, listOf(
                        LootEntry(Items.DIAMOND, 10, listOf(1, 3)),
                        LootEntry(Items.IRON_INGOT, 30, listOf(2, 6)),
                        LootEntry(Items.BREAD, 100, listOf(3, 11)),
                    )
                )
            }

            defaulted("trash") {
                CrateLoot(
                    1, listOf(
                        LootEntry(Items.COBWEB, 10),
                        LootEntry(Items.ROTTEN_FLESH, 20, listOf(2, 5)),
                        LootEntry(Items.POISONOUS_POTATO, 1, listOf(1, 3)),
                    )
                )
            }
        }

        fun random(): CrateLoot {
            // TODO use weight
            return values.values.random()
        }

        override fun map(raw: CrateLoot, server: MinecraftServer): CrateLoot {
            val list = super.map(raw, server)
            val items = server.registryAccess().registryOrThrow(Registry.ITEM_REGISTRY)
            list.entries.forEach { entry ->
                entry.item =
                    items[ResourceLocation(entry.id)]
                        ?: throw IllegalArgumentException("Item ${entry.id} does not exists")
            }
            return list
        }

    }

    fun generate(): List<ItemStack> {
        val amount = Random.nextInt(4, 12)
        val weighted = arrayListOf<ItemStack>()
        entries.forEach { loot ->
            repeat(loot.weight) {
                weighted.add(ItemStack(loot.item, loot.amount))
            }
        }
        return weighted.shuffled().take(amount)
    }

}