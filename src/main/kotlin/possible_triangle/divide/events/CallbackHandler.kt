package possible_triangle.divide.events

import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.world.timer.TimerCallback
import net.minecraft.world.timer.TimerCallbackSerializer
import possible_triangle.divide.DivideMod
import possible_triangle.divide.mixins.TimerAccessor

abstract class CallbackHandler<T : TimerCallback<MinecraftServer>>(private val id: String, clazz: Class<T>) :
    TimerCallback.Serializer<MinecraftServer, T>(
        Identifier(DivideMod.ID, id),
        clazz,
    ) {

    fun MinecraftServer.scheduledEvents() = saveProperties.mainWorldProperties.scheduledEvents!!

    init {
        @Suppress("LeakingThis")
        TimerCallbackSerializer.INSTANCE.registerSerializer(this)
    }

    private fun callbackId(suffix: String = ""): String {
        return Identifier(DivideMod.ID, id + if (suffix.isEmpty()) "" else "_${suffix}").toString()
    }

    fun isRunning(server: MinecraftServer, suffix: String = ""): Boolean {
        val accessor = server.scheduledEvents() as TimerAccessor<*>
        return accessor.eventsByName.containsRow(callbackId(suffix))
    }

    fun cancel(server: MinecraftServer, suffix: String = ""): Boolean {
        val cleared = isRunning(server)
        server.scheduledEvents().remove(callbackId(suffix))
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
        server.scheduledEvents().setEvent(
            callbackId(suffix),
            server.overworld.time + seconds * 20,
            callback,
        )
    }

}