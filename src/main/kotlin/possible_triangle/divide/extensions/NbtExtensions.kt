package possible_triangle.divide.extensions

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.entity.BlockEntity

fun BlockEntity.tileData(): CompoundTag {
    return extraCustomData
}

fun CompoundTag.putBlockPos(key: String, pos: BlockPos) = put(key, pos.toNbt())

fun BlockPos.toNbt() = NbtUtils.writeBlockPos(this)

fun CompoundTag.toBlockPos() = NbtUtils.readBlockPos(this)

fun CompoundTag.readBlockPos(key: String) = getCompound(key).toBlockPos()