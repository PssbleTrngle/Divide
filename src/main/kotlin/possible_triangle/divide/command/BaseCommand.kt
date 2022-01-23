package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.math.Vector3f
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.phys.Vec3
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.logic.Bases
import possible_triangle.divide.logic.Teams
import java.awt.Color

@Mod.EventBusSubscriber
object BaseCommand {

    private val NO_BASE = SimpleCommandExceptionType(TextComponent("Your team has not yet set a base point"))

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("base")
                .requires(Requirements::isPlayerInGame)
                .then(literal("move").executes(::setBase))
                .then(literal("compass").executes(::giveCompass))
                .then(literal("show").executes(::show))
        )
    }

    private fun setBase(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        Bases.setBase(player)
        ctx.source.sendSuccess(TextComponent("Changed your team's base position"), false)
        return 1
    }

    private fun giveCompass(ctx: CommandContext<CommandSourceStack>): Int {
        val stack = Bases.createCompass(ctx.source.playerOrException) ?: throw NO_BASE.create()
        val given = ctx.source.playerOrException.addItem(stack)
        if (!given) ctx.source.sendFailure(TextComponent("You inventory is already full"))
        return if (given) 1 else 0
    }

    private fun show(ctx: CommandContext<CommandSourceStack>): Int {
        val team = Teams.requiredTeam(ctx.source.playerOrException)
        val (pos, dimension) = Bases.getBase(ctx.source.server, team) ?: throw NO_BASE.create()
        val level = ctx.source.server.getLevel(dimension) ?: return 0

        val color = Color(team.color.color ?: 0xFFFFFF).let {
            Vector3f(it.red.toFloat() / 255F, it.green.toFloat() / 255F, it.blue.toFloat() / 255F)
        }
        val lighterColor = color.copy()
        lighterColor.mul(2F)
        lighterColor.clamp(0F, 1F)

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