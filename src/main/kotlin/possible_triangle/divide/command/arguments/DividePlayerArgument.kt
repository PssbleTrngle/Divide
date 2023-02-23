package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import possible_triangle.divide.command.optionalPlayer
import possible_triangle.divide.command.optionalTeam
import possible_triangle.divide.logic.Teams.isParticipant
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants

object DividePlayerArgument {

    private val SAME_TEAM =
        DynamicCommandExceptionType { Text.literal("$it is in your own team") }
    private val NOT_A_PLAYER =
        DynamicCommandExceptionType { Text.literal("$it is not playing") }
    private val SELF = SimpleCommandExceptionType(Text.literal("Choose another player"))
    private val NOT_FOUND = SimpleCommandExceptionType(Text.literal("Player not found"))

    fun validate(
        player: ServerPlayerEntity?,
        user: ServerPlayerEntity?,
        ignoreSelf: Boolean = false,
        otherTeam: Boolean = false,
    ) {
        if (player == null) throw  NOT_FOUND.create()
        if (!player.isParticipant()) throw NOT_A_PLAYER.create(player.entityName)
        if (ignoreSelf && user?.uuid == player.uuid) throw SELF.create()
        if (otherTeam && user?.participantTeam() == player.scoreboardTeam) throw SAME_TEAM.create(player.name)
    }

    fun getPlayer(
        ctx: CommandContext<ServerCommandSource>,
        name: String,
    ): ServerPlayerEntity {
        val playerName = StringArgumentType.getString(ctx, name)
        return ctx.source.server.playerManager.getPlayer(playerName) ?: throw NOT_FOUND.create()
    }

    fun suggestions(ignoreSelf: Boolean = false, otherTeam: Boolean = false): SuggestionProvider<ServerCommandSource> {
        return SuggestionProvider { ctx: CommandContext<ServerCommandSource>, suggestions: SuggestionsBuilder ->
            val team = ctx.source.optionalTeam()
            val player = ctx.source.optionalPlayer()
            CommandSource.suggestMatching(
                ctx.source.server.participants()
                    .asSequence()
                    .filterNot { ignoreSelf && it.uuid == player?.uuid }
                    .filterNot { otherTeam && it.scoreboardTeam == team }
                    .map { it.entityName }
                    .toList(),
                suggestions
            )
        }
    }

}