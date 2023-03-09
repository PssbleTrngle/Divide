package possible_triangle.divide.command.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.GameData
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.events.CycleEvent.Companion.EVENTS
import possible_triangle.divide.extensions.mainWorld
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants
import kotlin.time.DurationUnit

object EventsCommand {

    private val PLAYERS_MISSING_TEAM = DynamicCommandExceptionType {
        val players = it as List<ServerPlayer>
        val names = players.map { it.displayName as MutableComponent }.reduce { component, name ->
            component.append(Component.literal(", ")).append(name)
        }
        Component.literal("${players.size} players are missing not yet in a team: ").append(names)
    }

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack> {

        fun forEvents(
            literal: String,
            withEvent: (CommandContext<CommandSourceStack>, CycleEvent) -> Int,
            without: ((CommandContext<CommandSourceStack>) -> Int)? = null,
        ): LiteralArgumentBuilder<CommandSourceStack>? {
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

    private fun fix(ctx: CommandContext<CommandSourceStack>): Int {
        val missing = EVENTS.filterNot { it.isRunning(ctx.source.server) }

        if (missing.isNotEmpty()) {
            missing.forEach { it.startCycle(ctx.source.server) }
            ctx.source.sendSuccess(Component.literal("Started ${missing.size} missing events"), true)
        } else {
            ctx.source.sendFailure(Component.literal("No event cycle missing"))
        }

        return missing.size
    }

    private fun center(ctx: CommandContext<CommandSourceStack>): Int {
        val border = ctx.source.server.mainWorld().worldBorder
        val pos = ctx.source.position
        border.setCenter(pos.x, pos.z)
        ctx.source.server.mainWorld().setDefaultSpawnPos(BlockPos(pos), 0F)
        Border.lobby(ctx.source.server)

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>): Int {
        val noTeam = ctx.source.server.participants().filter { it.participantTeam() == null }
        if (noTeam.isNotEmpty()) throw PLAYERS_MISSING_TEAM.create(noTeam)

        GameData.setStarted(ctx.source.server, true)

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        val restarted = event.startCycle(ctx.source.server)
        ctx.source.sendSuccess(Component.literal("${if (restarted) "Restarted" else "Started"} ${event.id}"), true)
        return 1
    }

    private fun get(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        val index = event.data[ctx.source.server]
        val remaining = event.remaining(ctx.source.server)
        ctx.source.sendSuccess(
            Component.literal("${event.id} will happen in ${remaining}s for the ${index?.plus(2) ?: "first"} time"),
            false
        )
        return remaining.toInt(DurationUnit.SECONDS)
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.stop(ctx.source.server)
        ctx.source.sendSuccess(Component.literal("Stopped ${event.id}"), true)
        return 1
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>): Int {
        GameData.setStarted(ctx.source.server, false)
        ctx.source.sendSuccess(Component.literal("Stopped ${EVENTS.size} events"), true)
        return EVENTS.size
    }

    private fun skip(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.skip(ctx.source.server)
        ctx.source.sendSuccess(Component.literal("Skipped ${event.id}"), true)
        return 1
    }

}