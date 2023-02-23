package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.info.ExtraInfo
import possible_triangle.divide.info.Scores

object ShowCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("show")
                .then(
                    argument("type", StringArgumentType.string())
                        .suggests { _, it ->
                            CommandSource.suggestMatching(
                                ExtraInfo.values().map { it.name.lowercase() }, it
                            )
                        }.executes(::show)
                )
        )
    }

    private fun show(ctx: CommandContext<ServerCommandSource>): Int {
        val extra = ExtraInfo.valueOf(StringArgumentType.getString(ctx, "type").uppercase())
        Scores.show(ctx.source.playerOrThrow, extra)
        ctx.source.sendFeedback(Text.literal("${extra.name} are now visible on your scoreboard"), false)
        return 1
    }

}