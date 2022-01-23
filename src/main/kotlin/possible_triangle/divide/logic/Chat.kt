package possible_triangle.divide.logic

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import net.minecraftforge.event.TickEvent
import possible_triangle.divide.data.Util
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.util.*

object Chat {

    private val MESSAGES = QueuedChat { player, msg -> player.sendMessage(msg, ChatType.SYSTEM, player.uuid) }
    private val ACTIONBAR = QueuedChat { player, msg -> player.sendMessage(msg, ChatType.GAME_INFO, player.uuid) }
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
        pitch: Float = 1F
    ) {
        val packet = ClientboundCustomSoundPacket(sound, SoundSource.MASTER, at, volume, pitch)
        player.connection.send(packet)
    }

    fun message(player: ServerPlayer, message: String, log: Boolean = false) {
        message(player, TextComponent(message), log)
    }

    fun message(player: ServerPlayer, message: Component, log: Boolean = false) {
        ACTIONBAR.send(player, message)
        if (log) MESSAGES.send(player, message)
    }

    fun title(player: ServerPlayer, message: String) {
        title(player, TextComponent(message))
    }

    fun title(player: ServerPlayer, message: Component) {
        TITLE.send(player, message)
    }

    fun subtitle(player: ServerPlayer, message: String, setTitle: Boolean = true) {
        subtitle(player, TextComponent(message), setTitle)
    }

    fun subtitle(player: ServerPlayer, message: Component, setTitle: Boolean = true) {
        if (setTitle) TITLE.send(player, TextComponent(""))
        SUBTITLE.send(player, message)
    }

    class QueuedChat(private val consumer: (ServerPlayer, Component) -> Unit) {

        private val queue = hashMapOf<UUID, Queue<Component>>()

        fun tick(event: TickEvent.PlayerTickEvent) {
            if (Util.shouldSkip(event, { it.player.level }, ticks = 10)) return

            val player = event.player
            if (player is ServerPlayer) {
                val message = queue[player.uuid]?.poll()
                if (message != null) consumer(player, message)
            }
        }

        init {
            FORGE_BUS.addListener(::tick)
        }

        fun send(to: ServerPlayer, message: Component) {
            queue.getOrPut(to.uuid) { LinkedList() }.offer(message)
        }
    }

}