package possible_triangle.divide.bounty

import kotlinx.serialization.Serializable

@Serializable
data class Amount(private val type: Type, private val values: List<Int>) {

    @Serializable
    enum class Type {
        CONSTANT,
        INCREASING,
        FIRST,
    }

    fun get(index: Int): Int {
        return when (type) {
            Type.CONSTANT -> values.first()
            Type.INCREASING -> values.first() + values[2] * index
            Type.FIRST -> if (index > 0) values.getOrElse(2) { 0 } else values.first()
        }
    }

}