package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.api.ServerApi

@Mod.EventBusSubscriber
object WebCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(literal("web").executes(::login))
    }

    private fun login(ctx: CommandContext<CommandSourceStack>): Int {
        val token = ServerApi.createToken(ctx.source.playerOrException)

        ctx.source.sendSuccess(
            TextComponent("[open web view]").withStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "${Config.CONFIG.api.host}?token=${token}"))
                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent("click to open web view")))
                    .withColor(ChatFormatting.AQUA)
                    .withUnderlined(true)
            ), false
        )

        return 1
    }

}