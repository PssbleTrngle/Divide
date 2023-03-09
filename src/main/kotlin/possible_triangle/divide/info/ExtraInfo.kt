package possible_triangle.divide.info

import net.minecraft.ChatFormatting.*
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.logic.Chat

enum class ExtraInfo(val lines: (MinecraftServer, PlayerTeam?) -> List<String>) {
    BOUNTIES({ server, team ->
        PlayerBountyEvent.currentBounties(server).map { (target, bounty) ->
            "${
                Chat.apply(
                    "Kill ${target.scoreboardName}",
                    GOLD
                )
            }: ${valueStyle(bounty.price)}"
        } + Bounty.entries
            .filterValues { Bounty.isVisible(it, team, server) }
            .mapKeys { it.value.description }
            .mapValues { if (team == null) 0 else it.value.nextPoints(team, server) }
            .filterValues { it > 0 }
            .entries.sortedBy { it.value }
            .map { "${it.key}: ${valueStyle(it.value)}" }
    }),
    RANKS({ server, _ ->
        Scores.getRanks(server).map { (team, rank) -> "${Scores.rankString(rank)} ${team.name}" }
    }),
    COMMANDS({ _, _ ->
        mapOf(
            "order" to "pre-order item into next loot drop",
            "buy" to "spend points to unlock rewards",
            "sell" to "sell integral parts of your life for points",
            "show" to "toggle between different info views",
            "teammsg" to "send a message to your teammates",
            "w" to "send a message to one player",
        ).map { "/${it.key}: ${Chat.apply(it.value, GRAY)}" }
    });

    companion object {
        val valueStyle = { value: Int -> Chat.apply("$value points", GREEN) }
    }
}