package possible_triangle.divide.events

import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.event.TickEvent
import possible_triangle.divide.DivideMod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

open class Countdown(private val id: String, private val display: String = id.replaceFirstChar { it.titlecase() }) {

    init {
        FORGE_BUS.addListener(::tick)
    }

    fun countdown(server: MinecraftServer, Seconds: Int, resetPlayers: Boolean = true) {
        val bar = bar(server)
        if (resetPlayers) bar.players = listOf()
        bar.max = Seconds
        bar.value = Seconds
    }

    fun bar(server: MinecraftServer): CustomBossEvent {
        val name = ResourceLocation(DivideMod.ID, id)
        val existing = server.customBossEvents.get(name)
        if (existing != null) return existing
        val created = server.customBossEvents.create(name, TextComponent(display))
        created.isVisible = false
        return created
    }

    private fun tick(event: TickEvent.WorldTickEvent) {
        if (event.world is ServerLevel) {
            val server = event.world.server ?: return
            if (event.world == server.overworld() && event.phase == TickEvent.Phase.END && (event.world.gameTime % 20 == 0L)) {
                val bar = bar(server)
                if (bar.value > 0) bar.value--
                else bar.isVisible = false
            }
        }

    }

    fun remaining(server: MinecraftServer): Int {
        return bar(server).value
    }

}