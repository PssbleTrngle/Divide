package possible_triangle.divide

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable
import possible_triangle.divide.data.DefaultedResource
import kotlin.random.Random


object Config : DefaultedResource<Config.Values>(".", { Values.serializer() }, id = "config") {

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
        val loginShield: Int = 4,
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
        val api: ApiValues = ApiValues(),
    )

    @Serializable
    data class DeathValues(
        val keepPercent: DoubleRange = DoubleRange(0.2, 0.8),
        val downgradeProbability: Double = 0.0,
        val starterGearBreak: Int = 5.m,
    )

    @Serializable
    data class BorderValues(
        val enabled: Boolean = true,
        val startAfter: Int = 0,
        val lobbySize: Int = 10,
        val bigBorder: Int = 400,
        val smallBorder: Int = 200,
        val staySmallFor: IntRange = IntRange(10.m, 15.m),
        val stayBigFor: IntRange = IntRange(1.h, 2.h),
        val moveTime: Int = 5.m,
        val showBar: Boolean = false,
        val damagePerBlock: Double = 1.0,
        val damageSafeZone: Double = 2.0,
    )

    @Serializable
    data class CrateValues(
        val enabled: Boolean = true,
        val startAfter: Int = 30.m,
        val lockedFor: Int = 5.m,
        val pause: IntRange = IntRange(30.m, 1.h),
        val cleanUpTime: Int = 10.m,
        val cleanNonEmpty: Boolean = true,
        val clearOnCleanup: Boolean = true,
        val itemSaveChance: Double = 0.125,
        val splitAndShuffle: Boolean = true,
        val levels: List<Int> = listOf(70, 100, 120),
    )

    @Serializable
    data class EraValues(
        val enabled: Boolean = true,
        val startAfter: Int = 0,
        val peaceTime: IntRange = IntRange(10.m, 10.m),
        val warTime: IntRange = IntRange(1.h, 2.h),
        val showPeaceBar: Boolean = true,
        val showWarBar: Boolean = false,
    )

    @Serializable
    data class BountyValues(
        val enabled: Boolean = true,
        val startAfter: Int = 2.h,
        val baseAmount: Int = 120,
        val bonusPerAliveMinute: Int = 10,
        val pause: IntRange = IntRange(90.m, 150.m),
        val bountyTime: Int = 20.m,
        val clearOnDeath: Boolean = false,
    )

    @Serializable
    data class MissionValues(
        val enabled: Boolean = true,
        val singleBonus: Boolean = true,
        val startAfter: Int = 90.m,
        val safeTime: Int = 5,
        val pause: IntRange = IntRange(30.m, 90.m),
    )

    @Serializable
    data class BaseValues(
        val radius: Double = 6.0,
    )

    @Serializable
    data class ApiValues(
        val secret: String = "banana",
        val port: Int = 8080,
        val enabled: Boolean = true,
        val host: String = "http://localhost:3000",
        val ignoreEventPermission: Boolean = false,
    )

}