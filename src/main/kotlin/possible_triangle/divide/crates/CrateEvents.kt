package possible_triangle.divide.crates

import net.minecraft.block.entity.Hopper
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler.crateAt
import possible_triangle.divide.extensions.tileData

object CrateEvents {

    const val UNBREAKABLE_TAG = "${DivideMod.ID}:unbreakable"
    const val CRATE_UUID_TAG = "${DivideMod.ID}:crate"

    fun isUnbreakable(server: MinecraftServer, pos: BlockPos): Boolean {
        val crate = crateAt(server, pos) ?: return false
        return crate.tileData().getBoolean(UNBREAKABLE_TAG)
    }

    fun modifyExplosion(server: MinecraftServer, blocks: MutableList<BlockPos>) {
        blocks.removeIf { isUnbreakable(server, it) }
    }

    fun preventsSucking(world: World, hopper: Hopper): Boolean {
        val server = world.server ?: return false
        return isUnbreakable(server, BlockPos(hopper.hopperX, hopper.hopperY + 1, hopper.hopperZ))
    }

}