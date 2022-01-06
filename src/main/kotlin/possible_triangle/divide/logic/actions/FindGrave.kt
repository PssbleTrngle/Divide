package possible_triangle.divide.logic.actions

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import possible_triangle.divide.Chat
import possible_triangle.divide.logic.DeathLogic
import possible_triangle.divide.reward.RewardContext

object FindGrave : GlowingAction() {

    private val NOT_DIED = SimpleCommandExceptionType(TextComponent("You have not died yet"))

    private fun getPos(ctx: RewardContext): BlockPos {
        return DeathLogic.getDeathPos(ctx.player) ?: throw NOT_DIED.create()
    }

    override fun onStart(ctx: RewardContext) {
        val pos = getPos(ctx)
        Chat.message(ctx.player,  TextComponent("You died at [${pos.x}/${pos.y}/${pos.z}]").withStyle(ChatFormatting.GOLD), status = false)
    }

    override fun targets(ctx: RewardContext): List<Entity> {
        return ctx.world.getEntitiesOfClass(ItemEntity::class.java, AABB(getPos(ctx)).inflate(5.0))
    }

    override fun visibleTo(ctx: RewardContext): List<ServerPlayer> {
        return listOf(ctx.player)
    }

}