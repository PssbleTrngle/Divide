package possible_triangle.divide.logic

fun <T> makeWeightedDecition(rolls: Int, values: Map<T, Number>): List<T> {
    val weighted = arrayListOf<T>()

    values.forEach { (entry, weight) ->
        repeat(weight.toInt()) {
            weighted.add(entry)
        }
    }

    return weighted.shuffled().take(rolls)
}

fun <T> makeWeightedDecition(values: Map<T, Number>): T {
    return makeWeightedDecition(1, values).first()
}