package possible_triangle.divide.actions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Util
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext
import java.util.*

object TrackPlayerWeak : DataAction<UUID, ServerPlayer>(GLOWING, ActionTarget.PLAYER) {

    private const val TARGET_TAG = "${DivideMod.ID}:tracking"

    private fun getMarker(ctx: RewardContext<UUID, ServerPlayer>): Entity? {
        return ctx.ifComplete { player, target ->
            val id = Util.persistentData(target).getInt(TARGET_TAG)
            player.level.getEntity(id)
        }
    }

    override fun targets(ctx: RewardContext<UUID, ServerPlayer>): List<Entity> {
        return listOfNotNull(getMarker(ctx))
    }

    override fun visibleTo(ctx: RewardContext<UUID, ServerPlayer>, target: Entity): List<ServerPlayer> {
        return Teams.teammates(ctx.player ?: return emptyList())
    }

    override fun onStop(ctx: RewardContext<UUID, ServerPlayer>) {
        getMarker(ctx)?.remove(Entity.RemovalReason.DISCARDED)
    }

    override fun onPrepare(ctx: RewardContext<UUID, ServerPlayer>) {
        TrackPlayer.checkRequirements(ctx)

        ctx.ifComplete { player, target ->
            val pos = target.blockPosition().above()
            val marker = Util.spawnMarker(EntityType.SLIME, target.getLevel(), pos) {
                it.putInt("Size", 0)
                it.putUUID(TARGET_TAG, target.uuid)
            }

            Util.withoutCollision(marker, ctx.server, target.team)
            Util.persistentData(target).putInt(TARGET_TAG, marker.id)

            Chat.subtitle(target, "You will be tracked in ${ctx.reward.charge}s")
            Teams.teammates(player).forEach {
                Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
            }
        }
    }

}