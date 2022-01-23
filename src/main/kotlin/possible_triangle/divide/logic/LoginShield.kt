package possible_triangle.divide.logic

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.GameData
import possible_triangle.divide.data.Util
import java.util.*

@Mod.EventBusSubscriber
object LoginShield {

    private var players = mapOf<UUID, Int>()

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        if (Util.shouldSkip(event, { event.world })) return
        players = players
            .mapValues { it.value - 1 }
            .filterValues { it > 0 }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun damage(event: LivingDamageEvent) {
        val player = event.entity
        if (player !is ServerPlayer) return
        if (GameData.DATA[player.server].paused) event.amount = 0F
        else if (players.containsKey(player.uuid)) event.amount = 0F
    }

}