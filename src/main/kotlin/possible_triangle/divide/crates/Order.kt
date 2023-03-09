package possible_triangle.divide.crates

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.teamOrThrow

@Serializable
data class Order(@Contextual val item: ItemLike, val cost: Int, val max: Int?) {

    @Transient
    lateinit var id: String
        private set

    companion object : DefaultedResource<Order>("orders", { Order.serializer() }) {

        private val TOO_MUCH =
            Dynamic2CommandExceptionType { id, max -> Component.literal("You can only order up to $max of ").append(id as Component) }

        @Serializable
        private data class Event(
            val order: Order,
            val amount: Int,
            val cost: Int,
            val pointsNow: Int,
            val orderedBy: EventTarget,
        )

        private val LOGGER = EventLogger("order", { Event.serializer() }) { inTeam { it.orderedBy.team } }

        override fun populate(entry: Order, id: String) {
            entry.id = id
        }

        init {
            defaulted("ender_pearl") { Order(Items.ENDER_PEARL, 200, 4) }
            defaulted("tnt") { Order(Blocks.TNT, 100, 8) }
            defaulted("dragon_breath") { Order(Items.DRAGON_BREATH, 100, 4) }

            defaulted("golden_apple") { Order(Items.GOLDEN_APPLE, 300, 4) }
            defaulted("end_crystal") { Order(Items.END_CRYSTAL, 1000, 1) }

            defaulted("cow") { Order(Items.COW_SPAWN_EGG, 50, 1) }
            defaulted("pig") { Order(Items.PIG_SPAWN_EGG, 50, 1) }
            defaulted("sheep") { Order(Items.SHEEP_SPAWN_EGG, 50, 1) }
            defaulted("chicken") { Order(Items.CHICKEN_SPAWN_EGG, 50, 1) }
            defaulted("horse") { Order(Items.HORSE_SPAWN_EGG, 50, 1) }
            defaulted("wolf") { Order(Items.WOLF_SPAWN_EGG, 50, 1) }
        }

    }

    fun order(player: ServerPlayer, amount: Int): Boolean {
        if (max != null && amount > max) throw TOO_MUCH.create(item.asItem().description, max)

        val price = amount * cost
        val team = player.teamOrThrow()

        return Points.modify(player.server, team, -price) { pointsNow ->
            CrateScheduler.order(player.server, team, ItemStack(item, amount), this)
            LOGGER.log(player.server, Event(this, amount, price, pointsNow, EventTarget.of(player)))
        }

    }

}
