package possible_triangle.divide.extensions

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Items

fun MutableComponent.noItalic() = withStyle { it.noItalic() }

fun Style.noItalic() = withItalic(false)

fun ChatFormatting.toIcon() = when(this) {
    ChatFormatting.BLACK -> Items.BLACK_DYE
    ChatFormatting.DARK_BLUE -> Items.BLUE_DYE
    ChatFormatting.DARK_GREEN -> Items.GREEN_DYE
    ChatFormatting.DARK_AQUA -> Items.CYAN_DYE
    ChatFormatting.DARK_RED -> Items.RED_DYE
    ChatFormatting.DARK_PURPLE -> Items.PURPLE_DYE
    ChatFormatting.GOLD -> Items.ORANGE_DYE
    ChatFormatting.GRAY -> Items.LIGHT_GRAY_DYE
    ChatFormatting.DARK_GRAY -> Items.GRAY_DYE
    ChatFormatting.BLUE -> Items.LIGHT_BLUE_DYE
    ChatFormatting.GREEN -> Items.LIME_DYE
    ChatFormatting.AQUA -> Items.CYAN_DYE
    ChatFormatting.RED -> Items.RED_DYE
    ChatFormatting.LIGHT_PURPLE -> Items.PINK_DYE
    ChatFormatting.YELLOW -> Items.YELLOW_DYE
    ChatFormatting.WHITE -> Items.WHITE_DYE
    else -> Items.BEDROCK
}