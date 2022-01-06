package possible_triangle.divide.logic.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent
import net.minecraft.world.level.saveddata.SavedData
import possible_triangle.divide.Chat
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.TeamLogic

object Eras : CycleEvent("eras") {

    private const val PEACE_TIME = 60 * 5
    private const val WAR_TIME = 60 * 60

    fun peace(server: MinecraftServer) {
        Data[server] = true
        server.playerList.players.forEach {
            Chat.subtitle(it, "Peace has begun", false)
            Chat.title(it, "❤")
        }

        val bar = bar(server)
        bar.name = TextComponent("Peace Era")
        bar.color = BossEvent.BossBarColor.GREEN
        bar.players = server.playerList.players.filter(TeamLogic::isPlayer)
        bar.isVisible = true
    }

    fun war(server: MinecraftServer) {
        Data[server] = false
        server.playerList.players.forEach {
            Chat.subtitle(it, "War has started", false)
            Chat.title(it, "⚔")
        }

        bar(server).isVisible = false
    }

    override fun handle(server: MinecraftServer, index: Int): Int {
        val peace = index % 2 == 0
        val pause = if (peace) PEACE_TIME else WAR_TIME

        if (peace) peace(server)
        else war(server)

        return pause
    }

    class Data(private var isPeace: Boolean = false) : SavedData() {

        override fun save(nbt: CompoundTag): CompoundTag {
            nbt.putBoolean("value", isPeace)
            return nbt
        }

        companion object {
            private fun load(nbt: CompoundTag): Data {
                return Data(nbt.getBoolean("value"))
            }

            private fun data(server: MinecraftServer): Data {
                return server.overworld().dataStorage.computeIfAbsent(::load, ::Data, "${DivideMod.ID}_peace")
            }

            operator fun set(server: MinecraftServer, value: Boolean) {
                val data = data(server)
                data.isPeace = value
                data.setDirty()
            }

            operator fun get(server: MinecraftServer): Boolean {
                return data(server).isPeace
            }
        }

    }

}