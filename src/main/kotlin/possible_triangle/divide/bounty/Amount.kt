package possible_triangle.divide.bounty

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
sealed class Amount {
    abstract fun get(index: Int): Int
}

@Serializable
@SerialName("constant")
data class ConstantAmount(val value: Int) : Amount() {
    override fun get(index: Int) = value
}

@Serializable
@SerialName("increasing")
data class IncreasingAmount(val base: Int, val step: Int, val max: Int? = null) : Amount() {
    override fun get(index: Int): Int {
        val amount = base + step * index
        return if (max == null) amount
        else min(amount, max)
    }
}

@Serializable
@SerialName("decreasing")
data class DecreasingAmount(val base: Int, val step: Int, val min: Int = 0) : Amount() {
    override fun get(index: Int): Int {
        val amount = base - step * index
        return max(amount, min)
    }
}

@Serializable
@SerialName("list")
data class ListAmount(val values: List<Int>) : Amount() {
    override fun get(index: Int): Int {
        return values.getOrElse(index) { 0 }
    }
}