package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import possible_triangle.divide.command.optionalTeam
import possible_triangle.divide.reward.Reward

object RewardArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { Text.literal("Reward $it not found") }

    fun getReward(ctx: CommandContext<ServerCommandSource>, name: String, ignoreVisibility: Boolean = false): Reward {
        val id = StringArgumentType.getString(ctx, name)
        val team = ctx.source.optionalTeam()
        return Reward[id]?.takeIf { ignoreVisibility || Reward.isVisible(it, team, ctx.source.server) }
            ?: throw NOT_FOUND.create(id)
    }

    fun suggestions(ignoreVisibility: Boolean = false): SuggestionProvider<ServerCommandSource> {
        return SuggestionProvider { ctx: CommandContext<ServerCommandSource>, suggestions: SuggestionsBuilder ->
            val team = ctx.source.optionalTeam()
            CommandSource.suggestMatching(
                Reward.values
                    .filter { ignoreVisibility || Reward.isVisible(it, team, ctx.source.server) }
                    .map { it.id },
                suggestions
            )
        }
    }

}