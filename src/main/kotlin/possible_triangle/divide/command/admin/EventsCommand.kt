package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import possible_triangle.divide.crates.CrateEvent
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.events.Eras
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.Teams

object EventsCommand {

    private val EVENTS = listOf(Border, Eras, CrateEvent, PlayerBountyEvent)

    private val PLAYERS_MISSING_TEAM = DynamicCommandExceptionType {
        val players = it as List<ServerPlayer>
        val names = players.map { it.displayName as MutableComponent }.reduce { component, name ->
            component.append(TextComponent(", ")).append(name)
        }
        TextComponent("${players.size} players are missing not yet in a team: ").append(names)
    }

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack> {
        return base.then(literal("center").executes(::center))
            .then(EVENTS.fold(literal("start").executes(::start)) { node, event ->
                node.then(literal(event.id).executes {
                    start(it, event)
                })
            })
            .then(EVENTS.fold(literal("stop").executes(::stop)) { node, event ->
                node.then(literal(event.id).executes {
                    stop(it, event)
                })
            })
            .then(EVENTS.fold(literal("skip")) { node, event ->
                node.then(literal(event.id).executes {
                    skip(it, event)
                })
            })
            .then(literal("fix").executes(::fix))
    }

    private fun fix(ctx: CommandContext<CommandSourceStack>): Int {
        val missing = EVENTS.filterNot { it.isRunning(ctx.source.server) }

        if (missing.isNotEmpty()) {
            missing.forEach { it.startCycle(ctx.source.server) }
            ctx.source.sendSuccess(TextComponent("Started ${missing.size} missing events"), true)
        } else {
            ctx.source.sendFailure(TextComponent("No event cycle missing"))
        }

        return missing.size
    }

    private fun center(ctx: CommandContext<CommandSourceStack>): Int {
        val worldborder = ctx.source.server.overworld().worldBorder
        val pos = ctx.source.position
        worldborder.setCenter(pos.x, pos.z)
        ctx.source.level.setDefaultSpawnPos(BlockPos(pos), 0F)
        Border.lobby(ctx.source.server)

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>): Int {

        val noTeam = Teams.players(ctx.source.server).filter { it.team == null }
        if (noTeam.isNotEmpty()) throw PLAYERS_MISSING_TEAM.create(noTeam)

        EVENTS.forEach { it.startCycle(ctx.source.server) }
        ctx.source.server.gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(false, ctx.source.server)

        Teams.players(ctx.source.server).forEach { player ->
            player.setGameMode(GameType.SURVIVAL)
            DeathEvents.starterGear(player).forEach { player.addItem(it) }
            Chat.subtitle(player, "Started")
        }

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        val restarted = event.startCycle(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("${if (restarted) "Restarted" else "Started"} ${event.id}"), true)
        return 1
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.stop(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("Stopped ${event.id}"), true)
        return 1
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>): Int {
        EVENTS.forEach { it.stop(ctx.source.server) }
        Border.lobby(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("Stopped ${EVENTS.size} events"), true)
        return EVENTS.size
    }

    private fun skip(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.skip(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("Skipped ${event.id}"), true)
        return 1
    }

}