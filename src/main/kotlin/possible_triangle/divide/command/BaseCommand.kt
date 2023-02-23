package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.particle.DustParticleEffect
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import possible_triangle.divide.Config
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Teams.teamOrThrow
import java.awt.Color

object BaseCommand {

    private val NO_BASE = SimpleCommandExceptionType(Text.literal("Your team has not yet set a base point"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("base")
                .requires { it.isActiveParticipant() }
                .then(literal("move").executes(::setBase))
                .then(literal("compass").executes(::giveCompass))
                .then(literal("show").executes(::show))
        )
    }

    private fun setBase(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.playerOrThrow
        Bases.setBase(player)
        ctx.source.sendFeedback(Text.literal("Changed your team's base position"), false)
        return 1
    }

    private fun giveCompass(ctx: CommandContext<ServerCommandSource>): Int {
        val stack = Bases.createCompass(ctx.source.playerOrThrow) ?: throw NO_BASE.create()
        val given = ctx.source.playerOrThrow.giveItemStack(stack)
        if (!given) ctx.source.sendError(Text.literal("You inventory is already full"))
        return if (given) 1 else 0
    }

    private fun show(ctx: CommandContext<ServerCommandSource>): Int {
        val team = ctx.source.playerOrThrow.teamOrThrow()
        val (pos, dimension) = Bases.getBase(ctx.source.server, team) ?: throw NO_BASE.create()
        val level = ctx.source.server.getWorld(dimension) ?: return 0

        val color = Color(team.color.colorValue ?: 0xFFFFFF).let {
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
                    val vec = Vec3d(x * step + pos.x, y * step + pos.y, z * step + pos.z)
                    val edges = listOf(x, y, z).count { min == it || max == it }
                    val c =
                        if (edges > 1) color
                        else if (vec.distanceTo(ctx.source.playerOrThrow.pos) <= 4 && edges > 0) lighterColor
                        else null
                    if (c != null)
                        level.spawnParticles(
                            ctx.source.playerOrThrow, DustParticleEffect(c, 1F), false,
                            vec.x, vec.y, vec.z,
                            1, 0.0, 0.0, 0.0, 0.0
                        )
                }
            }
        }

        return 1
    }

}