package possible_triangle.divide.events

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import possible_triangle.divide.DivideMod
import possible_triangle.divide.extensions.inTicks
import possible_triangle.divide.extensions.time
import possible_triangle.divide.mixins.TimerAccessor
import kotlin.time.Duration

abstract class CallbackHandler<T : TimerCallback<MinecraftServer>>(private val id: String, clazz: Class<T>) :
    TimerCallback.Serializer<MinecraftServer, T>(
        ResourceLocation(DivideMod.ID, id),
        clazz,
    ) {

    fun MinecraftServer.scheduledEvents() = worldData.overworldData().scheduledEvents!!

    init {
        @Suppress("LeakingThis")
        TimerCallbacks.SERVER_CALLBACKS.register(this)
    }

    private fun callbackId(suffix: String = ""): String {
        return ResourceLocation(DivideMod.ID, id + if (suffix.isEmpty()) "" else "_${suffix}").toString()
    }

    fun isRunning(server: MinecraftServer, suffix: String = ""): Boolean {
        val accessor = server.scheduledEvents() as TimerAccessor<*>
        return accessor.events.containsRow(callbackId(suffix))
    }

    fun cancel(server: MinecraftServer, suffix: String = ""): Boolean {
        val cleared = isRunning(server)
        server.scheduledEvents().remove(callbackId(suffix))
        return cleared
    }

    fun schedule(
        server: MinecraftServer,
        duration: Duration,
        callback: T,
        suffix: String = "",
        clearPrevious: Boolean = false
    ) {
        if (clearPrevious) cancel(server)
        server.scheduledEvents().schedule(
            callbackId(suffix),
            server.time() + duration.inTicks,
            callback,
        )
    }

}