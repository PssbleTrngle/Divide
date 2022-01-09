package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.command.EnumArgument
import possible_triangle.divide.logic.Scores
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object ShowCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("show")
                .then(
                    argument("what", EnumArgument.enumArgument(Scores.Extra::class.java))
                        .executes(::show)
                )
        )
    }

    private fun show(ctx: CommandContext<CommandSourceStack>): Int {
        val extra = ctx.getArgument("what", Scores.Extra::class.java)
        Scores.show(Teams.teamOf(ctx), extra)
        ctx.source.sendSuccess(TextComponent("${extra.name} are now visible on your teams scoreboard"), false)
        return 1
    }

}