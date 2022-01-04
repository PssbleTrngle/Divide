package possible_triangle.divide.logic.actions

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Entity
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.Action
import kotlin.experimental.or

object FindGrave : Action {

    override fun start(ctx: Reward.Context) {
        val data = ctx.player.entityData
        val b0 = data.get(Entity.DATA_SHARED_FLAGS_ID)
        data.set(Entity.DATA_SHARED_FLAGS_ID, ((b0 or 1).toInt() shl 6).toByte())
        ctx.player.connection.send(ClientboundSetEntityDataPacket(ctx.player.id, data, false))
    }

}