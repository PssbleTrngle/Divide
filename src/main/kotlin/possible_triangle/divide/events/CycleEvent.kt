package possible_triangle.divide.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.Teams

@Suppress("LeakingThis")
abstract class CycleEvent(val id: String) : Countdown(id) {
    
    private val callbackId = "${DivideMod.ID}:$id"

    companion object {
        private val EVENTS = hashMapOf<String, CycleEvent>()

        init {
            TimerCallbacks.SERVER_CALLBACKS.register(Serializer)
        }
    }

    init {
        EVENTS[id] = this
    }

    fun isRunning(server: MinecraftServer): Boolean {
        return server.worldData.overworldData().scheduledEvents.eventsIds.contains(callbackId)
    }

    fun stop(server: MinecraftServer): Boolean {
        bar(server).value = 0
        return clear(server)
    }

    private fun clear(server: MinecraftServer): Boolean {
        val events = server.worldData.overworldData().scheduledEvents
        val cleared = isRunning(server)
        events.remove(callbackId)
        return cleared
    }

    fun startCycle(server: MinecraftServer, at: Int = 0): Boolean {
        val next = handle(server, at)
        val alreadyRun = clear(server)
        schedule(server, next, at + 1)
        return alreadyRun
    }

    fun skip(server: MinecraftServer) {
        val queue = server.worldData.overworldData().scheduledEvents
        val event = queue.events.row(callbackId).values.toList()
        clear(server)
        event.forEach {
            it.callback.handle(server, queue, server.overworld().gameTime)
        }
    }

    private fun schedule(server: MinecraftServer, seconds: Int, index: Int, invisible: Boolean = false) {
        clear(server)
        val bar = bar(server)
        if (invisible) {
            bar.players = listOf()
        } else {
            countdown(server, seconds)
            bar.players = server.playerList.players.filter(Teams::isPlayer)
        }
        server.worldData.overworldData().scheduledEvents.schedule(
            callbackId,
            server.overworld().gameTime + seconds * 20,
            Callback(index, id)
        )
    }

    abstract fun handle(server: MinecraftServer, index: Int): Int

    abstract fun isEnabled(server: MinecraftServer): Boolean

    class Callback(val index: Int, val id: String) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            val event = EVENTS[id] ?: return
            if (event.isEnabled(server)) {
                event.schedule(server, event.handle(server, index), index + 1)
            } else {
                event.schedule(server, 10, index, invisible = true)
            }
        }

    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, Callback>(
            ResourceLocation(DivideMod.ID, "event"),
            Callback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: Callback) {
            nbt.putInt("index", callback.index)
            nbt.putString("id", callback.id)
        }

        override fun deserialize(nbt: CompoundTag): Callback {
            return Callback(nbt.getInt("index"), nbt.getString("id"))
        }
    }

}