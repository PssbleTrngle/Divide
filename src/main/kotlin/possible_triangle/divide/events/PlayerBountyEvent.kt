package possible_triangle.divide.events

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import possible_triangle.divide.Config
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.Util
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.*

object PlayerBountyEvent : CycleEvent("player_bounty") {

    override val enabled: Boolean
        get() = Config.CONFIG.bounties.enabled

    override val startsAfter: Int
        get() = Config.CONFIG.bounties.startAfter

    @Serializable
    private data class Event(val target: EventTarget, val killer: EventTarget? = null, val bounty: Int)

    private val LOGGER = EventLogger(id, { Event.serializer() }) { always() }

    override fun handle(server: MinecraftServer, index: Int): Int {

        val minutesDead = Teams.players(server)
            .associateWith { DeathEvents.timeSinceDeath(it) / 20 / 60 }
            .mapValues { it.value.toInt() }
        val target = makeWeightedDecision(minutesDead)

        if (target != null) {
            val bonus = minutesDead.getOrDefault(target, 0) * Config.CONFIG.bounties.bonusPerAliveMinute
            val price = Config.CONFIG.bounties.baseAmount + bonus
            val opponents = Teams.players(server).filter { it.team?.isAlliedTo(target.team) != true }

            Chat.subtitle(target, "☠ There is a bounty on your head ☠")

            opponents.forEach {
                Chat.title(it, "☠ Wanted Dead ☠")
                Chat.subtitle(it, TextComponent("").append(target.displayName).append(" - $price points"), false)
            }

            setBounty(target, PlayerBounty(price, target.level.gameTime + Config.CONFIG.bounties.bountyTime * 20))
        }

        return Config.CONFIG.bounties.pause.value
    }

    fun currentBounties(server: MinecraftServer): Map<ServerPlayer, PlayerBounty> {
        return Teams.players(server)
            .associateWith { getBounty(it) }
            .filterValues { it != null }
            .mapValues { it.value as PlayerBounty }
    }

    private fun setBounty(player: ServerPlayer, bounty: PlayerBounty?) {
        val data = Util.persistentData(player)
        if (bounty != null) data.put("bounty", bounty.serialize())
        else data.remove("bounty")
    }

    fun checkBounty(target: ServerPlayer, killer: ServerPlayer?): Boolean {
        val bounty = getBounty(target) ?: return false

        val title = when {
            killer != null -> TextComponent("Bounty fulfilled by ").append(killer.displayName)
            Config.CONFIG.bounties.clearOnDeath -> TextComponent("Bounty cleared")
            else -> null
        }

        return if (title != null) {
            setBounty(target, null)
            Teams.players(target.server).forEach {
                Chat.subtitle(it, title, setTitle = false)
                Chat.title(
                    it,
                    TextComponent("☠ ").append(target.displayName).append(TextComponent(" ☠"))
                )
            }

            LOGGER.log(target.server, Event(EventTarget.of(target), EventTarget.optional(killer), bounty.price))

            val killerTeam = killer?.let { Teams.teamOf(it) }
            if (killerTeam != null) {
                Points.modify(killer.server, killerTeam, bounty.price)
                Teams.players(target.server, killerTeam).forEach {
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

    fun getBounty(player: ServerPlayer): PlayerBounty? {
        val nbt = Util.persistentData(player)
        return if (nbt.contains("bounty", 10)) {
            val bounty = PlayerBounty.deserialize(nbt.getCompound("bounty"))
            if (bounty != null && bounty.until > player.level.gameTime) bounty
            else null
        } else {
            null
        }
    }

    data class PlayerBounty(val price: Int, val until: Long) {
        fun serialize(): CompoundTag {
            val nbt = CompoundTag()
            nbt.putInt("price", price)
            nbt.putLong("until", until)
            return nbt
        }

        companion object {
            fun deserialize(nbt: CompoundTag): PlayerBounty? {
                val until = nbt.getLong("until")
                val price = nbt.getInt("price")
                return if (price > 0) PlayerBounty(price, until) else null
            }
        }
    }

}