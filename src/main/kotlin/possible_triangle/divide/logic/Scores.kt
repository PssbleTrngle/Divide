package possible_triangle.divide.logic

import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
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
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.crates.Order
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.reward.Reward

@Mod.EventBusSubscriber
object Scores {

    enum class Extra { prices, bounties, ranks, orders, commands }

    private val RANK_COLORS = listOf(GOLD, GRAY, DARK_RED)
    private val EXTRAS = hashMapOf<String, Extra>()

    fun show(team: Team, extra: Extra) {
        EXTRAS[team.name] = extra
    }

    private fun rankString(rank: Int): String {
        val color = RANK_COLORS.getOrElse(rank - 1) { WHITE }
        return apply("#${rank}", UNDERLINE, color)
    }

    private fun getRanks(server: MinecraftServer): List<String> {
        val teams = Teams.ranked(server)
        return teams.mapIndexed { index, team ->
            "${rankString(index + 1)} ${team.displayName.string}"
        }
    }

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        val server = event.world.server ?: return
        if (event.phase != TickEvent.Phase.END) return

        val overall = getObjective(server)
        server.scoreboard.setDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR, overall)
        update(overall, getRanks(server))

        Teams.ranked(server).forEachIndexed { index, team ->
            updateForTeam(server, team, index + 1)
        }
    }

    private fun getObjective(server: MinecraftServer, team: Team? = null): Objective {
        val name =
            if (team != null) "${DivideMod.ID}_team_${team.color.name.lowercase()}" else "${DivideMod.ID}_overall"
        return server.scoreboard.getObjective(name) ?: server.scoreboard.addObjective(
            name,
            ObjectiveCriteria.DUMMY,
            TextComponent("INFO").withStyle(UNDERLINE),
            ObjectiveCriteria.RenderType.HEARTS
        )
    }

    private fun apply(string: Any, vararg styles: ChatFormatting): String {
        return "${styles.joinToString(separator = "") { "§${it.char}" }}$string§r"
    }

    private fun update(objective: Objective, lines: List<String>) {
        val formatted = lines.mapIndexed { i, line ->
            if (line.isBlank()) " ".repeat(i)
            else line
        }

        objective.scoreboard.getPlayerScores(objective)
            .filterNot { formatted.contains(it.owner) }
            .forEach { objective.scoreboard.resetPlayerScore(it.owner, objective) }

        formatted.reversed().forEachIndexed { index, line ->
            objective.scoreboard.getOrCreatePlayerScore(line, objective).score = index
        }
    }

    private fun extraInfo(server: MinecraftServer, team: Team): List<String> {
        val points = { value: Int -> apply("$value points", GREEN) }

        val extra = EXTRAS[team.name] ?: return listOf()
        val info: List<String> = when (extra) {
            Extra.bounties -> PlayerBountyEvent.currentBounties(server).map { (target, bounty) ->
                "${apply("Kill ${target.scoreboardName}", GOLD)}: ${points(bounty.price)}"
            } + Bounty.entries
                .mapKeys { it.value.description }
                .mapValues { it.value.nextPoints(team, server) }
                .filterValues { it > 0 }
                .entries.sortedBy { it.value }
                .map { "${it.key}: ${points(it.value)}" }
            Extra.prices -> Reward.values.map { "${it.display}: ${points(it.price)}" }
            Extra.orders -> Order.values.map { "${it.id}: ${points(it.cost)}" }
            Extra.ranks -> getRanks(server)
            Extra.commands -> listOf(
                "/order: ${apply("pre-order item into next loot drop", GRAY)}",
                "/buy: ${apply("spend points to unlock rewards", GRAY)}",
                "/sell: ${apply("sell integral parts of your life for points", GRAY)}",
                "/show: ${apply("toggle between different info views", GRAY)}",
                "/teammsg: ${apply("send a message to your teammates", GRAY)}",
                "/w: ${apply("send a message to one player", GRAY)}",
            )
        }
        return listOf("") + info

    }

    private fun updateForTeam(server: MinecraftServer, team: Team, rank: Int) {
        val objective = getObjective(server, team)

        server.scoreboard.setDisplayObjective(
            Scoreboard.getDisplaySlotByName("sidebar.team.${team.color.name.lowercase()}"),
            objective
        )

        val lines = listOf("") + mapOf(
            "Rank" to rankString(rank),
            "Points" to Points.get(server, team),
        ).map { "${it.key}: ${apply(it.value, GREEN)}" } + extraInfo(server, team) + listOf(
            "",
            apply("use /show ...", GRAY),
        )

        update(objective, lines)
    }

}