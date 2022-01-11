package possible_triangle.divide.crates.loot

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import possible_triangle.divide.logic.makeWeightedDecision
import kotlin.random.Random

@Serializable
data class LootPools(val rolls: Int, val entries: List<LootEntry>, val functions: List<LootFunction>? = null) {

    fun populate(server: MinecraftServer) {
        entries.forEach { it.populate(server) }
    }

    fun generate(): List<ItemStack> {
        val rolls = Random.nextInt(this.rolls - 1, this.rolls + 2)
        if (rolls == 0) return listOf()

        return makeWeightedDecision(rolls, entries.associateWith { it.weight })
            .map { it.createStack() }.onEach { stack -> functions?.forEach { it.apply(stack) } }
    }

}
