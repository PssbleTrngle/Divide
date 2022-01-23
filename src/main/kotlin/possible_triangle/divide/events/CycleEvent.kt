package possible_triangle.divide.events

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Teams

@Suppress("LeakingThis")
abstract class CycleEvent(val id: String) : Countdown(id) {

    @Serializable
    private data class Event(val action: String)

    @Serializable
    private data class RunEvent(val timesRun: Int, val pause: Int, val action: String = "scheduled")

    private val callbackId = "${DivideMod.ID}:event_$id"

    private val logger = EventLogger(id) { Event.serializer() }
    private val runLogger = EventLogger(id) { RunEvent.serializer() }

    companion object : CallbackHandler<Callback>("event", Callback::class.java) {
        private val REGISTRY = hashMapOf<String, CycleEvent>()

        val EVENTS
            get() = REGISTRY.values.toList()

        override fun serialize(nbt: CompoundTag, callback: Callback) {
            nbt.putInt("index", callback.index)
            nbt.putString("id", callback.id)
        }

        override fun deserialize(nbt: CompoundTag): Callback {
            return Callback(nbt.getInt("index"), nbt.getString("id"))
        }
    }

    private fun cancel(server: MinecraftServer): Boolean {
        onCancel(server)
        return cancel(server, suffix = id)
    }

    fun isRunning(server: MinecraftServer): Boolean {
        return isRunning(server, suffix = id)
    }

    open fun onCancel(server: MinecraftServer) {}

    fun register() {
        REGISTRY[id] = this
    }

    fun stop(server: MinecraftServer): Boolean {
        bar(server).value = 0
        data[server] = null

        logger.log(server, Event(action = "stop"))

        return cancel(server)
    }

    fun startCycle(server: MinecraftServer, at: Int = 0): Boolean {
        val next = handle(server, at)
        val alreadyRun = cancel(server)
        data[server] = at
        schedule(server, next, at + 1)

        logger.log(server, Event(action = if (alreadyRun) "restart" else "start"))

        return alreadyRun
    }

    fun skip(server: MinecraftServer) {
        val queue = server.worldData.overworldData().scheduledEvents
        val event = queue.events.row(callbackId).values.toList()
        cancel(server)

        logger.log(server, Event(action = "skip"))

        event.forEach {
            it.callback.handle(server, queue, server.overworld().gameTime)
        }
    }

    private fun schedule(server: MinecraftServer, seconds: Int, index: Int, invisible: Boolean = false) {
        cancel(server)
        val bar = bar(server)
        if (invisible) {
            bar.players = listOf()
        } else {
            countdown(server, seconds)
            bar.players = server.playerList.players.filter(Teams::isPlayer)
        }
        schedule(server, seconds, Callback(index, id), suffix = id)
    }

    abstract fun handle(server: MinecraftServer, index: Int): Int

    abstract fun isEnabled(server: MinecraftServer): Boolean

    class Callback(val index: Int, val id: String) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            val event = REGISTRY[id] ?: return
            if (event.isEnabled(server)) {
                val pause = event.handle(server, index)
                event.runLogger.log(server, RunEvent(index, pause))
                event.schedule(server, pause, index + 1)
                event.data[server] = index
            } else {
                event.schedule(server, 10, index, invisible = true)
            }
        }

    }

    val data = object : ModSavedData<Int?>("current_$id") {
        override fun save(nbt: CompoundTag, value: Int?) {
            nbt.putBoolean("running", value != null)
            if (value != null) nbt.putInt("index", value)
        }

        override fun load(nbt: CompoundTag, server: MinecraftServer): Int? {
            return if (nbt.getBoolean("running")) nbt.getInt("index") else null
        }

        override fun default(): Int? {
            return null
        }
    }

}