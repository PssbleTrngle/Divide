package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import possible_triangle.divide.GameData
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.events.CycleEvent.Companion.EVENTS
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants

object EventsCommand {

    private val PLAYERS_MISSING_TEAM = DynamicCommandExceptionType {
        val players = it as List<ServerPlayerEntity>
        val names = players.map { it.displayName as MutableText }.reduce { component, name ->
            component.append(Text.literal(", ")).append(name)
        }
        Text.literal("${players.size} players are missing not yet in a team: ").append(names)
    }

    fun register(base: LiteralArgumentBuilder<ServerCommandSource>): LiteralArgumentBuilder<ServerCommandSource> {

        fun forEvents(
            literal: String,
            withEvent: (CommandContext<ServerCommandSource>, CycleEvent) -> Int,
            without: ((CommandContext<ServerCommandSource>) -> Int)? = null,
        ): LiteralArgumentBuilder<ServerCommandSource>? {
            return EVENTS.fold(if (without == null) literal(literal) else literal(literal).executes { without(it) }) { node, event ->
                node.then(literal(event.id).executes {
                    withEvent(it, event)
                })
            }
        }

        return base.then(literal("center").executes(::center))
            .then(forEvents("status", ::get))
            .then(forEvents("start", ::start, ::start))
            .then(forEvents("stop", ::stop, ::stop))
            .then(forEvents("skip", ::skip))
            .then(literal("fix").executes(::fix))
    }

    private fun fix(ctx: CommandContext<ServerCommandSource>): Int {
        val missing = EVENTS.filterNot { it.isRunning(ctx.source.server) }

        if (missing.isNotEmpty()) {
            missing.forEach { it.startCycle(ctx.source.server) }
            ctx.source.sendFeedback(Text.literal("Started ${missing.size} missing events"), true)
        } else {
            ctx.source.sendError(Text.literal("No event cycle missing"))
        }

        return missing.size
    }

    private fun center(ctx: CommandContext<ServerCommandSource>): Int {
        val worldborder = ctx.source.server.overworld.worldBorder
        val pos = ctx.source.position
        worldborder.setCenter(pos.x, pos.z)
        ctx.source.world.setSpawnPos(BlockPos(pos), 0F)
        Border.lobby(ctx.source.server)

        return 1
    }

    private fun start(ctx: CommandContext<ServerCommandSource>): Int {
        val noTeam = ctx.source.server.participants().filter { it.participantTeam() == null }
        if (noTeam.isNotEmpty()) throw PLAYERS_MISSING_TEAM.create(noTeam)

        GameData.setStarted(ctx.source.server, true)

        return 1
    }

    private fun start(ctx: CommandContext<ServerCommandSource>, event: CycleEvent): Int {
        val restarted = event.startCycle(ctx.source.server)
        ctx.source.sendFeedback(Text.literal("${if (restarted) "Restarted" else "Started"} ${event.id}"), true)
        return 1
    }

    private fun get(ctx: CommandContext<ServerCommandSource>, event: CycleEvent): Int {
        val index = event.data[ctx.source.server]
        val remaining = event.remaining(ctx.source.server)
        ctx.source.sendFeedback(
            Text.literal("${event.id} will happen in ${remaining}s for the ${index?.plus(2) ?: "first"} time"),
            false
        )
        return remaining
    }

    private fun stop(ctx: CommandContext<ServerCommandSource>, event: CycleEvent): Int {
        event.stop(ctx.source.server)
        ctx.source.sendFeedback(Text.literal("Stopped ${event.id}"), true)
        return 1
    }

    private fun stop(ctx: CommandContext<ServerCommandSource>): Int {
        GameData.setStarted(ctx.source.server, false)
        ctx.source.sendFeedback(Text.literal("Stopped ${EVENTS.size} events"), true)
        return EVENTS.size
    }

    private fun skip(ctx: CommandContext<ServerCommandSource>, event: CycleEvent): Int {
        event.skip(ctx.source.server)
        ctx.source.sendFeedback(Text.literal("Skipped ${event.id}"), true)
        return 1
    }

}