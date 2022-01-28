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
import possible_triangle.divide.GameData
import possible_triangle.divide.events.Border
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.events.CycleEvent.Companion.EVENTS
import possible_triangle.divide.logic.Teams

object EventsCommand {

    private val PLAYERS_MISSING_TEAM = DynamicCommandExceptionType {
        val players = it as List<ServerPlayer>
        val names = players.map { it.displayName as MutableComponent }.reduce { component, name ->
            component.append(TextComponent(", ")).append(name)
        }
        TextComponent("${players.size} players are missing not yet in a team: ").append(names)
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
            .then(forEvents("get", ::get))
            .then(forEvents("start", ::start, ::start))
            .then(forEvents("stop", ::stop, ::stop))
            .then(forEvents("skip", ::skip))
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

        GameData.setStarted(ctx.source.server, true)

        return 1
    }

    private fun start(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        val restarted = event.startCycle(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("${if (restarted) "Restarted" else "Started"} ${event.id}"), true)
        return 1
    }

    private fun get(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        val index = event.data[ctx.source.server]
        val remaining = event.remaining(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("${event.id} will happen in ${remaining}s for the ${index?.plus(2) ?: "first"} time"),
            false)
        return remaining
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.stop(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("Stopped ${event.id}"), true)
        return 1
    }

    private fun stop(ctx: CommandContext<CommandSourceStack>): Int {
        GameData.setStarted(ctx.source.server, false)
        ctx.source.sendSuccess(TextComponent("Stopped ${EVENTS.size} events"), true)
        return EVENTS.size
    }

    private fun skip(ctx: CommandContext<CommandSourceStack>, event: CycleEvent): Int {
        event.skip(ctx.source.server)
        ctx.source.sendSuccess(TextComponent("Skipped ${event.id}"), true)
        return 1
    }

}