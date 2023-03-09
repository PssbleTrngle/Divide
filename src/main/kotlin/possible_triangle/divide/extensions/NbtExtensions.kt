package possible_triangle.divide.extensions

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity

fun BlockEntity.tileData(): CompoundTag {
    return extraCustomData
}

fun CompoundTag.putBlockPos(key: String, pos: BlockPos) = put(key, pos.toNbt())

fun BlockPos.toNbt() = NbtUtils.writeBlockPos(this)

fun CompoundTag.toBlockPos() = NbtUtils.readBlockPos(this)

fun CompoundTag.readBlockPos(key: String) = getCompound(key).toBlockPos()

fun Iterable<Component>.toLoreTag(): ListTag {
    return map { Component.Serializer.toJson(it) }
        .mapTo(ListTag()) { StringTag.valueOf(it) }
}

fun ItemStack.setLore(vararg lore: String) {
    return setLore(lore.map { Component.literal(it) })
}

fun ItemStack.setLore(vararg lore: Component) {
    setLore(lore.toList())
}

fun ItemStack.setLore(lore: List<Component>) {
    val displayTag = with(orCreateTag) {
        if(contains("display")) getCompound("display")
        else CompoundTag().also { put("display", it) }
    }
    displayTag.put("Lore", lore.toLoreTag())
}