package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import possible_triangle.divide.command.AdminCommand
import possible_triangle.divide.command.arguments.ResourceArgument
import possible_triangle.divide.data.DefaultedResource

object ResourceCommand {

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack>? {
        return base.then(
            literal("reload").executes(::reload)
        ).then(
            literal("reset").then(
                literal("resource")
                    .then(
                        argument("resource", StringArgumentType.string()).suggests(ResourceArgument.suggestions())
                            .executes(::resetResource)
                    )
            )
        )
    }

    private fun reload(ctx: CommandContext<CommandSourceStack>): Int {
        AdminCommand.reload(ctx.source.server)
        return 1
    }

    fun <T> resetResource(resource: DefaultedResource<T>): Int {
        return resource.defaults.onEach { (id, entry) -> resource.save(id, entry(), overwrite = true) }.count()
    }

    private fun resetResource(ctx: CommandContext<CommandSourceStack>): Int {
        val resource = ResourceArgument.getDefaultedResource(ctx, "resource")
        return resetResource(resource)
    }

}