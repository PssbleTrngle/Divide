package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.DivideMod
import possible_triangle.divide.command.admin.*
import possible_triangle.divide.extensions.players
import possible_triangle.divide.logging.EventLogger

object AdminCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val base = literal(DivideMod.ID).requires { it.isAdmin() }

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

        dispatcher.register(base)
    }

    fun reload(server: MinecraftServer) {
        server.playerList.saveAll()
        server.playerList.reloadResources()
        server.players().forEach {
            server.playerList.sendPlayerPermissionLevel(it)
        }
    }

    private val ERROR_FAILED = SimpleCommandExceptionType(Component.translatable("commands.publish.failed"))
    private val ERROR_ALREADY_PUBLISHED =
        DynamicCommandExceptionType { Component.translatable("commands.publish.alreadyPublished", it) }

}