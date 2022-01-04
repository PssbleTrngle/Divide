package possible_triangle.divide.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.ColorArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.level.GameType
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Chat
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.Border
import possible_triangle.divide.logic.DeathLogic
import possible_triangle.divide.logic.TeamLogic

@Mod.EventBusSubscriber
object AdminCommand {

    private val TEAM_ALREADY_EXISTS =
        SimpleCommandExceptionType(TranslatableComponent("commands.team.add.duplicate"))


    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal(DivideMod.ID)
                .requires(TeamLogic::isAdmin)
                .then(
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
                .then(literal("center").executes(::center))
                .then(literal("start").executes(::start))
        )
    }

    private fun center(ctx: CommandContext<CommandSourceStack>): Int {
        val worldborder = ctx.source.server.overworld().worldBorder
        val pos = ctx.source.position
        worldborder.setCenter(pos.x, pos.z)
        Border.lobby(ctx.source.server)

        TeamLogic.players(ctx.source.level).filter { ctx.source.entity != it }.forEach {
            it.teleportTo(ctx.source.level, pos.x, pos.y, pos.z, it.yRot, it.xRot)
            ctx.source.level.setDefaultSpawnPos(BlockPos(pos), 0F)
        }

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>): Int {
        Border.startCycle(ctx.source.server)

        TeamLogic.players(ctx.source.level).forEach { player ->
            player.setGameMode(GameType.SURVIVAL)
            DeathLogic.starterGear(player).forEach { player.addItem(it) }
            Chat.subtitle(player, "Started")
        }

        return 1
    }

    private fun addToTeam(ctx: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(ctx, "team")
        EntityArgument.getPlayers(ctx, "players").forEach {
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