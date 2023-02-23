package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import possible_triangle.divide.data.Util
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.reward.RewardContext

object FindGrave : DataAction(GLOWING) {

    private val NOT_DIED = SimpleCommandExceptionType(Text.literal("You have not died yet"))

    override fun <T> onPrepare(ctx: RewardContext<T>) {
        DeathEvents.getDeathPos(ctx.player ?: return) ?: throw NOT_DIED.create()
    }

    override fun <T> onStart(ctx: RewardContext<T>) {
        ctx.targetPlayer()?.let { player ->
            val pos = DeathEvents.getDeathPos(player) ?: return@let
            val timeDead = DeathEvents.timeSinceDeath(player)
            val minutes = timeDead / 20 / 60
            Chat.message(
                player,
                Text.literal("You died at ").append(
                    Util.encodePos(pos, player)
                ).append(" ${minutes}m ago"),
                log = true
            )
        }
    }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        val pos = DeathEvents.getDeathPos(ctx.player ?: return emptyList()) ?: return emptyList()
        return ctx.server.worlds.map {
            it.getEntitiesByClass(ItemEntity::class.java, Box(pos).expand(5.0)) { true }
        }.flatten()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayerEntity> {
        return listOfNotNull(ctx.player)
    }

}