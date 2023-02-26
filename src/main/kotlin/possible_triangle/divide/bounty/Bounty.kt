package possible_triangle.divide.bounty

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.bounty.Amount.Type.*
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.PerTeamIntData
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.teammates

@Serializable
data class Bounty(val description: String, val amount: Amount) {

    @Transient
    lateinit var id: String
        private set

    @Serializable
    private data class Event(
        val bounty: Bounty,
        val pointsEarned: Int,
        val pointsNow: Int,
        val doneAlready: Int,
        val fulfilledBy: EventTarget
    )

    companion object : DefaultedResource<Bounty>("bounty", { Bounty.serializer() }) {

        private val LOGGER = EventLogger("bounty", { Event.serializer() }) { inTeam { it.fulfilledBy.team } }

        private val BOUNTY_COUNTS = PerTeamIntData("bounties")

        fun reset(server: MinecraftServer, team: PlayerTeam) {
            BOUNTY_COUNTS[server][team] = 0
        }

        override fun populate(entry: Bounty, server: MinecraftServer?, id: String) {
            entry.id = id
        }

        val PLAYER_KILL by defaulted("PLAYER_KILL") { Bounty("Kill a Player", Amount(CONSTANT, listOf(100))) }
        val BLOWN_UP by defaulted("BLOWN_UP") { Bounty("Blow a player up", Amount(CONSTANT, listOf(200))) }
        val ADVANCEMENT by defaulted("ADVANCEMENT") {
            Bounty(
                "Unlock an advancement",
                Amount(INCREASING, listOf(10, 5, 150))
            )
        }

        val SOLD_HEART by defaulted("SOLD_HEART") { Bounty("Sold a heart", Amount(INCREASING, listOf(100, 20))) }

        val MINED_COAL by defaulted("MINED_COAL") { Bounty("Mine a coal ore", Amount(DECREASING, listOf(10, 1))) }
        val MINED_IRON by defaulted("MINED_IRON") { Bounty("Mine a iron ore", Amount(DECREASING, listOf(20, 2))) }
        val MINED_GOLD by defaulted("MINED_GOLD") { Bounty("Mine a gold ore", Amount(DECREASING, listOf(25, 1))) }
        val MINED_DIAMOND by defaulted("MINED_DIAMOND") {
            Bounty(
                "Mine a diamond ore",
                Amount(DECREASING, listOf(160, 20))
            )
        }
        val MINED_EMERALD by defaulted("MINED_EMERALD") {
            Bounty(
                "Mine a emerald ore",
                Amount(DECREASING, listOf(160, 20))
            )
        }
        val MINED_NETHERITE by defaulted("MINED_NETHERITE") {
            Bounty(
                "Mine ancient debris",
                Amount(DECREASING, listOf(300, 20))
            )
        }

    }

    fun nextPoints(team: PlayerTeam, server: MinecraftServer): Int {
        val bounties = BOUNTY_COUNTS[server]
        val alreadyDone = bounties[team]
        return amount.get(alreadyDone)
    }

    fun gain(player: Player, modifier: Double = 1.0) {
        val team = player.participantTeam()

        if (player is ServerPlayer && team != null) {
            val cashGained = (nextPoints(team, player.server) * modifier).toInt()

            if (cashGained > 0) {
                Points.modify(player.server, team, cashGained) { pointsNow ->
                    LOGGER.log(
                        player.server,
                        Event(
                            this,
                            cashGained,
                            pointsNow,
                            BOUNTY_COUNTS[player.server][team],
                            EventTarget.of(player),
                        )
                    )
                }


                player.teammates().forEach { teammate ->
                    Chat.sound(teammate, ResourceLocation("entity.experience_orb.pickup"))
                    //it.sendMessage(Component.literal("You're team gained $cashGained"), ChatType.GAME_INFO, it.uuid)
                    Chat.subtitle(
                        teammate,
                        Component.literal(description).withStyle { it.withItalic(true) }
                    )
                    Chat.title(teammate, "+$cashGained")
                }
            }

            BOUNTY_COUNTS[player.server][team]++
        }
    }

}