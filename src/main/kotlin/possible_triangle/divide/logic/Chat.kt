package possible_triangle.divide.logic

import io.github.fabricators_of_create.porting_lib.event.common.PlayerTickEvents
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.*

object Chat {

    private val MESSAGES = QueuedChat { player, msg -> player.sendMessage(msg, false) }
    private val ACTIONBAR = QueuedChat { player, msg -> player.sendMessage(msg, true) }
    private val TITLE = QueuedChat { player, msg -> player.networkHandler.sendPacket(TitleS2CPacket(msg)) }
    private val SUBTITLE = QueuedChat { player, msg -> player.networkHandler.sendPacket(SubtitleS2CPacket(msg)) }

    fun apply(string: Any, vararg styles: Formatting): String {
        return "${styles.joinToString(separator = "") { "§${it.code}" }}$string§r"
    }

    fun sound(
        player: ServerPlayerEntity,
        sound: Identifier,
        at: Vec3d = player.pos,
        volume: Float = 1F,
        pitch: Float = 1F,
    ) {
        val entry = RegistryEntry.of<SoundEvent>(SoundEvent.of(sound))
        val packet =
            PlaySoundS2CPacket(entry, SoundCategory.MASTER, at.x, at.y, at.z, volume, pitch, player.random.nextLong())
        player.networkHandler.sendPacket(packet)
    }

    fun message(player: ServerPlayerEntity, message: String, log: Boolean = false) {
        message(player, Text.literal(message), log)
    }

    fun message(player: ServerPlayerEntity, message: Text, log: Boolean = false) {
        ACTIONBAR.send(player, message)
        if (log) MESSAGES.send(player, message)
    }

    fun title(player: ServerPlayerEntity, message: String) {
        title(player, Text.literal(message))
    }

    fun title(player: ServerPlayerEntity, message: Text) {
        TITLE.send(player, message)
    }

    fun subtitle(player: ServerPlayerEntity, message: String, setTitle: Boolean = true) {
        subtitle(player, Text.literal(message), setTitle)
    }

    fun subtitle(player: ServerPlayerEntity, message: Text, setTitle: Boolean = true) {
        if (setTitle) TITLE.send(player, Text.literal(""))
        SUBTITLE.send(player, message)
    }

    class QueuedChat(private val consumer: (ServerPlayerEntity, Text) -> Unit) {

        private val queue = hashMapOf<UUID, Queue<Text>>()

        init {
            PlayerTickEvents.END.register { player ->
                if (player.world.time % 10 != 0L) return@register

                if (player is ServerPlayerEntity) {
                    val message = queue[player.uuid]?.poll()
                    if (message != null) consumer(player, message)
                }
            }
        }

        fun send(to: ServerPlayerEntity, message: Text) {
            queue.getOrPut(to.uuid) { LinkedList() }.offer(message)
        }
    }

}