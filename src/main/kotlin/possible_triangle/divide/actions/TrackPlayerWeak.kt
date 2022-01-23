package possible_triangle.divide.actions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Util
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.RewardContext

object TrackPlayerWeak : DataAction(GLOWING) {

    private const val TARGET_TAG = "${DivideMod.ID}:tracking"

    private fun getMarker(ctx: RewardContext): Entity? {
        val id = Util.persistentData(ctx.target).getInt(TARGET_TAG)
        return ctx.player.level.getEntity(id)
    }

    override fun targets(ctx: RewardContext): List<Entity> {
        return listOfNotNull(getMarker(ctx))
    }

    override fun visibleTo(ctx: RewardContext, target: Entity): List<ServerPlayer> {
        return Teams.teammates(ctx.player)
    }

    override fun onStop(ctx: RewardContext) {
        getMarker(ctx)?.remove(Entity.RemovalReason.DISCARDED)
    }

    override fun onPrepare(ctx: RewardContext) {
        TrackPlayer.checkRequirements(ctx)

        val pos = ctx.target.blockPosition().above()
        val marker = Util.spawnMarker(EntityType.SLIME, ctx.target.getLevel(), pos) {
            it.putInt("Size", 0)
            it.putUUID(TARGET_TAG, ctx.target.uuid)
        }

        Util.withoutCollision(marker, ctx.server, ctx.target.team)
        Util.persistentData(ctx.target).putInt(TARGET_TAG, marker.id)

        Chat.subtitle(ctx.target, "You will be tracked in ${ctx.reward.charge}s")
        Teams.teammates(ctx.player).forEach {
            Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
        }
    }

}