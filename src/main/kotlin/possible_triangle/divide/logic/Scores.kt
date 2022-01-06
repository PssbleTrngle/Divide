package possible_triangle.divide.logic

import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod

@Mod.EventBusSubscriber
object Scores {

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        val world = event.world
        if (world !is ServerLevel) return
        if (event.phase == TickEvent.Phase.END) TeamLogic.teams(world).forEach {
            updateScore(world.server, it)
        }
    }

    private fun scoreboard(server: MinecraftServer, team: Team): Objective {
        val name = "${DivideMod.ID}_team_${team.color.name.lowercase()}"
        return server.scoreboard.getObjective(name) ?: server.scoreboard.addObjective(
            name,
            ObjectiveCriteria.DUMMY,
            TextComponent("Info"),
            ObjectiveCriteria.RenderType.HEARTS
        )
    }

    private fun updateScore(server: MinecraftServer, team: Team) {
        val scoreboard = scoreboard(server, team)

        server.scoreboard.setDisplayObjective(
            Scoreboard.getDisplaySlotByName("sidebar.team.${team.color.name.lowercase()}"),
            scoreboard
        )

        val lines = listOf(
            "Cash" to CashLogic.get(server.overworld(), team),
        )

       // server.scoreboard.getPlayerScores(scoreboard)
       //     .filterNot { lines.contains(it.owner) }
       //     .forEach { server.scoreboard.resetPlayerScore(it.owner, scoreboard) }

        lines.forEach { (line, value) ->
            server.scoreboard.getOrCreatePlayerScore(line.padEnd(10), scoreboard).score = value
        }
    }

}