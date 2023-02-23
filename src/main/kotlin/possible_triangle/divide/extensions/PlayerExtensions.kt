package possible_triangle.divide.extensions

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack

fun PlayerInventory.items(): List<ItemStack> {
    return listOf(player.inventory.main, player.inventory.armor, player.inventory.offHand).flatten()
}