package possible_triangle.divide.events

import kotlinx.serialization.Serializable
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import possible_triangle.divide.Config
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.Util.persistentData
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.makeWeightedDecision

object PlayerBountyEvent : CycleEvent("player_bounty") {

    override val enabled: Boolean
        get() = Config.CONFIG.bounties.enabled

    override val startsAfter: Int
        get() = Config.CONFIG.bounties.startAfter

    @Serializable
    private data class Event(val target: EventTarget, val killer: EventTarget? = null, val bounty: Int)

    private val LOGGER = EventLogger(id, { Event.serializer() }) { always() }

    override fun handle(server: MinecraftServer, index: Int): Int {

        val minutesDead = server.participants()
            .associateWith { DeathEvents.timeSinceDeath(it) / 20 / 60 }
            .mapValues { it.value.toInt() }
        val target = makeWeightedDecision(minutesDead)

        if (target != null) {
            val bonus = minutesDead.getOrDefault(target, 0) * Config.CONFIG.bounties.bonusPerAliveMinute
            val price = Config.CONFIG.bounties.baseAmount + bonus
            val opponents = server.participants().filterNot { it.isTeammate(target) }

            Chat.subtitle(target, "☠ There is a bounty on your head ☠")

            opponents.forEach {
                Chat.title(it, "☠ Wanted Dead ☠")
                Chat.subtitle(it, Text.literal("").append(target.displayName).append(" - $price points"), false)
            }

            setBounty(target, PlayerBounty(price, target.world.time + Config.CONFIG.bounties.bountyTime * 20))
        }

        return Config.CONFIG.bounties.pause.value
    }

    fun currentBounties(server: MinecraftServer): Map<ServerPlayerEntity, PlayerBounty> {
        return server.participants()
            .associateWith { getBounty(it) }
            .filterValues { it != null }
            .mapValues { it.value as PlayerBounty }
    }

    private fun setBounty(player: ServerPlayerEntity, bounty: PlayerBounty?) {
        val data = player.persistentData()
        if (bounty != null) data.put("bounty", bounty.serialize())
        else data.remove("bounty")
    }

    fun checkBounty(target: ServerPlayerEntity, killer: ServerPlayerEntity?): Boolean {
        val bounty = getBounty(target) ?: return false

        val title = when {
            killer != null -> Text.literal("Bounty fulfilled by ").append(killer.displayName)
            Config.CONFIG.bounties.clearOnDeath -> Text.literal("Bounty cleared")
            else -> null
        }

        return if (title != null) {
            setBounty(target, null)
            target.server.participants().forEach {
                Chat.subtitle(it, title, setTitle = false)
                Chat.title(
                    it,
                    Text.literal("☠ ").append(target.displayName).append(Text.literal(" ☠"))
                )
            }

            LOGGER.log(target.server, Event(EventTarget.of(target), EventTarget.optional(killer), bounty.price))

            val killerTeam = killer?.participantTeam()
            if (killerTeam != null) {
                Points.modify(killer.server, killerTeam, bounty.price)
                killerTeam.participants(target.server).forEach {
                    Chat.subtitle(it, "Bounty fulfilled")
                    Chat.title(it, "+${bounty.price}")
                }

                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun getBounty(player: ServerPlayerEntity): PlayerBounty? {
        val nbt = player.persistentData()
        return if (nbt.contains("bounty", 10)) {
            val bounty = PlayerBounty.deserialize(nbt.getCompound("bounty"))
            if (bounty != null && bounty.until > player.world.time) bounty
            else null
        } else {
            null
        }
    }

    data class PlayerBounty(val price: Int, val until: Long) {
        fun serialize(): NbtCompound {
            val nbt = NbtCompound()
            nbt.putInt("price", price)
            nbt.putLong("until", until)
            return nbt
        }

        companion object {
            fun deserialize(nbt: NbtCompound): PlayerBounty? {
                val until = nbt.getLong("until")
                val price = nbt.getInt("price")
                return if (price > 0) PlayerBounty(price, until) else null
            }
        }
    }

}