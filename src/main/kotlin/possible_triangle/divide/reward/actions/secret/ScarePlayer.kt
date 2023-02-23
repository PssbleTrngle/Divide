package possible_triangle.divide.reward.actions.secret

import net.minecraft.util.Identifier
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.makeWeightedDecision
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object ScarePlayer : Action() {

    private val FALL = Identifier("minecraft:entity.player.small_fall")
    private val HURT = Identifier("minecraft:entity.player.hurt")
    private val BREAK = Identifier("minecraft:block.stone.break")

    override fun <T> tick(ctx: RewardContext<T>) {
        val anyTarget = ctx.targetPlayer() ?: return
        if (anyTarget.world.time % 15 != 0L) return

        val sound = makeWeightedDecision(mapOf(
            FALL to 1,
            HURT to 1,
            BREAK to 10,
            null to 5,
        ))


        if (sound != null) ctx.targetPlayers().forEach {
            Chat.sound(it, sound, it.pos.add(1.0, -1.0, 0.0), volume = 0.7F)
        }
    }

}