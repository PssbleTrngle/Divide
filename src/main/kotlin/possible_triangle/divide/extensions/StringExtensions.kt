package possible_triangle.divide.extensions

fun Int?.toDuration() = this?.let { "in ${it}s" } ?: "now"