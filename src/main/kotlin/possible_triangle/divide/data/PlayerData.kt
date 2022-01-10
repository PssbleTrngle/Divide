package possible_triangle.divide.data

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

object PlayerData {

    fun persistentData(player: ServerPlayer): CompoundTag {
        if (!player.persistentData.contains(ServerPlayer.PERSISTED_NBT_TAG)) player.persistentData.put(
            ServerPlayer.PERSISTED_NBT_TAG,
            CompoundTag()
        )
        return player.persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG)
    }

}