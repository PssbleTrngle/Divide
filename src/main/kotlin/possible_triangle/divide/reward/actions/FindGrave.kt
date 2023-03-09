package possible_triangle.divide.reward.actions

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.AABB
import possible_triangle.divide.extensions.toComponent
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.logic.DeathEvents.getDeathPos
import possible_triangle.divide.reward.RewardContext

object FindGrave : DataAction(GLOWING) {

    private val NOT_DIED = SimpleCommandExceptionType(Component.literal("You have not died yet"))

    override fun <T> onPrepare(ctx: RewardContext<T>) {
        (ctx.player ?: return).getDeathPos() ?: throw NOT_DIED.create()
    }

    override fun <T> onStart(ctx: RewardContext<T>) {
        ctx.targetPlayer()?.let { player ->
            val pos = player.getDeathPos() ?: return@let
            val timeDead = DeathEvents.timeSinceDeath(player)
            val minutes = timeDead / 20 / 60
            Chat.message(
                player,
                Component.literal("You died at ").append(
                    pos.toComponent(player)
                ).append(" ${minutes}m ago"),
                log = true
            )
        }
    }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        val pos = (ctx.player ?: return emptyList()).getDeathPos() ?: return emptyList()
        return ctx.server.allLevels.map {
            it.getEntitiesOfClass(ItemEntity::class.java, AABB(pos).inflate(5.0)) { true }
        }.flatten()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer> {
        return listOfNotNull(ctx.player)
    }

}