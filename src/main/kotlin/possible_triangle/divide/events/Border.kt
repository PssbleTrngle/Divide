package possible_triangle.divide.events

import net.minecraft.server.MinecraftServer
import possible_triangle.divide.Config
import possible_triangle.divide.logic.Chat

object Border : CycleEvent("border") {

    override fun isEnabled(server: MinecraftServer): Boolean {
        return  Config.CONFIG.border.enabled
    }

    private fun resize(server: MinecraftServer, size: Int, seconds: Int = 0, message: Boolean = true) {
        val worldborder = server.overworld().worldBorder
        if (worldborder.size == size.toDouble()) return
        worldborder.lerpSizeBetween(worldborder.size, size.toDouble(), 1000L * seconds)
        val verb = if (worldborder.size < size.toDouble()) "grow" else "shrink"
        if (message)
            server.playerList.players.forEach {
                Chat.subtitle(it, "border started to $verb")
            }
    }

    fun lobby(server: MinecraftServer) {
        resize(server, Config.CONFIG.border.lobbySize, message = false)
        server.overworld().worldBorder.damagePerBlock = 0.0
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val grow = index % 2 == 0
        val size = if (grow) Config.CONFIG.border.bigBorder else Config.CONFIG.border.smallBorder
        val pause = if (grow) Config.CONFIG.border.stayBigFor else Config.CONFIG.border.staySmallFor
        val moveTime = if (index > 0) Config.CONFIG.border.moveTime else 60

        server.overworld().worldBorder.damagePerBlock =  Config.CONFIG.border.damagePerBlock
        server.overworld().worldBorder.damageSafeZone = Config.CONFIG.border.damageSafeZone

        resize(server, size, moveTime, index > 0)

        bar(server).isVisible = Config.CONFIG.border.showBar

        return pause.value
    }

}