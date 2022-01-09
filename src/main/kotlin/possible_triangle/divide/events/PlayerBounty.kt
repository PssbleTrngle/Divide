package possible_triangle.divide.events

import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.Config
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.Teams

object PlayerBounty : CycleEvent("player_bounty") {

    override fun isEnabled(server: MinecraftServer): Boolean {
        return Config.CONFIG.bounties.enabled
    }

    override fun handle(server: MinecraftServer, index: Int): Int {

        val players = Teams.players(server).sortedBy {
            -DeathEvents.timeSinceDeath(it)
        }

        val target = players.firstOrNull()

        if (target != null) {
            val price = Config.CONFIG.bounties.baseAmount
            val opponents = Teams.players(server).filter { it.team?.isAlliedTo(target.team) != true }

            Chat.subtitle(target, "☠ There is a bounty on your head ☠")

            opponents.forEach {
                Chat.title(it, "☠ Wanted Dead ☠")
                Chat.subtitle(it, TextComponent("").append(target.displayName).append(" - $price points"), false)
            }
        }

        return Config.CONFIG.bounties.pause.value
    }
}