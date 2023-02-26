package possible_triangle.divide.extensions

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

fun <T : Any> Registry<T>.getHolderOrThrow(value: T): Holder.Reference<T> {
    return getHolderOrThrow(getResourceKey(value).orElseThrow())
}

@Deprecated("use RegistryAccess instead")
fun ItemLike.id() = BuiltInRegistries.ITEM.getKey(asItem())

@Deprecated("use RegistryAccess instead")
fun ItemLike.stringId() = id().toString()

fun <T : Any> Holder<T>.isIn(tag: TagKey<T>) = `is`(tag)
fun <T : Any> Holder<T>.isOf(value: T) = value == this

fun BlockState.isIn(tag: TagKey<Block>) = `is`(tag)
fun BlockState.isOf(value: Block) = `is`(value)

fun ItemStack.isIn(tag: TagKey<Item>) = `is`(tag)
fun ItemStack.isOf(value: Item) = `is`(value)