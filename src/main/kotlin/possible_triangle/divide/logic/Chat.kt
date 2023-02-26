package possible_triangle.divide.logic

import io.github.fabricators_of_create.porting_lib.event.common.PlayerTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import possible_triangle.divide.extensions.time
import java.util.*

object Chat {

    private val MESSAGES = QueuedChat { player, msg -> player.sendSystemMessage(msg, false) }
    private val ACTIONBAR = QueuedChat { player, msg -> player.connection.send(ClientboundSetActionBarTextPacket(msg)) }
    private val TITLE = QueuedChat { player, msg -> player.connection.send(ClientboundSetTitleTextPacket(msg)) }
    private val SUBTITLE = QueuedChat { player, msg -> player.connection.send(ClientboundSetSubtitleTextPacket(msg)) }

    fun apply(string: Any, vararg styles: ChatFormatting): String {
        return "${styles.joinToString(separator = "") { "§${it.char}" }}$string§r"
    }

    fun sound(
        player: ServerPlayer,
        sound: ResourceLocation,
        at: Vec3 = player.position(),
        volume: Float = 1F,
        pitch: Float = 1F,
    ) {
        val entry = Holder.direct(SoundEvent.createVariableRangeEvent(sound))
        val packet =
            ClientboundSoundPacket(entry, SoundSource.MASTER, at.x, at.y, at.z, volume, pitch, player.random.nextLong())
        player.connection.send(packet)
    }

    fun message(player: ServerPlayer, message: String, log: Boolean = false) {
        message(player, Component.literal(message), log)
    }

    fun message(player: ServerPlayer, message: Component, log: Boolean = false) {
        ACTIONBAR.send(player, message)
        if (log) MESSAGES.send(player, message)
    }

    fun title(player: ServerPlayer, message: String) {
        title(player, Component.literal(message))
    }

    fun title(player: ServerPlayer, message: Component) {
        TITLE.send(player, message)
    }

    fun subtitle(player: ServerPlayer, message: String, setTitle: Boolean = true) {
        subtitle(player, Component.literal(message), setTitle)
    }

    fun subtitle(player: ServerPlayer, message: Component, setTitle: Boolean = true) {
        if (setTitle) TITLE.send(player, Component.literal(""))
        SUBTITLE.send(player, message)
    }

    class QueuedChat(private val consumer: (ServerPlayer, Component) -> Unit) {

        private val queue = hashMapOf<UUID, Queue<Component>>()

        init {
            PlayerTickEvents.END.register { player ->
                if (player.level.time() % 10 != 0L) return@register

                if (player is ServerPlayer) {
                    val message = queue[player.uuid]?.poll()
                    if (message != null) consumer(player, message)
                }
            }
        }

        fun send(to: ServerPlayer, message: Component) {
            queue.getOrPut(to.uuid) { LinkedList() }.offer(message)
        }
    }

}