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
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.TeamLogic

@Mod.EventBusSubscriber
object BuyCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("buy").then(
                argument("reward", EnumArgument.enumArgument(Reward::class.java))
                    .executes(::buyReward)
            )
        )
    }

    private fun buyReward(ctx: CommandContext<CommandSourceStack>): Int {
        val reward = ctx.getArgument("reward", Reward::class.java)
        if (reward.buy(ctx.source.level, TeamLogic.teamOf(ctx))) {
            ctx.source?.sendSuccess(TextComponent("Bought ${reward.display} for ${reward.price}"), false)
        } else {
            throw CashCommand.NOT_ENOUGH.create()
        }
        return 1
    }

}