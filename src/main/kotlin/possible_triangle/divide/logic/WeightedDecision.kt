package possible_triangle.divide.logic

import kotlin.random.Random

fun <T> makeWeightedDecision(rolls: Int, values: Map<T, Number>, random: Random = Random): List<T> {
    val weighted = arrayListOf<Pair<T, Double>>()

    var total = 0.0
    values.forEach { (entry, weight) ->
        weighted += entry to total
        total += weight.toDouble()
    }

    if (total <= 0.0) return emptyList()

    return (1..rolls)
        .map { random.nextDouble(0.0, total) }
        .map { roll ->
            weighted.findLast { it.second <= roll }
                ?: throw NullPointerException("Could not find entry for $roll with total $total")
        }
        .map { it.first }
}

fun <T> makeWeightedDecision(values: Map<T, Number>): T? {
    return makeWeightedDecision(1, values).firstOrNull()
}