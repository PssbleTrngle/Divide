package possible_triangle.divide

import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import possible_triangle.divide.api.ServerApi
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.crates.Order
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.reward.Reward

@Mod(DivideMod.ID)
@Mod.EventBusSubscriber
object DivideMod {
    const val ID = "divide"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.info("Divide booting")

        ReloadedResource.register(CrateLoot)
        ReloadedResource.register(Bounty)
        ReloadedResource.register(Reward)
        ReloadedResource.register(Config)
        ReloadedResource.register(Order)
    }

    @SubscribeEvent
    fun onServerStart(event: ServerStartedEvent) {
        if(Config.CONFIG.api.enabled) ServerApi.start(event.server)
    }

    @SubscribeEvent
    fun onServerStop(event: ServerStoppedEvent) {
        if(Config.CONFIG.api.enabled) ServerApi.stop()
    }

}