package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.logic.Scores
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object ShowCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Scores.Extra.values().fold(
                literal("show"),
            ) { node, extra ->
                node.then(literal(extra.name.lowercase()).executes { show(it, extra) })
            }
        )
    }

    private fun show(ctx: CommandContext<CommandSourceStack>, extra: Scores.Extra): Int {
        Scores.show(Teams.teamOf(ctx), extra)
        ctx.source.sendSuccess(TextComponent("${extra.name} are now visible on your teams scoreboard"), false)
        return 1
    }

}