package possible_triangle.divide.events

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.bossevents.CustomBossEvent
import possible_triangle.divide.DivideMod
import possible_triangle.divide.GameData
import possible_triangle.divide.extensions.time

fun CustomBossEvent.clearPlayers() {
    players = emptyList()
}

open class Countdown(private val id: String, private val display: String = id.replaceFirstChar { it.titlecase() }) {

    fun countdown(
        server: MinecraftServer,
        seconds: Int,
        resetPlayers: Boolean = true,
        consumer: CustomBossEvent.() -> Unit = {},
    ) {
        val bar = bar(server)
        if (resetPlayers) bar.clearPlayers()
        bar.max = seconds
        bar.value = seconds
        consumer(bar)
    }

    fun bar(server: MinecraftServer): CustomBossEvent {
        val name = ResourceLocation(DivideMod.ID, id)
        return with(server.customBossEvents) {
            get(name) ?: create(name, Component.literal(display)).apply {
                isVisible = true
            }
        }
    }

    init {
        ServerTickEvents.START_SERVER_TICK.register { server ->
            if (server.time() % 20 != 0L) return@register
            if (GameData.DATA[server].paused) return@register

            val bar = bar(server)
            if (bar.value > 0) bar.value--
            else bar.isVisible = false
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            bar(server).addPlayer(handler.player)
        }
    }

    fun remaining(server: MinecraftServer): Int {
        return bar(server).value
    }

}