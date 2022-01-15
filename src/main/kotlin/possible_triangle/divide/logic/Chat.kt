package possible_triangle.divide.logic

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

object Chat {

    fun sound(player: ServerPlayer, sound: ResourceLocation, at: Vec3 = player.position(), volume: Float = 1F, pitch: Float = 1F) {
        val packet = ClientboundCustomSoundPacket(sound,SoundSource.MASTER,at,volume,  pitch)
        player.connection.send(packet)
    }

    fun message(player: ServerPlayer, message: String, log: Boolean = false) {
        message(player, TextComponent(message), log)
    }

    fun message(player: ServerPlayer, message: Component, log: Boolean = false) {
        player.sendMessage(message, ChatType.GAME_INFO, player.uuid)
        if (log) player.sendMessage(message, ChatType.SYSTEM, player.uuid)
    }

    fun title(player: ServerPlayer, message: String) {
        title(player, TextComponent(message))
    }

    fun title(player: ServerPlayer, message: Component) {
        player.connection.send(ClientboundSetTitleTextPacket(message))
    }

    fun subtitle(player: ServerPlayer, message: String, setTitle: Boolean = true) {
        subtitle(player, TextComponent(message), setTitle)
    }

    fun subtitle(player: ServerPlayer, message: Component, setTitle: Boolean = true) {
        if (setTitle) title(player, "")
        player.connection.send(ClientboundSetSubtitleTextPacket(message))
    }

}