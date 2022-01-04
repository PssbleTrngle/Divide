package possible_triangle.divide.logic

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.Chat
import possible_triangle.divide.DivideMod

object Border {

    init {
        TimerCallbacks.SERVER_CALLBACKS.register(Serializer)
    }

    private const val LOBBY = 20
    private const val MIN = 100
    private const val MAX = 400
    private const val CYCLE_TIME = 60 * 10

    private val QUEUE = listOf(MIN to CYCLE_TIME, MAX to CYCLE_TIME)

    private fun resize(server: MinecraftServer, size: Int, seconds: Int = 0, message: Boolean = true) {
        val worldborder = server.overworld().worldBorder
        if (worldborder.size == size.toDouble()) return
        worldborder.lerpSizeBetween(worldborder.size, size.toDouble(), 20L * seconds)
        val verb = if (worldborder.size < size.toDouble()) "grow" else "shrink"
        if (message)
            TeamLogic.players(server.overworld()).forEach {
                Chat.sendMessage(it, "border started to $verb")
            }
    }

    fun lobby(server: MinecraftServer) {
        resize(server, LOBBY, message = false)
        server.overworld().worldBorder.damagePerBlock = 0.0
    }

    fun startCycle(server: MinecraftServer) {
        resize(server, MAX, 60)
        schedule(server, 300)
    }

    private fun schedule(server: MinecraftServer, seconds: Int, index: Int = 0) {
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:border",
            server.overworld().gameTime + seconds * 20,
            BorderCallback(index)
        )
    }

    class BorderCallback(val index: Int) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
            server.overworld().worldBorder.damagePerBlock = 2.0
            server.overworld().worldBorder.damageSafeZone = 2.0

            val (value, pause) = QUEUE[index % QUEUE.size]
            resize(server, value, 300)

            schedule(server, pause, (index + 1) % QUEUE.size)
        }

    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, BorderCallback>(
            ResourceLocation(DivideMod.ID, "border"),
            BorderCallback::class.java
        ) {

        override fun serialize(tag: CompoundTag, callback: BorderCallback) {
            tag.putInt("index", callback.index)
        }

        override fun deserialize(tag: CompoundTag): BorderCallback {
            return BorderCallback(tag.getInt("index"))
        }
    }

}