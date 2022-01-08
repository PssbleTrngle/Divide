package possible_triangle.divide.crates.loot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.world.level.ItemLike
import kotlin.random.Random

@Serializable
data class LootEntry(
    @SerialName("item") internal val id: String,
    val weight: Int,
    @SerialName("amount") private val amounts: List<Int> = listOf()
) {

    constructor(item: ItemLike, weight: Int, amounts: List<Int> = listOf()) : this(
        item.asItem().registryName?.path ?: throw NullPointerException(),
        weight,
        amounts
    )

    @Transient
    lateinit var item: ItemLike

    val amount: Int
        get() {
            return if (amounts.isEmpty()) 1
            else if (amounts.size == 1) amounts.first()
            else {
                val (min, max) = amounts
                Random.nextInt(min, max)
            }
        }

}