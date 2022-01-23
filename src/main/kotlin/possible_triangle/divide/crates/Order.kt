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
data class Order(@SerialName("item") internal val itemId: String, val cost: Int, val max: Int?) {

    @Transient
    lateinit var id: String
        private set

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

        override fun populate(entry: Order, server: MinecraftServer?, id: String) {
            entry.id = id
            if (server != null) {
                val items = server.registryAccess().registryOrThrow(Registry.ITEM_REGISTRY)
                entry.item = items[ResourceLocation(entry.itemId)]
                    ?: throw IllegalArgumentException("Item ${entry.itemId} does not exists")
            }
        }

        init {
            defaulted("ender_pearl") { Order(Items.ENDER_PEARL, 200, 2) }
            defaulted("tnt") { Order(Blocks.TNT, 100, 4) }
            defaulted("dragon_breath") { Order(Items.DRAGON_BREATH, 100, 4) }

            defaulted("cow") { Order(Items.COW_SPAWN_EGG, 50, 1) }
            defaulted("pig") { Order(Items.PIG_SPAWN_EGG, 50, 1) }
            defaulted("sheep") { Order(Items.SHEEP_SPAWN_EGG, 50, 1) }
            defaulted("chicken") { Order(Items.CHICKEN_SPAWN_EGG, 50, 1) }
            defaulted("horse") { Order(Items.HORSE_SPAWN_EGG, 50, 1) }
            defaulted("wolf") { Order(Items.WOLF_SPAWN_EGG, 50, 1) }
        }

    }

    constructor(item: ItemLike, cost: Int, max: Int?) : this(
        item.asItem().registryName?.path ?: throw NullPointerException(),
        cost, max,
    )

    fun order(player: ServerPlayer, team: Team, amount: Int): Boolean {
        if (max != null && amount > max) throw TOO_MUCH.create(itemId, max)

        val price = amount * cost

        return Points.modify(player.server, team, -price) { pointsNow ->
            CrateScheduler.order(player.server, team, ItemStack(item, amount), this)
            LOGGER.log(player.server, Event(itemId, amount, price, pointsNow, EventPlayer.of(player)))
        }

    }

    @Transient
    lateinit var item: ItemLike

}
