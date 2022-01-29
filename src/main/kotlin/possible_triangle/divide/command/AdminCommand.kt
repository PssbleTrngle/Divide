package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.command.admin.*
import possible_triangle.divide.logging.EventLogger

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
        ResourceCommand.register(base)
        ActionCommand.register(base)

        base.then(literal("reset").then(literal("events")
            .executes { EventLogger.archive(it.source.server) }
        ))

        base.then(literal("publish").executes(::publish))

        event.dispatcher.register(base)
    }

    fun reload(server: MinecraftServer) {
        server.playerList.saveAll()
        server.playerList.reloadResources()
        server.playerList.players.forEach {
            server.playerList.sendPlayerPermissionLevel(it)
        }
    }

    private val ERROR_FAILED = SimpleCommandExceptionType(TranslatableComponent("commands.publish.failed"))
    private val ERROR_ALREADY_PUBLISHED =
        DynamicCommandExceptionType { TranslatableComponent("commands.publish.alreadyPublished", it) }

    private fun publish(ctx: CommandContext<CommandSourceStack>): Int {
        val port = 25565
        return if (ctx.source.server.isPublished) {
            throw ERROR_ALREADY_PUBLISHED.create(ctx.source.server.port)
        } else if (!ctx.source.server.publishServer(null, false, port)) {
            throw ERROR_FAILED.create()
        } else {
            ctx.source.sendSuccess(TranslatableComponent("commands.publish.success", port), true)
            port
        }
    }

}