package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.reward.RewardContext

object FindGrave : DataAction(GLOWING, targets = null) {

    private val NOT_DIED = SimpleCommandExceptionType(TextComponent("You have not died yet"))

    private fun getPos(ctx: RewardContext): BlockPos {
        return DeathEvents.getDeathPos(ctx.player) ?: throw NOT_DIED.create()
    }

    override fun onStart(ctx: RewardContext) {
        val pos = getPos(ctx)
        val timeDead = DeathEvents.timeSinceDeath(ctx.player)
        val minutes = timeDead / 20 / 60
        Chat.message(
            ctx.player,
            TextComponent("You died at [${pos.x}/${pos.y}/${pos.z}] ${minutes}m ago").withStyle(ChatFormatting.GOLD),
            log = true
        )
    }

    override fun targets(ctx: RewardContext): List<Entity> {
        return ctx.server.allLevels.map {
            return it.getEntitiesOfClass(ItemEntity::class.java, AABB(getPos(ctx)).inflate(5.0))
        }.flatten()
    }

    override fun visibleTo(ctx: RewardContext, target: Entity): List<ServerPlayer> {
        return listOf(ctx.player)
    }

}