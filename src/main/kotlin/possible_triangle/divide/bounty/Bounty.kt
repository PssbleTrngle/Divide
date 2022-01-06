package possible_triangle.divide.bounty

import kotlinx.serialization.Serializable
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import possible_triangle.divide.Chat
import possible_triangle.divide.bounty.Amount.Type.*
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.CashLogic
import possible_triangle.divide.logic.PerTeamData
import possible_triangle.divide.logic.TeamLogic

@Serializable
data class Bounty(val description: String, val amount: Amount) {

    companion object : DefaultedResource<Bounty>("bounty", { Bounty.serializer() }) {

        private val BOUNTY_COUNTS = PerTeamData("bounties")

        val PLAYER_KILL by defaulted("PLAYER_KILL") { Bounty("Kill a Player", Amount(CONSTANT, listOf(100))) }
        val BLOWN_UP by defaulted("BLOWN_UP") { Bounty("Blow a player up", Amount(CONSTANT, listOf(200))) }
        val ADVANCEMENT by defaulted("ADVANCEMENT") {
            Bounty(
                "Unlock an advancement",
                Amount(INCREASING, listOf(20, 10))
            )
        }

        val SOLD_HEART by defaulted("SOLD_HEART") { Bounty("Sold a heart", Amount(CONSTANT, listOf(200))) }

        val MINED_COAL by defaulted("MINED_COAL") { Bounty("Mine a coal ore", Amount(FIRST, listOf(20))) }
        val MINED_IRON by defaulted("MINED_IRON") { Bounty("Mine a iron ore", Amount(FIRST, listOf(20, 1))) }
        val MINED_GOLD by defaulted("MINED_GOLD") { Bounty("Mine a gold ore", Amount(FIRST, listOf(20, 1))) }
        val MINED_DIAMOND by defaulted("MINED_DIAMOND") { Bounty("Mine a diamond ore", Amount(FIRST, listOf(200, 10))) }
        val MINED_EMERALD by defaulted("MINED_EMERALD") { Bounty("Mine a emerald ore", Amount(FIRST, listOf(200, 10))) }
        val MINED_NETHERITE by defaulted("MINED_NETHERITE") {
            Bounty(
                "Mine ancient debris",
                Amount(FIRST, listOf(300, 20))
            )
        }

    }

    fun gain(player: Player, modifier: Double = 1.0) {
        val team = TeamLogic.teamOf(player)

        if (player is ServerPlayer && team != null) {
            val bounties = BOUNTY_COUNTS.get(player.getLevel())
            val alreadyDone = bounties[team]
            val cashGained = (amount.get(alreadyDone) * modifier).toInt()

            if (cashGained > 0) {
                CashLogic.modify(player.getLevel(), team, cashGained)

                TeamLogic.teammates(player).forEach { teammate ->
                    //it.sendMessage(TextComponent("You're team gained $cashGained"), ChatType.GAME_INFO, it.uuid)
                    Chat.subtitle(
                        teammate,
                        TextComponent(description).withStyle { it.withItalic(true) }
                    )
                    Chat.title(teammate, "+$cashGained")
                }
            }

            bounties[team] = alreadyDone + 1
        }
    }

}