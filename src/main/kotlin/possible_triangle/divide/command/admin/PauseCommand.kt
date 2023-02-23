package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.boss.BossBar
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.GameData
import possible_triangle.divide.events.Countdown

object PauseCommand {

    private val DISPLAY = Countdown("pause", "Paused")

    fun register(node: LiteralArgumentBuilder<ServerCommandSource>): LiteralArgumentBuilder<ServerCommandSource> {
        return node.then(
            literal("pause").requires { !GameData.DATA[it.server].paused }.executes(::pause)
        ).then(
            literal("resume").requires { GameData.DATA[it.server].paused }.executes(::resume)
        )
    }

    fun showDisplay(server: MinecraftServer) {
        val bar = DISPLAY.bar(server)
        bar.isVisible = true
        bar.addPlayers(server.playerManager.playerList)
        bar.maxValue = 1
        bar.value = 1
        bar.color = BossBar.Color.RED
        bar.style = BossBar.Style.NOTCHED_20
        bar.setThickenFog(true)
    }

    private fun pause(ctx: CommandContext<ServerCommandSource>): Int {
        GameData.setPaused(ctx.source.server, true)
        ctx.source.sendFeedback(Text.literal("Paused the game"), true)
        return 1
    }

    private fun resume(ctx: CommandContext<ServerCommandSource>): Int {
        GameData.setPaused(ctx.source.server, false)
        ctx.source.sendFeedback(Text.literal("Resumed the game"), true)
        DISPLAY.bar(ctx.source.server).isVisible = false
        return 1
    }

}