package possible_triangle.divide.bounty

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Amount(private val type: Type, private val values: List<Int>) {

    @Serializable
    enum class Type {
        CONSTANT,
        INCREASING,
        DECREASING,
        FIRST,
    }

    fun get(index: Int): Int {
        return when (type) {
            Type.CONSTANT -> values.first()
            Type.INCREASING -> min(values.first() + values.getOrElse(1) { 0 } * index, values.getOrElse(2) { Int.MAX_VALUE })
            Type.DECREASING -> max(values.first() - values.getOrElse(1) { 0 } * index, values.getOrElse(2) { 0 })
            Type.FIRST ->
                if (index <= 0) values.first()
                else if (values.size < 3 || index <= values[2]) values.getOrElse(1) { 0 }
                else 0
        }
    }

}