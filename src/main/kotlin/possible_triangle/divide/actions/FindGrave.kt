package possible_triangle.divide.actions

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import possible_triangle.divide.data.Util
import possible_triangle.divide.hacks.DataHacker.Type.GLOWING
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.DeathEvents
import possible_triangle.divide.reward.RewardContext

object FindGrave : DataAction(GLOWING) {

    private val NOT_DIED = SimpleCommandExceptionType(TextComponent("You have not died yet"))

    override fun <T> onPrepare(ctx: RewardContext<T>) {
        DeathEvents.getDeathPos(ctx.player ?: return) ?: throw NOT_DIED.create()
    }

    override fun <T> onStart(ctx: RewardContext<T>) {
        ctx.player?.let { player ->
            val pos = DeathEvents.getDeathPos(player) ?: return@let
            val timeDead = DeathEvents.timeSinceDeath(player)
            val minutes = timeDead / 20 / 60
            Chat.message(
                player,
                TextComponent("You died at ").append(
                    Util.encodePos(pos, player)
                ).append(TextComponent(" ${minutes}m ago")),
                log = true
            )
        }
    }

    override fun <T> targets(ctx: RewardContext<T>): List<Entity> {
        val pos = DeathEvents.getDeathPos(ctx.player ?: return emptyList()) ?: return emptyList()
        return ctx.server.allLevels.map {
            it.getEntitiesOfClass(ItemEntity::class.java, AABB(pos).inflate(5.0))
        }.flatten()
    }

    override fun <T> visibleTo(ctx: RewardContext<T>, target: Entity): List<ServerPlayer> {
        return listOfNotNull(ctx.player)
    }

}