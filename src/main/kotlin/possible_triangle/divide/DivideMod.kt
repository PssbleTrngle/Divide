package possible_triangle.divide

import io.github.fabricators_of_create.porting_lib.event.common.PlayerBreakSpeedCallback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.network.ServerPlayerEntity
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.command.*
import possible_triangle.divide.command.admin.PauseCommand
import possible_triangle.divide.crates.CrateEvent
import possible_triangle.divide.crates.Order
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.Eras
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.info.Scores
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.LoginShield
import possible_triangle.divide.missions.Mission
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.actions.secret.MiningFatigue

inline val Double.m get() = this * 60
inline val Double.h get() = this.m * 60
inline val Int.m get() = this * 60
inline val Int.h get() = this.m * 60

object DivideMod : ModInitializer {
    const val ID = "divide"

    val LOGGER: Logger = LogManager.getLogger(ID)

    override fun onInitialize() {
        LOGGER.info("Divide booting")

        listOf(CrateLoot, Bounty, Reward, Config, Order, Mission).forEach {
            ReloadedResource.register(it)
        }

        listOf(Border, Eras, CrateEvent, PlayerBountyEvent, MissionEvent).forEach {
            it.register()
        }

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            if (GameData.DATA[server].paused) PauseCommand.showDisplay(server)
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            AdminCommand.register(dispatcher)
            BaseCommand.register(dispatcher)
            BuyCommand.register(dispatcher)
            GlowCommand.register(dispatcher)
            OrderCommand.register(dispatcher)
            PointsCommand.register(dispatcher)
            SellCommand.register(dispatcher)
            ShowCommand.register(dispatcher)
        }

        ServerTickEvents.START_SERVER_TICK.register {
            ReloadedResource.tickWatchers(it)

            val time = it.overworld.time

            if (time % 5 == 0L) {
                Scores.updateScores(it)
            }

            if (time % 20 == 0L) {
                LoginShield.tickLogin()
            }
        }

        PlayerBreakSpeedCallback.EVENT.register { event ->
            val player = event.player
            if (player !is ServerPlayerEntity) return@register

            event.newSpeed = listOf(
                MiningFatigue::modifyBreakSpeed
            ).fold(event.newSpeed) { original, func -> func(player, original) }
        }

        ServerPlayerEvents.AFTER_RESPAWN.register { player, _, _ ->
            DeathEvents.restoreItems(player)
        }

        ServerPlayerEvents.COPY_FROM.register { original, player, _ ->
            DeathEvents.copyHeartModifier(original, player)
        }
    }

}