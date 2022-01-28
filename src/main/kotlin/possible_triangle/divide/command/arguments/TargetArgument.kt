package possible_triangle.divide.command.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.TextComponent
import possible_triangle.divide.reward.ActionTarget
import java.util.stream.Stream

object TargetArgument {

    private val TARGET_REQUIRED = DynamicCommandExceptionType { TextComponent("Target of type $it required") }

    fun <T> getTarget(ctx: CommandContext<CommandSourceStack>, name: String, targetType: ActionTarget<T>): T {
        val requiresTarget = targetType.suggestions() != null
        return try {
            targetType.fromContext(ctx, name)
        }  catch (e: IllegalArgumentException)  {
            throw TARGET_REQUIRED.create(targetType.id)
        }
    }

    fun suggestions(): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { ctx: CommandContext<CommandSourceStack>, suggestions: SuggestionsBuilder ->
            val reward = RewardArgument.getReward(ctx, "reward")
            reward.target.suggestions()?.getSuggestions(ctx, suggestions)
                ?: SharedSuggestionProvider.suggest(Stream.empty(), suggestions)
        }
    }

}