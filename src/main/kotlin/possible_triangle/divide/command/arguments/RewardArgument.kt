package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import possible_triangle.divide.command.optionalTeam
import possible_triangle.divide.reward.Reward

object RewardArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { Component.literal("Reward $it not found") }

    fun getReward(ctx: CommandContext<CommandSourceStack>, name: String, ignoreVisibility: Boolean = false): Reward {
        val id = StringArgumentType.getString(ctx, name)
        val team = ctx.source.optionalTeam()
        return Reward[id]?.takeIf { ignoreVisibility || Reward.isVisible(it, team, ctx.source.server) }
            ?: throw NOT_FOUND.create(id)
    }

    fun suggestions(ignoreVisibility: Boolean = false): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { ctx: CommandContext<CommandSourceStack>, suggestions: SuggestionsBuilder ->
            val team = ctx.source.optionalTeam()
            SharedSuggestionProvider.suggest(
                Reward.values
                    .filter { ignoreVisibility || Reward.isVisible(it, team, ctx.source.server) }
                    .map { it.id },
                suggestions
            )
        }
    }

}