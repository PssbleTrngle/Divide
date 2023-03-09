package possible_triangle.divide.gui

import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import possible_triangle.divide.logic.Teams.isAdmin

open class ClearableGui(type: MenuType<*>, player: ServerPlayer) : SimpleGui(type, player, false) {

    protected fun withCleared(block: () -> Unit) {
        (0 until size).forEach {
            clearSlot(it)
        }
        block()
    }

    protected fun <T> runCatching(block: () -> T): T? {
        return try {
            block()
        } catch (ex: Exception) {
            title = Component.literal(ex.message?.takeIf { player.isAdmin() } ?: "An error occurred :/")
                .withStyle(ChatFormatting.RED)
            null
        }
    }

}