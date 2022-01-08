package possible_triangle.divide.logic

import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
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
        val server = event.world.server ?: return

        val overall = scoreboard(server)
        server.scoreboard.setDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR, overall)

        if (event.phase == TickEvent.Phase.END) TeamLogic.ranked(server).forEachIndexed { index, team ->
            val rank = index + 1
            server.scoreboard.getOrCreatePlayerScore(team.name, overall).score = rank
            updateScore(server, team, rank)
        }
    }

    private fun scoreboard(server: MinecraftServer, team: Team? = null): Objective {
        val name =
            if (team != null) "${DivideMod.ID}_team_${team.color.name.lowercase()}" else "${DivideMod.ID}_overall"
        return server.scoreboard.getObjective(name) ?: server.scoreboard.addObjective(
            name,
            ObjectiveCriteria.DUMMY,
            TextComponent("Info"),
            ObjectiveCriteria.RenderType.HEARTS
        )
    }

    private fun updateScore(server: MinecraftServer, team: Team, rank: Int) {
        val scoreboard = scoreboard(server, team)

        server.scoreboard.setDisplayObjective(
            Scoreboard.getDisplaySlotByName("sidebar.team.${team.color.name.lowercase()}"),
            scoreboard
        )

        val lines = mapOf(
            "Rank" to rank,
            "Score" to TeamLogic.score(server, team),
            "Points" to CashLogic.get(server, team),
        ).mapKeys { it.key.padEnd(10) }

        server.scoreboard.getPlayerScores(scoreboard)
            .filterNot { lines.containsKey(it.owner) }
            .forEach { server.scoreboard.resetPlayerScore(it.owner, scoreboard) }

        lines.forEach { (line, value) ->
            server.scoreboard.getOrCreatePlayerScore(line, scoreboard).score = value
        }
    }

}