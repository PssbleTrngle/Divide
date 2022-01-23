package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.hacks.DataHacker
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.hacks.PacketIntercepting
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object GlowCommand {

    private const val REASON_ID = "glow_own_team"

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("glow")
                .requires(Requirements::isPlayerInGame)
                .executes(::run)
        )
    }

    private fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val glowing = DataHacker.removeReason(ctx.source.server) { it.target == player.uuid && it.id == REASON_ID }
        if (glowing) PacketIntercepting.updateData(player, ctx.source.server)
        else DataHacker.addReason(GLOWING,player, Teams.teammates(player, false), 60 * 5, id = REASON_ID)

        ctx.source.sendSuccess(TextComponent("You are${if (glowing) " no longer " else " "}glowing"), false)

        return 1
    }

}