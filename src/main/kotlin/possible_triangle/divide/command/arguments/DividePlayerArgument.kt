package possible_triangle.divide.command.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.command.Requirements
import possible_triangle.divide.logic.Teams

object DividePlayerArgument {

    private val SAME_TEAM =
        DynamicCommandExceptionType { TextComponent("$it is in your own team") }
    private val NOT_A_PLAYER =
        DynamicCommandExceptionType { TextComponent("$it is not playing") }
    private val SELF = SimpleCommandExceptionType(TextComponent("Choose another player"))
    private val NOT_FOUND = SimpleCommandExceptionType(TextComponent("Player not found"))

    fun validate(
        player: ServerPlayer?,
        user: ServerPlayer?,
        ignoreSelf: Boolean = false,
        otherTeam: Boolean = false,
    ) {
        if (player == null) throw  NOT_FOUND.create()
        if (!Teams.isPlayer(player)) throw NOT_A_PLAYER.create(player.scoreboardName)
        if (ignoreSelf && user?.uuid == player.uuid) throw SELF.create()
        if (otherTeam && user?.let { Teams.teamOf(it) }?.name == player.team?.name) throw SAME_TEAM.create(player.name)
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
            val team = Requirements.optionalTeam(ctx.source)
            val player = Requirements.optionalPlayer(ctx.source)
            val server = ctx.source.server
            SharedSuggestionProvider.suggest(
                ctx.source.onlinePlayerNames
                    .asSequence()
                    .mapNotNull { server.playerList.getPlayerByName(it) }
                    .filter { Teams.isPlayer(it) }
                    .filterNot { ignoreSelf && it.uuid == player?.uuid }
                    .filterNot { otherTeam && it.team?.name == team?.name }
                    .map { it.scoreboardName }
                    .toList(),
                suggestions
            )
        }
    }

}