package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import possible_triangle.divide.Config
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Teams.teamOrThrow
import java.awt.Color

object BaseCommand {

    private val NO_BASE = SimpleCommandExceptionType(Component.literal("Your team has not yet set a base point"))

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("base")
                .requires { it.isActiveParticipant() }
                .then(literal("move").executes(::setBase))
                .then(literal("compass").executes(::giveCompass))
                .then(literal("show").executes(::show))
        )
    }

    private fun setBase(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        Bases.setBase(player)
        ctx.source.sendSuccess(Component.literal("Changed your team's base position"), false)
        return 1
    }

    private fun giveCompass(ctx: CommandContext<CommandSourceStack>): Int {
        val stack = Bases.createCompass(ctx.source.playerOrException) ?: throw NO_BASE.create()
        val given = ctx.source.playerOrException.addItem(stack)
        if (!given) ctx.source.sendFailure(Component.literal("You inventory is already full"))
        return if (given) 1 else 0
    }

    private fun show(ctx: CommandContext<CommandSourceStack>): Int {
        val team = ctx.source.playerOrException.teamOrThrow()
        val (pos, dimension) = Bases.getBase(ctx.source.server, team) ?: throw NO_BASE.create()
        val level = ctx.source.server.getLevel(dimension) ?: return 0

        val color = Color(team.color.color ?: 0xFFFFFF).let {
            Vector3f(it.red.toFloat() / 255F, it.green.toFloat() / 255F, it.blue.toFloat() / 255F)
        }
        val lighterColor = Vector3f(color)
        lighterColor.mul(2F)
        lighterColor.min(Vector3f(1F, 1F, 1F))

        val step = 0.25
        val to = Config.CONFIG.bases.radius.div(step).toInt()
        val min = -to
        val max = to + (1 / step).toInt()

        for (x in min..max) {
            for (y in min..max) {
                for (z in min..max) {
                    val vec = Vec3(x * step + pos.x, y * step + pos.y, z * step + pos.z)
                    val edges = listOf(x, y, z).count { min == it || max == it }
                    val c =
                        if (edges > 1) color
                        else if (vec.distanceTo(ctx.source.playerOrException.position()) <= 4 && edges > 0) lighterColor
                        else null
                    if (c != null)
                        level.sendParticles(
                            ctx.source.playerOrException, DustParticleOptions(c, 1F), false,
                            vec.x, vec.y, vec.z,
                            1, 0.0, 0.0, 0.0, 0.0
                        )
                }
            }
        }

        return 1
    }

}