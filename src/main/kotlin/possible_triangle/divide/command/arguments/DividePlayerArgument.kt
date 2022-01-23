package possible_triangle.divide.command.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.command.Requirements
import possible_triangle.divide.logic.Teams

object DividePlayerArgument {

    private val SAME_TEAM =
        DynamicCommandExceptionType { (it as MutableComponent).append(TextComponent(" is in your own team")) }
    private val NOT_A_PLAYER =
        DynamicCommandExceptionType { (it as MutableComponent).append(TextComponent(" is not playing")) }
    private val SELF = SimpleCommandExceptionType(TextComponent("Choose another player"))

    fun getPlayer(
        ctx: CommandContext<CommandSourceStack>,
        name: String,
        ignoreSelf: Boolean = false,
        otherTeam: Boolean = false
    ): ServerPlayer {
        val player = EntityArgument.getPlayer(ctx, name)
        if (!Teams.isPlayer(player)) throw NOT_A_PLAYER.create(player.name)
        if (ignoreSelf && Requirements.optionalPlayer(ctx.source)?.uuid == player.uuid) throw SELF.create()
        if (otherTeam && Requirements.optionalTeam(ctx.source)?.name == player.team?.name) throw SAME_TEAM.create(player.name)
        return player
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