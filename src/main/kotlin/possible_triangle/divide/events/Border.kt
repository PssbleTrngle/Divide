package possible_triangle.divide.events

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import possible_triangle.divide.Config
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams

object Border : CycleEvent("border") {

    override fun isEnabled(server: MinecraftServer): Boolean {
        return Config.CONFIG.border.enabled
    }

    private fun resize(server: MinecraftServer, size: Int, seconds: Int = 0, message: Boolean = true) {
        val worldborder = server.overworld().worldBorder
        if (worldborder.size == size.toDouble()) return
        worldborder.lerpSizeBetween(worldborder.size, size.toDouble(), 1000L * seconds)
        val verb = if (worldborder.size < size.toDouble()) "grow" else "shrink"
        val color = if (worldborder.size < size.toDouble()) ChatFormatting.GREEN else ChatFormatting.RED
        if (message)
            server.playerList.players.forEach {
                Chat.subtitle(it, Chat.apply("border started to $verb", color))
            }
    }

    fun lobby(server: MinecraftServer) {
        val world = server.overworld()
        val worldborder = world.worldBorder

        val pos = (world.minBuildHeight..world.maxBuildHeight).reversed()
            .map { BlockPos(worldborder.centerX.toInt(), it, worldborder.centerZ.toInt()) }
            .find { world.getBlockState(it).isFaceSturdy(world, it, Direction.UP) }
            ?.above()

        Teams.players(server).forEach {
            if (pos != null) it.teleportTo(
                world,
                worldborder.centerX,
                pos.y.toDouble(),
                worldborder.centerZ,
                it.yRot,
                it.xRot
            )
            server.gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server)
            it.setGameMode(GameType.ADVENTURE)
        }

        resize(server, Config.CONFIG.border.lobbySize, message = false)
        worldborder.damagePerBlock = 0.0
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val grow = index % 2 == 0
        val size = if (grow) Config.CONFIG.border.bigBorder else Config.CONFIG.border.smallBorder
        val pause = if (grow) Config.CONFIG.border.stayBigFor else Config.CONFIG.border.staySmallFor
        val moveTime = if (index > 0) Config.CONFIG.border.moveTime else 60

        if (index >= Config.CONFIG.border.startAt) {
            server.overworld().worldBorder.damagePerBlock = Config.CONFIG.border.damagePerBlock
            server.overworld().worldBorder.damageSafeZone = Config.CONFIG.border.damageSafeZone

            resize(server, size, moveTime, index > 0)
            bar(server).isVisible = Config.CONFIG.border.showBar
        }

        return pause.value
    }

}