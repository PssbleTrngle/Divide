package possible_triangle.divide.logic

import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.GameData
import java.util.*

object LoginShield {

    private var players = mapOf<UUID, Int>()

    init {
        ServerTickEvents.START_SERVER_TICK.register { server ->
            if (server.overworld.time % 20 != 0L) return@register
            players = players
                .mapValues { it.value - 1 }
                .filterValues { it > 0 }
        }

        LivingEntityEvents.HURT.register { _, entity, amount ->
            if (entity !is ServerPlayerEntity) return@register amount

            if (GameData.DATA[entity.server].paused) 0F
            else if (players.containsKey(entity.uuid)) 0F
            else amount
        }
    }

}