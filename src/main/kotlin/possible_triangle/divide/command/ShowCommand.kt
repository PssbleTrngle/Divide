package possible_triangle.divide.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.info.ExtraInfo
import possible_triangle.divide.info.Scores

@Mod.EventBusSubscriber
object ShowCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("show")
                .then(
                    Commands.argument("type", StringArgumentType.string())
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
        ctx.source.sendSuccess(TextComponent("${extra.name} are now visible on your scoreboard"), false)
        return 1
    }

}