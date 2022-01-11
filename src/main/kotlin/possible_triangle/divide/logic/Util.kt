package possible_triangle.divide.logic

import kotlin.random.Random

fun <T> makeWeightedDecision(rolls: Int, values: Map<T, Number>): List<T> {
    val weighted = hashMapOf<T, Double>()

    var total = 0.0
    values.mapValues { it.value.toDouble() }.forEach { (entry, weight) ->
        weighted[entry] = total
        total += weight
    }

    if (total <= 0.0) throw  IllegalArgumentException("Total weight is 0")

    return (0..rolls)
        .map { Random.nextDouble(0.0, total) }
        .map { roll ->
            weighted.entries.findLast { it.value <= roll }
                ?: throw NullPointerException("Could not find entry for $roll in map $weighted with total $total")
        }
        .map { it.key }
}

fun <T> makeWeightedDecision(values: Map<T, Number>): T? {
    return makeWeightedDecision(1, values).firstOrNull()
}