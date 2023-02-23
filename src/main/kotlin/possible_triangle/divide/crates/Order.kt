package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.block.Blocks
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.teamOrThrow

@Serializable
data class Order(@SerialName("item") internal val itemId: String, val cost: Int, val max: Int?) {

    @Transient
    lateinit var id: String
        private set

    companion object : DefaultedResource<Order>("orders", { Order.serializer() }) {

        private val TOO_MUCH =
            Dynamic2CommandExceptionType { id, max -> Text.literal("You can only order up to $max of $id") }

        @Serializable
        private data class Event(
            val order: Order,
            val amount: Int,
            val cost: Int,
            val pointsNow: Int,
            val orderedBy: EventTarget,
        )

        private val LOGGER = EventLogger("order", { Event.serializer() }) { inTeam { it.orderedBy.team } }

        override fun populate(entry: Order, server: MinecraftServer?, id: String) {
            entry.id = id
            if (server != null) {
                val items = server.registryManager.get(RegistryKeys.ITEM)
                entry.item = items[Identifier(entry.itemId)]
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

    constructor(item: ItemConvertible, cost: Int, max: Int?) : this(
        item.asItem().registryEntry.registryKey().value.path ?: throw NullPointerException(),
        cost, max,
    )

    fun order(player: ServerPlayerEntity, amount: Int): Boolean {
        if (max != null && amount > max) throw TOO_MUCH.create(itemId, max)

        val price = amount * cost
        val team = player.teamOrThrow()

        return Points.modify(player.server, team, -price) { pointsNow ->
            CrateScheduler.order(player.server, team, ItemStack(item, amount), this)
            LOGGER.log(player.server, Event(this, amount, price, pointsNow, EventTarget.of(player)))
        }

    }

    @Transient
    lateinit var item: ItemConvertible

}
