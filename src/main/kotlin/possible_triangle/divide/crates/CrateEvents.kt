package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.Hopper
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler.crateAt
import possible_triangle.divide.crates.callbacks.CleanCallback

@Mod.EventBusSubscriber
object CrateEvents {

    const val UNBREAKABLE_TAG = "${DivideMod.ID}:unbreakable"
    const val CRATE_UUID_TAG = "${DivideMod.ID}:crate"

    @SubscribeEvent
    fun preventBreaking(event: PlayerEvent.BreakSpeed) {
        val crate = crateAt(event.entity.server ?: return, event.pos) ?: return
        if (crate.tileData.getBoolean(UNBREAKABLE_TAG)) event.newSpeed = 0F
    }

    @SubscribeEvent
    fun blockBreak(event: BlockEvent.BreakEvent) {
        val server = event.world.server ?: return
        val crate = crateAt(server, event.pos)
        if (crate != null) CleanCallback.cleanMarker(server, event.pos)
    }

    @SubscribeEvent
    fun explosion(event: ExplosionEvent.Detonate) {
        val server = event.world.server ?: return
        event.explosion.toBlow.removeIf {
            val crate = crateAt(server, it) ?: return@removeIf false
            crate.tileData.getBoolean(UNBREAKABLE_TAG)
        }
    }

    fun preventsSucking(world: Level, hopper: Hopper): Boolean {
        val server = world.server ?: return false
        val crate = crateAt(server, BlockPos(hopper.levelX, hopper.levelY + 1, hopper.levelZ)) ?: return false
        return crate.tileData.getBoolean(UNBREAKABLE_TAG)
    }

}