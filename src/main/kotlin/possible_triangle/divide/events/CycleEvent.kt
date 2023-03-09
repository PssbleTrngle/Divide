package possible_triangle.divide.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.extensions.time
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.mixins.TimerAccessor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Suppress("LeakingThis")
abstract class CycleEvent(val id: String) : Countdown(id) {

    abstract val enabled: Boolean

    abstract val startsAfter: Duration

    @Serializable
    private data class Event(val id: String, val action: String, val timesRun: Int? = null, @Contextual val pause: Duration? = null)

    private val callbackId = "${DivideMod.ID}:event_$id"

    private val logger = EventLogger("cycle_event", { Event.serializer() }) { isAdmin() }

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
        return cancel(server, suffix = id)
    }

    fun isRunning(server: MinecraftServer): Boolean {
        return isRunning(server, suffix = id)
    }

    protected open fun onStop(server: MinecraftServer) {}

    fun register() {
        REGISTRY[id] = this
    }

    fun stop(server: MinecraftServer): Boolean {
        onStop(server)
        bar(server).value = 0
        data[server] = null

        logger.log(server, Event(id, "stopped"))

        return cancel(server)
    }

    fun startCycle(server: MinecraftServer, at: Int = 0): Boolean {
        val started = !startsAfter.isPositive()
        val pause = if (started) handle(server, at) else startsAfter

        val alreadyRun = cancel(server)
        if (started) data[server] = at
        schedule(server, pause, if (started) at + 1 else at)

        logger.log(server, Event(id, if (alreadyRun) "restarted" else "started"))

        return alreadyRun
    }

    fun skip(server: MinecraftServer) {
        val queue = server.scheduledEvents()
        val accessor = queue as TimerAccessor<MinecraftServer>
        val event = accessor.events.row(callbackId).values.toList()
        cancel(server)
        onStop(server)

        logger.log(server, Event(id, "skipped"))

        event.forEach {
            it.callback.handle(server, queue, server.time())
        }
    }

    private fun schedule(server: MinecraftServer, duration: Duration, index: Int, invisible: Boolean = false) {
        cancel(server)
        val bar = bar(server)
        if (invisible) {
            bar.clearPlayers()
        } else {
            countdown(server, duration)
            bar.players = server.participants()
        }
        schedule(server, duration, Callback(index, id), suffix = id)
    }

    abstract fun handle(server: MinecraftServer, index: Int): Duration

    class Callback(val index: Int, val id: String) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            val event = REGISTRY[id] ?: return
            if (event.enabled) {
                val pause = event.handle(server, index)
                event.logger.log(server, Event(id, "scheduled", index, pause))
                event.schedule(server, pause, index + 1)
                event.data[server] = index
            } else {
                event.schedule(server, 10.seconds, index, invisible = true)
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