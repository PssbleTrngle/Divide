package possible_triangle.divide.crates.loot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import kotlin.random.Random

@Serializable
data class LootEntry(
    @SerialName("item") internal val id: String,
    val weight: Int,
    @SerialName("amount") private val amounts: List<Int>? = null,
    val functions: List<LootFunction>? = null,
) {

    fun populate(server: MinecraftServer) {
        val items = server.registryAccess().registryOrThrow(Registry.ITEM_REGISTRY)
        item = items[ResourceLocation(id)]
            ?: throw IllegalArgumentException("Item $id does not exists")
    }

    constructor(
        item: ItemLike, weight: Int, amounts: List<Int> = listOf(),
        functions: List<LootFunction>? = null,
    ) : this(
        item.asItem().registryName?.path ?: throw NullPointerException(),
        weight, amounts, functions,
    )

    @Transient
    lateinit var item: ItemLike

    fun createStack(): ItemStack {
        val stack = ItemStack(item, amount)
        functions?.forEach { it.apply(stack) }
        return stack
    }

    val amount: Int
        get() {
            return if (amounts == null || amounts.isEmpty()) 1
            else if (amounts.size == 1) amounts.first()
            else {
                val (min, max) = amounts
                Random.nextInt(min, max + 1)
            }
        }

}