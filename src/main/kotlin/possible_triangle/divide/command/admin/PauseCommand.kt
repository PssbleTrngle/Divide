package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import possible_triangle.divide.GameData
import possible_triangle.divide.events.Countdown

object PauseCommand {

    private val DISPLAY = Countdown("pause", "Paused")

    fun register(node: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack> {
        return node.then(
            literal("pause").requires { !GameData.DATA[it.server].paused }.executes(::pause)
        ).then(
            literal("resume").requires { GameData.DATA[it.server].paused }.executes(::resume)
        )
    }

    fun showDisplay(server: MinecraftServer) {
        val bar = DISPLAY.bar(server)
        bar.isVisible = true
        bar.players = server.playerList.players
        bar.max = 1
        bar.value = 1
        bar.color = BossEvent.BossBarColor.RED
        bar.overlay = BossEvent.BossBarOverlay.NOTCHED_20
        bar.setCreateWorldFog(true)
    }

    private fun pause(ctx: CommandContext<CommandSourceStack>): Int {
        GameData.setPaused(ctx.source.server, true)
        ctx.source.sendSuccess(TextComponent("Paused the game"), true)
        return 1
    }

    private fun resume(ctx: CommandContext<CommandSourceStack>): Int {
        GameData.setPaused(ctx.source.server, false)
        ctx.source.sendSuccess(TextComponent("Resumed the game"), true)
        DISPLAY.bar(ctx.source.server).isVisible = false
        return 1
    }

}