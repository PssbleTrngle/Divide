package possible_triangle.divide.command.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import possible_triangle.divide.command.Requirements
import possible_triangle.divide.logic.Teams

object DivideTeamArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { TranslatableComponent("team.notFound", it) }
    private val SAME_TEAM = SimpleCommandExceptionType(TextComponent("That's you own team"))

    fun validate(team: PlayerTeam?, user: ServerPlayer?, otherTeam: Boolean = false) {
        if(team == null) throw NOT_FOUND.create("???")
        if (otherTeam && user?.let { Teams.teamOf(user) }?.name == team.name) throw SAME_TEAM.create()
    }

    fun getTeam(ctx: CommandContext<CommandSourceStack>, name: String): PlayerTeam {
        val baseName = ctx.getArgument(name, String::class.java)
        val scoreboard: Scoreboard = ctx.source.server.scoreboard
        return scoreboard.getPlayerTeam(Teams.TEAM_PREFIX + baseName)?.takeIf { Teams.isPlayingTeam(it) }
            ?: throw NOT_FOUND.create(baseName)
    }


    fun suggestions(otherTeam: Boolean = false): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { ctx: CommandContext<CommandSourceStack>, suggestions: SuggestionsBuilder ->
            val team = Requirements.optionalTeam(ctx.source)
            SharedSuggestionProvider.suggest(
                ctx.source.allTeams
                    .filter { Teams.isPlayingTeam(it) }
                    .filterNot { otherTeam && it == team?.name }
                    .map { it.substringAfterLast('_') },
                suggestions
            )
        }
    }

}