package possible_triangle.divide.events

import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import possible_triangle.divide.DivideMod
import possible_triangle.divide.GameData
import possible_triangle.divide.data.Util
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

open class Countdown(private val id: String, private val display: String = id.replaceFirstChar { it.titlecase() }) {

    init {
        FORGE_BUS.addListener(::tick)
        FORGE_BUS.addListener(::playerJoin)
    }

    fun countdown(
        server: MinecraftServer,
        Seconds: Int,
        resetPlayers: Boolean = true,
        consumer: CustomBossEvent.() -> Unit = {}
    ) {
        val bar = bar(server)
        if (resetPlayers) bar.players = listOf()
        bar.max = Seconds
        bar.value = Seconds
        consumer(bar)
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
        if (Util.shouldSkip(event, { it.world })) return
        val server = event.world.server ?: return
        if (GameData.DATA[server].paused) return

        val bar = bar(server)
        if (bar.value > 0) bar.value--
        else bar.isVisible = false
    }

    private fun playerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.player
        if (player !is ServerPlayer) return
        bar(player.server).addPlayer(player)
    }

    fun remaining(server: MinecraftServer): Int {
        return bar(server).value
    }

}