package possible_triangle.divide

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.command.AdminCommand
import possible_triangle.divide.command.admin.PauseCommand
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent.Companion.EVENTS
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.SecretRewards

data class GameData(val paused: Boolean, val started: Boolean) {

    @Serializable
    private data class Event(val action: String)

    @Mod.EventBusSubscriber
    companion object {

        private val LOGGER = EventLogger("game", { Event.serializer() }) { always() }

        fun setPaused(server: MinecraftServer, boolean: Boolean) {
            LOGGER.log(server, Event(if (boolean) "paused" else "resumed"))

            PauseCommand.showDisplay(server)

            val current = DATA[server]
            DATA[server] = current.copy(paused = boolean)
            AdminCommand.reload(server)
        }

        fun setStarted(server: MinecraftServer, boolean: Boolean) {
            LOGGER.log(server, Event(if (boolean) "started" else "stopped"))

            val current = DATA[server]
            DATA[server] = current.copy(started = boolean)
            AdminCommand.reload(server)

            server.gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(!boolean, server)

            if (boolean) {

                if (Config.CONFIG.secretRewards) SecretRewards.choose(server)

                Teams.players(server).forEach { player ->
                    player.setGameMode(GameType.SURVIVAL)
                    DeathEvents.startedGear(player).forEach { player.addItem(it) }
                    Chat.subtitle(player, "Started")
                }

                EVENTS.forEach { it.startCycle(server) }

            } else {

                EVENTS.forEach { it.stop(server) }
                Border.lobby(server)

            }
        }

        val DATA = object : ModSavedData<GameData>("_gamedata") {

            override fun save(nbt: CompoundTag, value: GameData) {
                nbt.putBoolean("paused", value.paused)
                nbt.putBoolean("started", value.started)
            }

            override fun load(nbt: CompoundTag, server: MinecraftServer): GameData {
                val paused = nbt.getBoolean("paused")
                val started = nbt.getBoolean("started")
                return GameData(paused || (started && Config.CONFIG.autoPause), started)
            }

            override fun default(): GameData {
                return GameData(paused = false, started = false)
            }
        }

        private val LOBBY_EFFECTS = listOf(MobEffects.SATURATION, MobEffects.DAMAGE_RESISTANCE)
            .map { MobEffectInstance(it, 20 * 5, 100, false, false) }

        @SubscribeEvent
        fun tick(event: TickEvent.PlayerTickEvent) {
            if (Util.shouldSkip(event, { it.player.level }, ticks = 1)) return

            val player = event.player
            if (player !is ServerPlayer) return
            if (!DATA[player.server].started) {
                LOBBY_EFFECTS.forEach(player::addEffect)
            }
        }

        @SubscribeEvent
        fun playerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
            val player = event.player
            if (player !is ServerPlayer) return
            player.awardRecipes(player.server.recipeManager.recipes)
            player.setGameMode(if (DATA[player.server].started) GameType.SURVIVAL else GameType.ADVENTURE)
            player.connection.send(ClientboundSetTitlesAnimationPacket(5, 20, 5))
        }
    }
}