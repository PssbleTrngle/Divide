package possible_triangle.divide.crates.loot

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import possible_triangle.divide.extensions.setLore
import possible_triangle.divide.logic.makeWeightedDecision
import kotlin.random.Random

@Serializable
data class LootPools(val rolls: Int, val entries: List<LootEntry>, val functions: List<LootFunction>? = null) {

    fun populate(server: MinecraftServer) {
        entries.forEach { it.populate(server) }
    }

    fun generate(random: Random): List<ItemStack> {
        val rolls = if (random.nextBoolean())
            random.nextInt(this.rolls - 1, this.rolls + 2)
        else this.rolls

        if (rolls == 0) return emptyList()

        return makeWeightedDecision(rolls, entries.associateWith { it.weight }, random).mapIndexed { i, entry ->
            val stack = entry.createStack()
            functions?.forEach { it.apply(stack) }
            stack.setLore("Roll ${i + 1}/$rolls", "Weight ${entry.weight}")
            stack
        }
    }

}
