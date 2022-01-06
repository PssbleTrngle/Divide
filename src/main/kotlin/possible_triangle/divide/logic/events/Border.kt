package possible_triangle.divide.logic.events

import net.minecraft.server.MinecraftServer
import possible_triangle.divide.Chat

object Border : CycleEvent("border") {

    private const val LOBBY = 20
    private const val MIN = 100
    private const val MAX = 400
    private const val CYCLE_TIME = 60 * 10

    private val QUEUE = listOf(MIN to CYCLE_TIME, MAX to CYCLE_TIME)

    private fun resize(server: MinecraftServer, size: Int, seconds: Int = 0, message: Boolean = true) {
        val worldborder = server.overworld().worldBorder
        if (worldborder.size == size.toDouble()) return
        worldborder.lerpSizeBetween(worldborder.size, size.toDouble(), 1000L * seconds)
        val verb = if (worldborder.size < size.toDouble()) "grow" else "shrink"
        if (message)
            server.playerList.players.forEach {
                Chat.message(it, "border started to $verb")
            }
    }

    fun lobby(server: MinecraftServer) {
        resize(server, LOBBY, message = false)
        server.overworld().worldBorder.damagePerBlock = 0.0
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val (size, pause) = QUEUE[index % QUEUE.size]
        resize(server, size, if (index > 0) 300 else 60, index > 0)
        return pause
    }

}