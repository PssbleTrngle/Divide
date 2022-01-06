package possible_triangle.divide.logic.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import possible_triangle.divide.DivideMod
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

abstract class CycleEvent(val id: String) {

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        if (event.world is ServerLevel) {
            val server = event.world.server ?: return
            if (event.world == server.overworld() && event.phase == TickEvent.Phase.END && (event.world.gameTime % 20 == 0L)) {
                val bar = bar(server)
                if (bar.value > 0) bar.value --
            }
        }

    }

    fun bar(server: MinecraftServer): CustomBossEvent {
        val name = ResourceLocation(DivideMod.ID, id)
        return server.customBossEvents.get(name) ?: server.customBossEvents.create(name, TextComponent(id))
    }

    init {
        TimerCallbacks.SERVER_CALLBACKS.register(Serializer())
        FORGE_BUS.addListener(::tick)
    }

    fun stop(server: MinecraftServer) {
        server.worldData.overworldData().scheduledEvents.remove("${DivideMod.ID}:$id")
    }

    fun startCycle(server: MinecraftServer, at: Int = 0) {
        stop(server)
        val next = handle(server, at)
        schedule(server, next, at + 1)
    }

    fun skip(server: MinecraftServer) {
        val queue = server.worldData.overworldData().scheduledEvents
        val event = queue.events.row("${DivideMod.ID}:$id").values.toList()
        stop(server)
        event.forEach {
            it.callback.handle(server, queue, server.overworld().gameTime)
        }
    }

    private fun schedule(server: MinecraftServer, seconds: Int, index: Int) {
        stop(server)
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:$id",
            server.overworld().gameTime + seconds * 20,
            Callback(index)
        )
    }

    abstract fun handle(server: MinecraftServer, index: Int): Int

    inner class Callback(val index: Int) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            server.overworld().worldBorder.damagePerBlock = 2.0
            server.overworld().worldBorder.damageSafeZone = 2.0

            val pause = handle(server, index)
            val bar = bar(server)
            bar.max = pause
            bar.value = pause

            schedule(server, pause, index + 1)
        }

    }

    inner class Serializer :
        TimerCallback.Serializer<MinecraftServer, CycleEvent.Callback>(
            ResourceLocation(DivideMod.ID, id),
            Callback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: Callback) {
            nbt.putInt("index", callback.index)
        }

        override fun deserialize(nbt: CompoundTag): Callback {
            return Callback(nbt.getInt("index"))
        }
    }

}