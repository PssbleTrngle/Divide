package possible_triangle.divide

import kotlinx.serialization.Serializable
import possible_triangle.divide.data.DefaultedResource


object Config : DefaultedResource<Config.Values>(".", { Values.serializer() }) {

    val CONFIG by defaulted("config") { Values() }

    @Serializable
    data class Values(
        val minHearts: Int = 4,
        val starterCash: Int = 0,
        val starterGearBreak: Int = 60 * 5,
        val border: BorderValues = BorderValues(),
        val crate: CrateValues = CrateValues(),
        val eras: EraValues = EraValues(),
    )

    @Serializable
    data class BorderValues(
        val lobbySize: Int = 10,
        val bigBorder: Int = 400,
        val smallBorder: Int = 150,
        val staySmallFor: Int = 60 * 1,
        val stayBigFor: Int = 60 * 5,
        val moveTime: Int = 60,
    )

    @Serializable
    data class CrateValues(
        val cleanUpTime: Int = 20,
        val cleanNonEmpty: Boolean = false,
        val itemSaveChance: Double = 0.5,
    )

    @Serializable
    data class EraValues(
        val peaceTime: Int = 60 * 5,
        val warTime: Int = 60 * 60,
    )

}