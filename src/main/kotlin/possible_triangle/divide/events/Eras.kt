package possible_triangle.divide.events

import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import possible_triangle.divide.Config
import possible_triangle.divide.GameData
import possible_triangle.divide.extensions.players
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat

object Eras : CycleEvent("eras") {

    override val enabled: Boolean
        get() = Config.CONFIG.eras.enabled

    override val startsAfter: Int
        get() = Config.CONFIG.eras.startAfter

    @Serializable
    private data class Event(val era: String)

    private val LOGGER = EventLogger(id, { Event.serializer() }) { always() }

    private fun isPeace(index: Int): Boolean {
        return index % 2 == 0
    }

    fun isPeace(server: MinecraftServer): Boolean {
        val data = GameData.DATA[server]
        if (data.paused || !data.started) return true
        val index = this.data[server]
        return index != null && isPeace(index)
    }

    private fun peace(server: MinecraftServer) {
        LOGGER.log(server, Event("peace"))

        server.players().forEach {
            Chat.subtitle(it, "Peace has begun", false)
            Chat.title(it, "❤")
        }

        val bar = bar(server)
        bar.name = Component.literal("Peace Era")
        bar.color = BossEvent.BossBarColor.GREEN
        bar.isVisible = Config.CONFIG.eras.showPeaceBar
    }

    private fun war(server: MinecraftServer) {
        LOGGER.log(server, Event("war"))

        server.players().forEach {
            Chat.subtitle(it, "War has started", false)
            Chat.title(it, "⚔")
        }

        val bar = bar(server)
        bar.name = Component.literal("War Era")
        bar.color = BossEvent.BossBarColor.RED
        bar.isVisible = Config.CONFIG.eras.showWarBar
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val peace = isPeace(index)
        val pause = if (peace) Config.CONFIG.eras.peaceTime else Config.CONFIG.eras.warTime

        if (peace) peace(server)
        else war(server)

        return pause.value
    }

}