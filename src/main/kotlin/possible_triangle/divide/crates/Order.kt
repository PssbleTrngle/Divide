package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.core.Registry
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.scores.Team
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.EventPlayer
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Points

@Serializable
data class Order(@SerialName("item") internal val id: String, val cost: Int, val max: Int?) {

    companion object : DefaultedResource<Order>("orders", { Order.serializer() }) {

        private val TOO_MUCH =
            Dynamic2CommandExceptionType { id, max -> TextComponent("You can only order up to $max of $id") }

        @Serializable
        private data class Event(
            val order: String,
            val amount: Int,
            val cost: Int,
            val pointsNow: Int, val orderedBy: EventPlayer,
        )

        private val LOGGER = EventLogger("order") { Event.serializer() }

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

    constructor(item: ItemLike, cost: Int, max: Int?) : this(
        item.asItem().registryName?.path ?: throw NullPointerException(),
        cost, max,
    )

    fun order(player: ServerPlayer, team: Team, amount: Int): Boolean {
        if (max != null && amount > max) throw TOO_MUCH.create(id, max)

        val price = amount * cost

        return Points.modify(player.server, team, -price) { pointsNow ->
            CrateScheduler.order(player.server, team, ItemStack(item, amount), this)
            LOGGER.log(player.server, Event(id, amount, price, pointsNow, EventPlayer.of(player)))
        }

    }

    @Transient
    lateinit var item: ItemLike

}
