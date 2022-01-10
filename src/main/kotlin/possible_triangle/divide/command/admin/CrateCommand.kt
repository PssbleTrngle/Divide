package possible_triangle.divide.command.admin

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.network.chat.TextComponent
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot

object CrateCommand {

    private val NO_CRATE_POS = DynamicCommandExceptionType { TextComponent("Could no find a valid pos around $it") }

    fun register(base: LiteralArgumentBuilder<CommandSourceStack>): LiteralArgumentBuilder<CommandSourceStack>? {
        return base.then(
            literal("crate").then(
                literal("spawn").then(
                    argument("pos", BlockPosArgument.blockPos())
                        .then(
                            CrateLoot.keys.fold(argument("in", TimeArgument.time()).executes(::spawnCrate))
                            { node, key -> node.then(literal(key).executes { spawnCrate(it) { CrateLoot.getOrThrow(key) } }) })
                        .executes(::spawnCrate)
                )
            )
        )
    }

    private fun spawnCrate(
        ctx: CommandContext<CommandSourceStack>,
        type: () -> CrateLoot = { CrateLoot.random() }
    ): Int {

        val center = BlockPosArgument.getSpawnablePos(ctx, "pos")
        val pos = CrateScheduler.findInRange(ctx.source.server, center, 10.0) ?: throw NO_CRATE_POS.create(center)

        val timeTicks = try {
            IntegerArgumentType.getInteger(ctx, "in")
        } catch (e: IllegalArgumentException) {
            0
        }

        val seconds = timeTicks / 20
        CrateScheduler.schedule(ctx.source.server, seconds, pos, type())

        ctx.source.sendSuccess(
            TextComponent(
                if (seconds == 0) "Crate delivered to $pos"
                else "Crate will be delivered to $pos in $seconds seconds"
            ), false
        )

        return seconds
    }

}