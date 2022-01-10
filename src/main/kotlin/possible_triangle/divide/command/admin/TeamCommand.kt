package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.ColorArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent

object TeamCommand {

    private val TEAM_ALREADY_EXISTS =
        SimpleCommandExceptionType(TranslatableComponent("commands.team.add.duplicate"))

    fun register(node: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack> {
        return node.then(
            literal("team")
                .then(
                    literal("create").then(
                        argument("name", StringArgumentType.string()).then(
                            argument(
                                "color",
                                ColorArgument.color()
                            ).executes(::createTeam)
                        )
                    )
                )
                .then(
                    literal("join").then(
                        argument("team", TeamArgument.team()).then(
                            argument(
                                "players",
                                EntityArgument.players()
                            ).executes(::addToTeam)
                        )
                    )
                )
        )
    }

    private fun addToTeam(ctx: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(ctx, "team")
        EntityArgument.getPlayers(ctx, "players").forEach {
            it.tags.remove("spectator")
            ctx.source.server.scoreboard.addPlayerToTeam(it.scoreboardName, team)
        }
        return 1
    }

    private fun createTeam(ctx: CommandContext<CommandSourceStack>): Int {
        val scoreboard = ctx.source.server.scoreboard

        val name = StringArgumentType.getString(ctx, "name")
        val color = ColorArgument.getColor(ctx, "color")
        val id = name.replace("\\s+".toRegex(), "_").lowercase()

        if (scoreboard.getPlayerTeam(id) != null) throw TEAM_ALREADY_EXISTS.create()
        if (scoreboard.playerTeams.any { it.color == color }) throw TEAM_ALREADY_EXISTS.create()

        val team = scoreboard.addPlayerTeam(id)
        team.displayName = TextComponent(name)
        team.isAllowFriendlyFire = false
        team.setSeeFriendlyInvisibles(true)
        team.color = color

        ctx.source.sendSuccess(TranslatableComponent("commands.team.add.success", team.formattedDisplayName), true)
        return scoreboard.playerTeams.size
    }

}