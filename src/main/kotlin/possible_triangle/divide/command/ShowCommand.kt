package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import possible_triangle.divide.info.ExtraInfo
import possible_triangle.divide.info.Scores

object ShowCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("show")
                .then(
                    argument("type", StringArgumentType.string())
                        .suggests { _, it ->
                            SharedSuggestionProvider.suggest(
                                ExtraInfo.values().map { it.name.lowercase() }, it
                            )
                        }.executes(::show)
                )
        )
    }

    private fun show(ctx: CommandContext<CommandSourceStack>): Int {
        val extra = ExtraInfo.valueOf(StringArgumentType.getString(ctx, "type").uppercase())
        Scores.show(ctx.source.playerOrException, extra)
        ctx.source.sendSuccess(Component.literal("${extra.name} are now visible on your scoreboard"), false)
        return 1
    }

}