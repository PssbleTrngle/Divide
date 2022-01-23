package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.command.admin.*

@Mod.EventBusSubscriber
object AdminCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        val base = literal(DivideMod.ID).requires(Requirements::isAdmin)

        TeamCommand.register(base)
        CrateCommand.register(base)
        EventsCommand.register(base)
        ResetCommand.register(base)
        PauseCommand.register(base)

        base.then(literal("reload").executes(::reload))

        event.dispatcher.register(base)
    }

    fun reload(server: MinecraftServer) {
        server.playerList.saveAll()
        server.playerList.reloadResources()
        server.playerList.players.forEach {
            server.playerList.sendPlayerPermissionLevel(it)
        }
    }

    private fun reload(ctx: CommandContext<CommandSourceStack>): Int {
        reload(ctx.source.server)
        return 1
    }

}