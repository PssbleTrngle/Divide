package possible_triangle.divide.events

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode
import net.minecraft.world.GameRules
import possible_triangle.divide.Config
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams.participants

object Border : CycleEvent("border") {

    override val enabled: Boolean
        get() = Config.CONFIG.border.enabled

    override val startsAfter: Int
        get() = Config.CONFIG.border.startAfter

    @Serializable
    private data class Event(val action: String)

    private val LOGGER = EventLogger(id, { Event.serializer() }) { always() }

    private fun resize(server: MinecraftServer, size: Int, seconds: Int = 0, message: Boolean = true) {
        val border = server.overworld.worldBorder
        if (border.size == size.toDouble()) return
        border.interpolateSize(border.size, size.toDouble(), 1000L * seconds)
        val verb = if (border.size < size.toDouble()) "grow" else "shrink"
        val color = if (border.size < size.toDouble()) Formatting.GREEN else Formatting.RED
        if (message) {
            LOGGER.log(server, Event(verb))
            server.playerManager.playerList.forEach {
                Chat.subtitle(it, Chat.apply("border started $verb", color))
            }
        }
    }

    fun lobby(server: MinecraftServer) {
        val world = server.overworld
        val worldborder = world.worldBorder

        val pos = (world.bottomY..world.topY).reversed()
            .map { BlockPos(worldborder.centerX.toInt(), it, worldborder.centerZ.toInt()) }
            .find { world.getBlockState(it).hasSolidTopSurface(world, it, null) }
            ?.up()

        server.participants().forEach {
            if (pos != null) it.teleport(
                world,
                worldborder.centerX,
                pos.y.toDouble(),
                worldborder.centerZ,
                it.headYaw,
                it.pitch
            )
            server.gameRules.get(GameRules.KEEP_INVENTORY).set(true, server)
            it.changeGameMode(GameMode.ADVENTURE)
        }

        resize(server, Config.CONFIG.border.lobbySize, message = false)
        worldborder.damagePerBlock = 0.0
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val grow = index % 2 == 0
        val size = if (grow) Config.CONFIG.border.bigBorder else Config.CONFIG.border.smallBorder
        val pause = if (grow) Config.CONFIG.border.stayBigFor else Config.CONFIG.border.staySmallFor
        val moveTime = if (index > 0) Config.CONFIG.border.moveTime else 60

        server.overworld.worldBorder.damagePerBlock = Config.CONFIG.border.damagePerBlock
        server.overworld.worldBorder.safeZone = Config.CONFIG.border.damageSafeZone

        resize(server, size, moveTime, index > 0)
        bar(server).isVisible = Config.CONFIG.border.showBar

        return pause.value
    }

}