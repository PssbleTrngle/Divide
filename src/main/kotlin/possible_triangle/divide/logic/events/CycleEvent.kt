package possible_triangle.divide.logic.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.TeamLogic

@Suppress("LeakingThis")
abstract class CycleEvent(val id: String) : Countdown(id) {

    companion object {
        private val EVENTS = hashMapOf<String, CycleEvent>()

        init {
            TimerCallbacks.SERVER_CALLBACKS.register(Serializer)
        }
    }

    init {
        EVENTS[id] = this
    }

    fun stop(server: MinecraftServer) {
        clear(server)
        bar(server).value = 0
    }

    private fun clear(server: MinecraftServer) {
        server.worldData.overworldData().scheduledEvents.remove("${DivideMod.ID}:$id")
    }

    fun startCycle(server: MinecraftServer, at: Int = 0) {
        val next = handle(server, at)
        schedule(server, next, at + 1)
    }

    fun skip(server: MinecraftServer) {
        val queue = server.worldData.overworldData().scheduledEvents
        val event = queue.events.row("${DivideMod.ID}:$id").values.toList()
        clear(server)
        event.forEach {
            it.callback.handle(server, queue, server.overworld().gameTime)
        }
    }

    private fun schedule(server: MinecraftServer, seconds: Int, index: Int) {
        clear(server)
        countdown(server, seconds)
        bar(server).players = server.playerList.players.filter(TeamLogic::isPlayer)
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:$id",
            server.overworld().gameTime + seconds * 20,
            Callback(index, id)
        )
    }

    abstract fun handle(server: MinecraftServer, index: Int): Int

    class Callback(val index: Int, val id: String) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            val event = EVENTS[id] ?: return
            val pause = event.handle(server, index)
            event.schedule(server, pause, index + 1)
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