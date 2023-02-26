package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.Hopper
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

    fun preventsSucking(world: Level, hopper: Hopper): Boolean {
        val server = world.server ?: return false
        return isUnbreakable(server, BlockPos(hopper.levelX, hopper.levelY + 1, hopper.levelZ))
    }

}