package possible_triangle.divide

import io.github.fabricators_of_create.porting_lib.event.common.PlayerTickEvents
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import net.minecraft.world.GameRules
import possible_triangle.divide.command.AdminCommand
import possible_triangle.divide.command.admin.PauseCommand
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent.Companion.EVENTS
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.reward.SecretRewards

data class GameData(val paused: Boolean, val started: Boolean) {

    @Serializable
    private data class Event(val action: String)

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

            server.gameRules.get(GameRules.KEEP_INVENTORY).set(!boolean, server)

            if (boolean) {

                if (Config.CONFIG.secretRewards) SecretRewards.choose(server)

                server.participants().forEach { player ->
                    player.changeGameMode(GameMode.SURVIVAL)
                    DeathEvents.startedGear(player).forEach { player.giveItemStack(it) }
                    Chat.subtitle(player, "Started")
                }

                EVENTS.forEach { it.startCycle(server) }

            } else {

                EVENTS.forEach { it.stop(server) }
                Border.lobby(server)

            }
        }

        val DATA = object : ModSavedData<GameData>("_gamedata") {

            override fun save(nbt: NbtCompound, value: GameData) {
                nbt.putBoolean("paused", value.paused)
                nbt.putBoolean("started", value.started)
            }

            override fun load(nbt: NbtCompound, server: MinecraftServer): GameData {
                val paused = nbt.getBoolean("paused")
                val started = nbt.getBoolean("started")
                return GameData(paused || (started && Config.CONFIG.autoPause), started)
            }

            override fun default(): GameData {
                return GameData(paused = false, started = false)
            }
        }

        private val LOBBY_EFFECTS = listOf(StatusEffects.SATURATION, StatusEffects.RESISTANCE)
            .map { StatusEffectInstance(it, 20 * 5, 100, false, false) }

        init {
            PlayerTickEvents.START.register { player ->
                if(player !is ServerPlayerEntity) return@register
                if (!DATA[player.server].started) {
                    LOBBY_EFFECTS.forEach(player::addStatusEffect)
                }
            }

            ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
                handler.player.unlockRecipes(server.recipeManager.values())
                handler.player.changeGameMode(if (DATA[server].started) GameMode.SURVIVAL else GameMode.ADVENTURE)
                handler.player.networkHandler.sendPacket(TitleFadeS2CPacket(5, 20, 5))
            }
        }
    }
}