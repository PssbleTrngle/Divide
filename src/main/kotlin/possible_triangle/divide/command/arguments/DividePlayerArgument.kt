package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.command.optionalPlayer
import possible_triangle.divide.command.optionalTeam
import possible_triangle.divide.logic.Teams.isParticipant
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants

object DividePlayerArgument {

    private val SAME_TEAM =
        DynamicCommandExceptionType { Component.literal("$it is in your own team") }
    private val NOT_A_PLAYER =
        DynamicCommandExceptionType { Component.literal("$it is not playing") }
    private val SELF = SimpleCommandExceptionType(Component.literal("Choose another player"))
    private val NOT_FOUND = SimpleCommandExceptionType(Component.literal("Player not found"))

    fun validate(
        player: ServerPlayer?,
        user: ServerPlayer?,
        ignoreSelf: Boolean = false,
        otherTeam: Boolean = false,
    ) {
        if (player == null) throw  NOT_FOUND.create()
        if (!player.isParticipant()) throw NOT_A_PLAYER.create(player.scoreboardName)
        if (ignoreSelf && user?.uuid == player.uuid) throw SELF.create()
        if (otherTeam && user?.participantTeam() == player.team) throw SAME_TEAM.create(player.name)
    }

    fun getPlayer(
        ctx: CommandContext<CommandSourceStack>,
        name: String,
    ): ServerPlayer {
        val playerName = StringArgumentType.getString(ctx, name)
        return ctx.source.server.playerList.getPlayerByName(playerName) ?: throw NOT_FOUND.create()
    }

    fun suggestions(ignoreSelf: Boolean = false, otherTeam: Boolean = false): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { ctx: CommandContext<CommandSourceStack>, suggestions: SuggestionsBuilder ->
            val team = ctx.source.optionalTeam()
            val player = ctx.source.optionalPlayer()
            SharedSuggestionProvider.suggest(
                ctx.source.server.participants()
                    .asSequence()
                    .filterNot { ignoreSelf && it.uuid == player?.uuid }
                    .filterNot { otherTeam && it.team == team }
                    .map { it.scoreboardName }
                    .toList(),
                suggestions
            )
        }
    }

}