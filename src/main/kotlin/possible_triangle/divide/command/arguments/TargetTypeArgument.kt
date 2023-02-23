package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.reward.ActionTarget

object TargetTypeArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { Text.literal("Target-Type $it not found") }

    fun getTargetType(ctx: CommandContext<ServerCommandSource>, name: String): ActionTarget<*> {
        val id = StringArgumentType.getString(ctx, name)
        return ActionTarget[id] ?: throw NOT_FOUND.create(id)
    }

    fun suggestions(): SuggestionProvider<ServerCommandSource> {
        return SuggestionProvider { _: CommandContext<ServerCommandSource>, suggestions: SuggestionsBuilder ->
            CommandSource.suggestMatching(ActionTarget.values.map {
                it.id
            }, suggestions)
        }
    }

}