package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer

@Serializable
enum class Reward(val display: String, val price: Int) {

    TRACK_PLAYER("Track Player", 1000),
    PEACE_TIME("Peace Time", 500);

    fun buy(player: ServerPlayer) {
        player.sendMessage(TextComponent("Bought $display for $price"), ChatType.GAME_INFO, player.uuid)
    }


}

