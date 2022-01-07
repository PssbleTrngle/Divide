package possible_triangle.divide

import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable
import possible_triangle.divide.data.DefaultedResource


object Config : DefaultedResource<Config.Values>(".", { Values.serializer() }) {

    val CONFIG by defaulted("config") {
        Values(
            minHearts = 6,

            )
    }

    @Serializable
    data class Values(
        val minHearts: Int,
        val starterCash: Int,
        val starterGearBreak: Int,
        val border: BorderValues,
    )

    @Serializable
    data class BorderValues(
        val lobbySize: Int,
        val bigBorder: Int,
        val smallBorder: Int,
        val cycleTime: Int
    )

}