package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import possible_triangle.divide.reward.ActionTarget

object TargetTypeArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { Component.literal("Target-Type $it not found") }

    fun getTargetType(ctx: CommandContext<CommandSourceStack>, name: String): ActionTarget<*> {
        val id = StringArgumentType.getString(ctx, name)
        return ActionTarget[id] ?: throw NOT_FOUND.create(id)
    }

    fun suggestions(): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { _: CommandContext<CommandSourceStack>, suggestions: SuggestionsBuilder ->
            SharedSuggestionProvider.suggest(ActionTarget.values.map {
                it.id
            }, suggestions)
        }
    }

}