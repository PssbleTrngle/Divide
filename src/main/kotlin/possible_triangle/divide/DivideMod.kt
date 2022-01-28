package possible_triangle.divide

import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import possible_triangle.divide.api.ServerApi
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.command.admin.PauseCommand
import possible_triangle.divide.crates.CrateEvent
import possible_triangle.divide.crates.Order
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.Eras
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.missions.Mission
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.reward.Reward

inline val Double.m get() = this * 60
inline val Double.h get() = this.m * 60
inline val Int.m get() = this * 60
inline val Int.h get() = this.m * 60

@Mod(DivideMod.ID)
@Mod.EventBusSubscriber
object DivideMod {
    const val ID = "divide"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.info("Divide booting")

        listOf(CrateLoot, Bounty, Reward, Config, Order, Mission).forEach {
            ReloadedResource.register(it)
        }

        listOf(Border, Eras, CrateEvent, PlayerBountyEvent, MissionEvent).forEach {
            it.register()
        }

    }

    @ExperimentalSerializationApi
    @SubscribeEvent
    fun onServerStart(event: ServerStartedEvent) {
        if (GameData.DATA[event.server].paused) PauseCommand.showDisplay(event.server)
        if (Config.CONFIG.api.enabled) ServerApi.start(event.server)
    }

    @ExperimentalSerializationApi
    @SubscribeEvent
    fun onServerStop(event: ServerStoppedEvent) {
        if (Config.CONFIG.api.enabled) ServerApi.stop()
    }

}