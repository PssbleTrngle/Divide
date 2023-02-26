package possible_triangle.divide

import io.github.fabricators_of_create.porting_lib.event.common.*
import io.github.fabricators_of_create.porting_lib.event.common.PlayerEvents.BreakSpeed
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.bounty.BountyEvents
import possible_triangle.divide.command.*
import possible_triangle.divide.command.admin.PauseCommand
import possible_triangle.divide.crates.CrateEvent
import possible_triangle.divide.crates.CrateEvents
import possible_triangle.divide.crates.Order
import possible_triangle.divide.crates.callbacks.CleanCallback
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.Eras
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.extensions.time
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.info.Scores
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.LoginShield
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.missions.Mission
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.actions.BaseBuff
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

        ServerLifecycleEvents.SERVER_STOPPED.register {
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

            val time = it.time()

            if (time % 5 == 0L) {
                Scores.updateScores(it)
            }

            if (time % 20 == 0L) {
                Teams.updateSpectators(it)
                LoginShield.tickLogin()
                Action.tickActions(it)
            }
        }

        ServerTickEvents.START_WORLD_TICK.register {
            Bases.boostCrops(it)
        }

        BreakSpeed.BREAK_SPEED.register { event ->
            val player = event.player
            val pos = event.pos ?: return@register
            if (player !is ServerPlayer) return@register

            if (CrateEvents.isUnbreakable(player.server, pos)) 0F
            else if (BaseBuff.isBuffed(player, Reward.MINING_FATIGUE)) event.newSpeed * MiningFatigue.MODIFIER
            else event.newSpeed
        }

        ServerPlayerEvents.AFTER_RESPAWN.register { player, _, _ ->
            DataHacker.clearReasons(player)
            DeathEvents.restoreItems(player)
        }

        ServerPlayerEvents.COPY_FROM.register { original, player, _ ->
            DeathEvents.copyHeartModifier(original, player)
        }

        PlayerTickEvents.START.register { player ->
            if (player !is ServerPlayer) return@register

            GameData.tickLobbyPlayer(player)
            Bases.updateBaseState(player)
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            GameData.onPlayerJoin(handler.player, server)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            Scores.resetSpoof(handler.player)
        }

        PlayerBlockBreakEvents.AFTER.register { world, _, pos, _, _ ->
            val server = world.server ?: return@register
            CleanCallback.cleanMarker(server, pos)
        }

        AdvancementCallback.EVENT.register { player, advancement ->
            if (player !is ServerPlayer) return@register
            BountyEvents.onAdvancement(player, advancement)
        }

        BlockEvents.BLOCK_BREAK.register { event ->
            val player = event.player
            if (player !is ServerPlayer) return@register

            BountyEvents.onBlockBreak(player, event.state)
        }

        LivingEntityEvents.HURT.register { _, entity, amount ->
            if (entity !is ServerPlayer) return@register amount

            if (LoginShield.isProtected(entity)) 0F
            else amount
        }

        EntitySleepEvents.STOP_SLEEPING.register { entity, _ ->
            if (entity !is ServerPlayer) return@register
            Mission.onSleep(entity)
        }

        ServerLivingEntityEvents.AFTER_DEATH.register { entity, source ->
            Mission.onDeath(entity, source)
            DeathEvents.onDeath(entity, source)
        }

        ItemCraftedCallback.EVENT.register { player, stack, _ ->
            Mission.onCrafted(player, stack)
        }

        ExplosionEvents.DETONATE.register { level, explosion, _, _ ->
            val server = level.server ?: return@register
            CrateEvents.modifyExplosion(server, explosion.toBlow)
        }
    }

}