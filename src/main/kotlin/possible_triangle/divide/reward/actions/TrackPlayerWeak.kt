package possible_triangle.divide.reward.actions

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Util
import possible_triangle.divide.data.Util.persistentData
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.reward.RewardContext

object TrackPlayerWeak : DataAction(GLOWING) {

    private const val TARGET_TAG = "${DivideMod.ID}:tracking"

    private fun getMarker(target: ServerPlayerEntity): Entity? {
        val id = target.persistentData().getInt(TARGET_TAG)
        return target.world.getEntityById(id)
    }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        return ctx.targetPlayers().mapNotNull { getMarker(it) }
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayerEntity> {
        return (ctx.player ?: return emptyList()).teammates()
    }

    override fun <T> onStop(ctx: RewardContext<T>) {
        return ctx.targetPlayers().mapNotNull { getMarker(it) }.forEach {
            it.remove(Entity.RemovalReason.DISCARDED)
        }
    }

    override fun <T> onPrepare(ctx: RewardContext<T>) {
        TrackPlayer.checkRequirements(ctx)

        val target = ctx.targetPlayer() ?: return

        val pos = target.blockPos.up()
        val marker = Util.spawnMarker(EntityType.SLIME, target.getWorld(), pos) {
            it.putInt("Size", 0)
            it.putUuid(TARGET_TAG, target.uuid)
        }

        Util.withoutCollision(marker, ctx.server, target.participantTeam())
        target.persistentData().putInt(TARGET_TAG, marker.id)

        Chat.subtitle(target, "Your position will be recorded in ${ctx.reward.charge}s")
        ctx.team.participants(ctx.server).forEach {
            Chat.subtitle(it, "Tracking in ${ctx.reward.charge}s")
        }
    }

}