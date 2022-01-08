package possible_triangle.divide.crates

import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod

@Mod.EventBusSubscriber
object CrateEvents {

    const val UNBREAKABLE_TAG = "${DivideMod.ID}:unbreakable"
    const val CRATE_TAG = "${DivideMod.ID}:crate"

    @SubscribeEvent
    fun preventBreaking(event: PlayerEvent.BreakSpeed) {
        val crate = CrateScheduler.crateAt(event.entity.server ?: return, event.pos) ?: return
        if (crate.tileData.getBoolean(UNBREAKABLE_TAG)) event.newSpeed = 0F
    }

    @SubscribeEvent
    fun blockBreak(event: BlockEvent.BreakEvent) {
        val server = event.world.server ?: return
        val crate = CrateScheduler.crateAt(server, event.pos)
        if (crate != null) CleanCallback.cleanMarker(server, event.pos)
    }

    @SubscribeEvent
    fun explosion(event: ExplosionEvent.Detonate) {
        val server = event.world.server ?: return
        event.explosion.toBlow.removeIf {
            val crate = CrateScheduler.crateAt(server, it) ?: return@removeIf false
            crate.tileData.getBoolean(UNBREAKABLE_TAG)
        }
    }

    @SubscribeEvent
    fun preventExtraction(event: ) {}

}