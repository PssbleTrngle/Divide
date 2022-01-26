package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.TextComponent
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.ReloadedResource

object ResourceArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { TextComponent("Resource $it not found") }

    fun getDefaultedResource(ctx: CommandContext<CommandSourceStack>, name: String): DefaultedResource<*> {
        val id = StringArgumentType.getString(ctx, name)
        val resource = ReloadedResource[id]
        if (resource is DefaultedResource) return resource
        throw NOT_FOUND.create(id)
    }

    fun suggestions(): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { _: CommandContext<CommandSourceStack>, suggestions: SuggestionsBuilder ->
            SharedSuggestionProvider.suggest(
                ReloadedResource.values.filterIsInstance(DefaultedResource::class.java).map { it.resourceID },
                suggestions
            )
        }
    }

}