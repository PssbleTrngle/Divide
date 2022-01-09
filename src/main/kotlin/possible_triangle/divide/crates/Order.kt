package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.core.Registry
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.scores.Team
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.Points

@Serializable
data class Order(
    @SerialName("item") internal val id: String,
    val cost: Int,
    val max: Int?,
) {

    @Transient
    private val TOO_MUCH = SimpleCommandExceptionType(TextComponent("You can only order up to $max of $id"))

    constructor(item: ItemLike, cost: Int, max: Int?) : this(
        item.asItem().registryName?.path ?: throw NullPointerException(),
        cost, max,
    )

    companion object : DefaultedResource<Order>("orders", { Order.serializer() }) {

        override fun populate(entry: Order, server: MinecraftServer) {
            super.populate(entry, server)
            val items = server.registryAccess().registryOrThrow(Registry.ITEM_REGISTRY)
            entry.item = items[ResourceLocation(entry.id)]
                ?: throw IllegalArgumentException("Item ${entry.id} does not exists")
        }

        init {
            defaulted("ender_pearl") { Order(Items.ENDER_PEARL, 200, 2) }
            defaulted("tnt") { Order(Blocks.TNT, 500, 4) }
        }

    }

    fun order(server: MinecraftServer, team: Team, amount: Int): Boolean {
        if (max != null && amount > max) throw TOO_MUCH.create()

        val price = amount * cost

        return Points.modify(server, team, -price) {
            CrateScheduler.order(server, team, ItemStack(item, amount), this)
        }
    }

    @Transient
    lateinit var item: ItemLike

}
