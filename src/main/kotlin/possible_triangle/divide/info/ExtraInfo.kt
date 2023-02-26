package possible_triangle.divide.info

import net.minecraft.ChatFormatting.*
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.crates.Order
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.reward.Reward

enum class ExtraInfo(val lines: (MinecraftServer, PlayerTeam?) -> List<String>) {
    PRICES({ server, team ->
        Reward.values.filter { Reward.isVisible(it, team, server) }.map { reward ->
            "${reward.display}: ${valueStyle(reward.price)}".let {
                if (reward.secret) Chat.apply(it, GRAY, ITALIC) else it
            }
        }
    }),
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
    ORDERS({ server, team ->
        Order.values
            .filter { Order.isVisible(it, team, server) }
            .map { "${it.itemId}: ${valueStyle(it.cost)}" }
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