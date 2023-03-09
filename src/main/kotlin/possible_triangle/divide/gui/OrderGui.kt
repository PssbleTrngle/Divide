package possible_triangle.divide.gui

import eu.pb4.sgui.api.elements.GuiElementInterface
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import possible_triangle.divide.crates.Order
import possible_triangle.divide.extensions.noItalic
import possible_triangle.divide.extensions.setLore
import possible_triangle.divide.extensions.lore
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participantTeam

class OrderGui(player: ServerPlayer) : ClearableGui(MenuType.GENERIC_9x4, player) {

    companion object {
        private val NOT_ENOUGH_POINTS = Component.literal("Not enough points :/").withStyle(ChatFormatting.RED).noItalic()
    }

    val team get() = player.participantTeam()

    val points get() = team?.let { Points.get(player.server, it) } ?: 0

    fun showOrders() = withCleared {
        title = Component.literal("Order items ")
            .append(Component.literal("[$points points]").withStyle(ChatFormatting.GOLD))
        Order.values.filter { Order.isVisible(it, team, player.server) }.forEachIndexed { i, reward ->
            setSlot(i, OrderElement(reward))
        }
    }

    fun showAmounts(order: Order) = withCleared {
        title = Component.literal("How many?")
        1.rangeTo(order.max ?: 27).filter {
            order.cost * it <= points
        }.forEachIndexed { i, amount ->
            setSlot(i, AmountElement(order, amount))
        }
    }

    fun buy(order: Order, amount: Int) = runCatching {
        val success = order.order(player, amount)
        if (success) {
            close()
        } else {
            title = NOT_ENOUGH_POINTS
        }
    }

    inner class AmountElement(private val order: Order, private val amount: Int) : GuiElementInterface {

        private val icon = ItemStack(order.item).apply {
            count = amount
        }

        override fun getItemStack() = icon

        override fun getGuiCallback(): GuiElementInterface.ClickCallback {
            return GuiElementInterface.ClickCallback { _, _, _, _ ->
                buy(order, amount)
            }
        }
    }

    inner class OrderElement(private val order: Order) : GuiElementInterface {

        private val canBuy get() = points >= order.cost

        private val icon = ItemStack(order.item).apply {
            setLore(listOfNotNull(
                NOT_ENOUGH_POINTS.takeUnless { canBuy },
                lore("costs ${order.cost} points"),
            ))
        }

        override fun getItemStack() = icon

        override fun getGuiCallback(): GuiElementInterface.ClickCallback {
            return GuiElementInterface.ClickCallback { _, _, _, _ ->
                if (canBuy) showAmounts(order)
            }
        }
    }

}

fun ServerPlayer.openOrderGui() {
    OrderGui(this).apply {
        showOrders()
        open()
    }
}