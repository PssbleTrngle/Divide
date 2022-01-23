package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.RewardContext

object FindGrave : DataAction<Unit, Unit>(GLOWING, ActionTarget.NONE) {

    private val NOT_DIED = SimpleCommandExceptionType(TextComponent("You have not died yet"))

    override fun onPrepare(ctx: RewardContext<Unit, Unit>) {
        DeathEvents.getDeathPos(ctx.player ?: return) ?: throw NOT_DIED.create()
    }

    override fun onStart(ctx: RewardContext<Unit, Unit>) {
        ctx.ifComplete { player, _ ->
            val pos = DeathEvents.getDeathPos(player) ?: return@ifComplete null
            val timeDead = DeathEvents.timeSinceDeath(player)
            val minutes = timeDead / 20 / 60
            Chat.message(
                player,
                TextComponent("You died at [${pos.x}/${pos.y}/${pos.z}] ${minutes}m ago").withStyle(ChatFormatting.GOLD),
                log = true
            )
        }
    }

    override fun targets(ctx: RewardContext<Unit, Unit>): List<Entity> {
        return ctx.ifComplete { player, _ ->
            val pos = DeathEvents.getDeathPos(player) ?: return@ifComplete null
            ctx.server.allLevels.map {
                it.getEntitiesOfClass(ItemEntity::class.java, AABB(pos).inflate(5.0))
            }.flatten()
        } ?: listOf()
    }

    override fun visibleTo(ctx: RewardContext<Unit, Unit>, target: Entity): List<ServerPlayer> {
        return listOfNotNull(ctx.player)
    }

}