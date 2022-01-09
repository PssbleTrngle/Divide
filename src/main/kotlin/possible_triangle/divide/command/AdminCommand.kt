package possible_triangle.divide.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.ColorArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.level.GameType
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateEvent
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.events.Eras
import possible_triangle.divide.events.PlayerBounty
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object AdminCommand {

    private val TEAM_ALREADY_EXISTS =
        SimpleCommandExceptionType(TranslatableComponent("commands.team.add.duplicate"))

    private val NO_CRATE_POS = DynamicCommandExceptionType { TextComponent("Could no find a valid pos around $it") }

    private val EVENTS = listOf(Border, Eras, CrateEvent, PlayerBounty)

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal(DivideMod.ID)
                .requires(Teams::isAdmin)
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
                .then(
                    literal("crate").then(
                        literal("spawn").then(
                            argument("pos", BlockPosArgument.blockPos())
                                .then(argument("in", TimeArgument.time()).executes(::spawnCrate))
                                .executes(::spawnCrate)
                        )
                    )
                )
                .then(literal("center").executes(::center))
                .then(literal("start").executes(::start))
                .then(literal("stop").executes(::stop))
                .then(EVENTS.fold(literal("skip")) { node, cycleEvent ->
                    node.then(literal(cycleEvent.id).executes {
                        skip(
                            it,
                            cycleEvent
                        )
                    })
                })
        )
    }

    private fun spawnCrate(ctx: CommandContext<CommandSourceStack>): Int {

        val center = BlockPosArgument.getSpawnablePos(ctx, "pos")
        val pos = CrateScheduler.findInRange(ctx.source.server, center, 10.0) ?: throw NO_CRATE_POS.create(center)

        val timeTicks = try {
            IntegerArgumentType.getInteger(ctx, "in")
        } catch (e: IllegalArgumentException) {
            0
        }

        val seconds = timeTicks / 20
        CrateScheduler.schedule(ctx.source.server, seconds, pos)

        ctx.source.sendSuccess(
            TextComponent(
                if (seconds == 0) "Crate delivered to $pos"
                else "Crate will be delivered to $pos in $seconds seconds"
            ), false
        )

        return seconds
    }

    private fun center(ctx: CommandContext<CommandSourceStack>): Int {
        val worldborder = ctx.source.server.overworld().worldBorder
        val pos = ctx.source.position
        worldborder.setCenter(pos.x, pos.z)
        Border.lobby(ctx.source.server)

        Teams.players(ctx.source.server).filter { ctx.source.entity != it }.forEach {
            it.teleportTo(ctx.source.level, pos.x, pos.y, pos.z, it.yRot, it.xRot)
            ctx.source.level.setDefaultSpawnPos(BlockPos(pos), 0F)
        }

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>): Int {
        EVENTS.forEach {
            it.startCycle(ctx.source.server)
        }

        Teams.players(ctx.source.server).forEach { player ->
            player.setGameMode(GameType.SURVIVAL)
            DeathEvents.starterGear(player).forEach { player.addItem(it) }
            Chat.subtitle(player, "Started")
        }

        return 1
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>): Int {
        EVENTS.forEach {
            it.stop(ctx.source.server)
        }
        return 1
    }

    private fun skip(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.skip(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("Skipped ${event.id}"), true)
        return 1
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