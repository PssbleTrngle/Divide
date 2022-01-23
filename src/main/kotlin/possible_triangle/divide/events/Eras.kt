package possible_triangle.divide.events

import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import possible_triangle.divide.Config
import possible_triangle.divide.GameData
import possible_triangle.divide.logic.Chat

object Eras : CycleEvent("eras") {

    private fun isPeace(index: Int): Boolean {
        return index % 2 == 0
    }

    fun isPeace(server: MinecraftServer): Boolean {
        val data = GameData.DATA[server]
        if (data.paused || !data.started) return true
        val index = this.data[server]
        return index != null && isPeace(index)
    }

    override fun isEnabled(server: MinecraftServer): Boolean {
        return Config.CONFIG.eras.enabled
    }

    private fun peace(server: MinecraftServer) {
        server.playerList.players.forEach {
            Chat.subtitle(it, "Peace has begun", false)
            Chat.title(it, "❤")
        }

        val bar = bar(server)
        bar.name = TextComponent("Peace Era")
        bar.color = BossEvent.BossBarColor.GREEN
        bar.isVisible = Config.CONFIG.eras.showPeaceBar
    }

    private fun war(server: MinecraftServer) {
        server.playerList.players.forEach {
            Chat.subtitle(it, "War has started", false)
            Chat.title(it, "⚔")
        }

        val bar = bar(server)
        bar.name = TextComponent("War Era")
        bar.color = BossEvent.BossBarColor.RED
        bar.isVisible = Config.CONFIG.eras.showWarBar
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val peace = isPeace(index)
        val pause = if (peace) Config.CONFIG.eras.peaceTime else Config.CONFIG.eras.warTime

        if (index >= Config.CONFIG.eras.startAt) {
            if (peace) peace(server)
            else war(server)
        }

        return pause.value
    }

}