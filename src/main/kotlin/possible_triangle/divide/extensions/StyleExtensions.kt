package possible_triangle.divide.extensions

import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

fun MutableComponent.noItalic() = withStyle { it.noItalic() }

fun Style.noItalic() = withItalic(false)