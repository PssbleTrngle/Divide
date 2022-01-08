package possible_triangle.divide.crates.loot

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import kotlin.random.Random

@Serializable
data class LootPools(val rolls: Int, val entries: List<LootEntry>) {

    fun populate(server: MinecraftServer) {
        entries.forEach { it.populate(server) }
    }

    fun generate(): List<ItemStack> {
        val rolls = Random.nextInt(this.rolls - 1, this.rolls + 2)
        if (rolls == 0) return listOf()

        val weighted = arrayListOf<ItemStack>()

        entries.forEach { loot ->
            repeat(loot.weight) {
                weighted.add(loot.createStack())
            }
        }

        return weighted.shuffled().take(rolls)
    }

}
