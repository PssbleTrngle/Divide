package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.DivideMod
import possible_triangle.divide.command.admin.*
import possible_triangle.divide.logging.EventLogger

object AdminCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
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
        server.playerManager.saveAllPlayerData()
        server.playerManager.onDataPacksReloaded()
        server.playerManager.playerList.forEach {
            //server.playerManager.sendPlayerPermissionLevel(it)
        }
    }

    private val ERROR_FAILED = SimpleCommandExceptionType(Text.translatable("commands.publish.failed"))
    private val ERROR_ALREADY_PUBLISHED =
        DynamicCommandExceptionType { Text.translatable("commands.publish.alreadyPublished", it) }

}