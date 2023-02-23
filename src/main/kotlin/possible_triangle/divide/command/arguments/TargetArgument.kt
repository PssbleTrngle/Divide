package possible_triangle.divide.command.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.reward.ActionTarget
import java.util.stream.Stream

object TargetArgument {

    private val TARGET_REQUIRED = DynamicCommandExceptionType { Text.literal("Target of type $it required") }

    fun <T> getTarget(ctx: CommandContext<ServerCommandSource>, name: String, targetType: ActionTarget<T>): T {
        val requiresTarget = targetType.suggestions() != null
        return try {
            targetType.fromContext(ctx, name)
        }  catch (e: IllegalArgumentException)  {
            throw TARGET_REQUIRED.create(targetType.id)
        }
    }

    fun suggestions(): SuggestionProvider<ServerCommandSource> {
        return SuggestionProvider { ctx: CommandContext<ServerCommandSource>, suggestions: SuggestionsBuilder ->
            val reward = RewardArgument.getReward(ctx, "reward")
            reward.target.suggestions()?.getSuggestions(ctx, suggestions)
                ?: CommandSource.suggestMatching(Stream.empty(), suggestions)
        }
    }

}