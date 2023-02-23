package possible_triangle.divide.command.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.scoreboard.Team
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import possible_triangle.divide.command.optionalTeam
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.logic.Teams.isParticipantTeam
import possible_triangle.divide.logic.Teams.participantTeam

object DivideTeamArgument {

    private val NOT_FOUND = DynamicCommandExceptionType { Text.translatable("team.notFound", it) }
    private val SAME_TEAM = SimpleCommandExceptionType(Text.literal("That's you own team"))

    fun validate(team: Team?, user: ServerPlayerEntity?, otherTeam: Boolean = false) {
        if(team == null) throw NOT_FOUND.create("???")
        if (otherTeam && user?.participantTeam() == team) throw SAME_TEAM.create()
    }

    fun getTeam(ctx: CommandContext<ServerCommandSource>, name: String): Team {
        val baseName = ctx.getArgument(name, String::class.java)
        val scoreboard = ctx.source.server.scoreboard
        return scoreboard.getPlayerTeam(Teams.TEAM_PREFIX + baseName)
            ?.takeIf { it.isParticipantTeam() }
            ?: throw NOT_FOUND.create(baseName)
    }


    fun suggestions(otherTeam: Boolean = false): SuggestionProvider<ServerCommandSource> {
        return SuggestionProvider { ctx: CommandContext<ServerCommandSource>, suggestions: SuggestionsBuilder ->
            val team = ctx.source.optionalTeam()
            CommandSource.suggestMatching(
                ctx.source.teamNames
                    .filter { isParticipantTeam(it) }
                    .filterNot { otherTeam && it == team?.name }
                    .map { it.substringAfterLast('_') },
                suggestions
            )
        }
    }

}