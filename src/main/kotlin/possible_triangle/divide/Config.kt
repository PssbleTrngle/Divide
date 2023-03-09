package possible_triangle.divide

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.DurationAsInt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Config : DefaultedResource<Config.Values>(".", { Values.serializer() }, id = "config") {

    val CONFIG by defaulted("config") { Values() }

    override fun config(): YamlConfiguration {
        return YamlConfiguration()
    }

    @Serializable
    data class IntRange(private val min: Int, private val max: Int = min) {
        val value
            get() = if (min >= max) min else Random.nextInt(min, max)
    }

    @Serializable
    data class DurationRange(private val min: DurationAsInt, private val max: DurationAsInt = min) {
        val value
            get() = if (min >= max) min else Random.nextLong(
                min.inWholeMilliseconds,
                max.inWholeMilliseconds
            ).milliseconds
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
        val loginShield: DurationAsInt = 4.seconds,
        val autoPause: Boolean = true,
        val secretRewards: Boolean = false,
        val deaths: DeathValues = DeathValues(),
        val border: BorderValues = BorderValues(),
        val crate: CrateValues = CrateValues(),
        val eras: EraValues = EraValues(),
        val bounties: BountyValues = BountyValues(),
        val bases: BaseValues = BaseValues(),
        val missions: MissionValues = MissionValues(),
        val admins: List<String> = listOf(),
    )

    @Serializable
    data class DeathValues(
        val keepPercent: DoubleRange = DoubleRange(0.2, 0.8),
        val downgradeProbability: Double = 0.0,
        val starterGearBreak: DurationAsInt = 5.minutes,
    )

    @Serializable
    data class BorderValues(
        val enabled: Boolean = true,
        val startAfter: DurationAsInt = Duration.ZERO,
        val lobbySize: Int = 10,
        val bigBorder: Int = 400,
        val smallBorder: Int = 200,
        val staySmallFor: DurationRange = DurationRange(8.minutes, 12.minutes),
        val stayBigFor: DurationRange = DurationRange(40.minutes, 1.hours),
        val firstGrowTime: DurationAsInt = 5.minutes,
        val secondsPerBlock: DurationAsInt = 2.seconds,
        val showBar: Boolean = false,
        val damagePerBlock: Double = 1.0,
        val damageSafeZone: Double = 2.0,
    )

    @Serializable
    data class CrateValues(
        val enabled: Boolean = true,
        val startAfter: DurationAsInt = 10.minutes,
        val lockedFor: DurationAsInt = 4.minutes,
        val pause: DurationRange = DurationRange(20.minutes, 40.minutes),
        val cleanUpTime: DurationAsInt = 10.minutes,
        val cleanNonEmpty: Boolean = true,
        val clearOnCleanup: Boolean = true,
        val itemSaveChance: Double = 0.125,
        val splitAndShuffle: Boolean = true,
        val levels: List<Int> = listOf(50, 70, 100, 120),
    )

    @Serializable
    data class EraValues(
        val enabled: Boolean = true,
        val startAfter: DurationAsInt = Duration.ZERO,
        val peaceTime: DurationRange = DurationRange(10.minutes),
        val warTime: DurationRange = DurationRange(40.minutes, 1.hours),
        val showPeaceBar: Boolean = true,
        val showWarBar: Boolean = false,
    )

    @Serializable
    data class BountyValues(
        val enabled: Boolean = true,
        val startAfter: DurationAsInt = 1.hours,
        val baseAmount: Int = 120,
        val bonusPerAliveMinute: Int = 10,
        val pause: DurationRange = DurationRange(40.minutes, 1.hours),
        val bountyTime: DurationAsInt = 20.minutes,
        val clearOnDeath: Boolean = false,
    )

    @Serializable
    data class MissionValues(
        val enabled: Boolean = true,
        val singleBonus: Boolean = true,
        val startAfter: DurationAsInt = 90.minutes,
        val safeTime: DurationAsInt = 5.seconds,
        val pause: DurationRange = DurationRange(30.minutes, 90.minutes),
    )

    @Serializable
    data class BaseValues(
        val radius: Double = 6.0,
    )

}