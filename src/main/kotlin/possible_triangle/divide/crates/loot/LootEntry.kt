package possible_triangle.divide.crates.loot

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import kotlin.random.Random

@Serializable
data class LootEntry(
    @Contextual val item: ItemLike,
    val weight: Double = 1.0,
    @SerialName("amount") private val amounts: List<Int>? = null,
    val functions: List<LootFunction>? = null,
) {

    fun createStack(): ItemStack {
        val stack = ItemStack(item, amount)
        functions?.forEach { it.apply(stack) }
        return stack
    }

    val amount: Int
        get() {
            return if (amounts.isNullOrEmpty()) 1
            else if (amounts.size == 1) amounts.first()
            else {
                val (min, max) = amounts
                Random.nextInt(min, max + 1)
            }
        }

}