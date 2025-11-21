package possible_triangle.divide.extensions

import net.minecraft.network.chat.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

inline val Int.ticks get() = times(50).milliseconds

inline val Long.ticks get() = times(50).milliseconds

inline val Duration.inTicks get() = inWholeMilliseconds / 50

fun Duration.toText() = this.toString()

fun Duration.toComponent() = Component.literal(toText())