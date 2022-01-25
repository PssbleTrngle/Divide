package possible_triangle.divide.events

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import possible_triangle.divide.DivideMod

abstract class CallbackHandler<T : TimerCallback<MinecraftServer>>(private val id: String, clazz: Class<T>) :
    TimerCallback.Serializer<MinecraftServer, T>(
        ResourceLocation(DivideMod.ID, id),
        clazz,
    ) {

    init {
        @Suppress("LeakingThis")
        TimerCallbacks.SERVER_CALLBACKS.register(this)
    }

    private fun callbackId(suffix: String = ""): String {
        return ResourceLocation(DivideMod.ID, id + if (suffix.isEmpty()) "" else "_${suffix}").toString()
    }

    fun isRunning(server: MinecraftServer, suffix: String = ""): Boolean {
        return server.worldData.overworldData().scheduledEvents.eventsIds.contains(callbackId(suffix))
    }

    fun cancel(server: MinecraftServer, suffix: String = ""): Boolean {
        val cleared = isRunning(server)
        server.worldData.overworldData().scheduledEvents.remove(callbackId(suffix))
        return cleared
    }

    fun schedule(
        server: MinecraftServer,
        seconds: Int,
        callback: T,
        suffix: String = "",
        clearPrevious: Boolean = false
    ) {
        if (clearPrevious) cancel(server)
        server.worldData.overworldData().scheduledEvents.schedule(
            callbackId(suffix),
            server.overworld().gameTime + seconds * 20,
            callback,
        )
    }

}