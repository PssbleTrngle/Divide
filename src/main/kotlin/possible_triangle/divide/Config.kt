package possible_triangle.divide

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable
import possible_triangle.divide.data.DefaultedResource
import kotlin.random.Random


object Config : DefaultedResource<Config.Values>(".", { Values.serializer() }) {

    val CONFIG by defaulted("config") { Values() }

    override fun config(): YamlConfiguration {
        return YamlConfiguration()
    }

    @Serializable
    data class IntRange(private val min: Int, private val max: Int) {
        val value
            get() = if (min >= max) min else Random.nextInt(min, max)
    }

    @Serializable
    data class DoubleRange(private val min: Double, private val max: Double) {
        val value
            get() = if (min >= max) min else Random.nextDouble(min, max)
    }

    @Serializable
    data class Values(
        val minHearts: Int = 4,
        val starterCash: Int = 0,
        val loginShield: Int = 5,
        val deaths: DeathValues = DeathValues(),
        val border: BorderValues = BorderValues(),
        val crate: CrateValues = CrateValues(),
        val eras: EraValues = EraValues(),
        val bounties: BountyValues = BountyValues(),
        val bases: BaseValues = BaseValues(),
        val missions: MissionValues = MissionValues(),
        val admins: List<String> = listOf(),
        val api: ApiValues = ApiValues(),
    )

    @Serializable
    data class DeathValues(
        val keepPercent: DoubleRange = DoubleRange(0.2, 0.8),
        val downgradeProbability: Double = 0.3,
        val starterGearBreak: Int = 60 * 5,
    )

    @Serializable
    data class BorderValues(
        val enabled: Boolean = true,
        val startAt: Int = 0,
        val lobbySize: Int = 10,
        val bigBorder: Int = 400,
        val smallBorder: Int = 200,
        val staySmallFor: IntRange = IntRange(60 * 15, 60 * 30),
        val stayBigFor: IntRange = IntRange(3600, 3600 * 2),
        val moveTime: Int = 60 * 5,
        val showBar: Boolean = false,
        val damagePerBlock: Double = 1.0,
        val damageSafeZone: Double = 2.0,
    )

    @Serializable
    data class CrateValues(
        val enabled: Boolean = true,
        val startAt: Int = 4,
        val lockedFor: Int = 60 * 5,
        val pause: IntRange = IntRange(3600 * 30, 3600 * 60),
        val cleanUpTime: Int = 60 * 10,
        val cleanNonEmpty: Boolean = true,
        val clearOnCleanup: Boolean = true,
        val itemSaveChance: Double = 0.125,
        val splitAndShuffle: Boolean = true,
        val levels: List<Int> = listOf(70, 100, 120),
    )

    @Serializable
    data class EraValues(
        val enabled: Boolean = true,
        val startAt: Int = 0,
        val peaceTime: IntRange = IntRange(60 * 5, 60 * 5),
        val warTime: IntRange = IntRange(60 * 60, 60 * 60),
        val showPeaceBar: Boolean = true,
        val showWarBar: Boolean = false,
    )

    @Serializable
    data class BountyValues(
        val enabled: Boolean = true,
        val startAt: Int = 2,
        val baseAmount: Int = 120,
        val bonusPerAliveMinute: Int = 10,
        val pause: IntRange = IntRange(3600, 3600 * 2),
        val bountyTime: Int = 60 * 20,
        val clearOnDeath: Boolean = false
    )

    @Serializable
    data class MissionValues(
        val enabled: Boolean = true,
        val startAt: Int = 2,
        val pause: IntRange = IntRange(3600, 3600 * 2),
    )

    @Serializable
    data class BaseValues(
        val radius: Double = 8.0
    )

    @Serializable
    data class ApiValues(
        val secret: String = "banana",
        val port: Int = 8080,
        val enabled: Boolean = true,
        val host: String = "http://localhost",
    )

}