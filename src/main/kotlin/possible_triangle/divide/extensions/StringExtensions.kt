package possible_triangle.divide.extensions

fun Int?.toDuration() = this?.let { "in ${it / 20}s" } ?: "now"