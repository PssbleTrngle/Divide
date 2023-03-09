package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.hacks.PacketIntercepting
import possible_triangle.divide.logic.Teams.teammates
import kotlin.time.Duration.Companion.minutes

object GlowCommand {

    private const val REASON_ID = "glow_own_team"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("glow")
                .requires { it.isActiveParticipant() }
                .executes(::run)
        )
    }

    private fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val glowing = DataHacker.removeReason(ctx.source.server) { it.target == player.uuid && it.id == REASON_ID }
        if (glowing) PacketIntercepting.updateData(player, ctx.source.server)
        if (!glowing) DataHacker.addReason(GLOWING, player, player.teammates(true), 5.minutes, id = REASON_ID)

        ctx.source.sendSuccess(Component.literal("You are${if (glowing) " no longer " else " "}glowing"), false)

        return 1
    }

}