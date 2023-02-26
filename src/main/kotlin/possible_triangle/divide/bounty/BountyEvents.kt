package possible_triangle.divide.bounty

import net.minecraft.advancements.Advancement
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import possible_triangle.divide.extensions.getHolderOrThrow
import possible_triangle.divide.extensions.isIn
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.missions.Mission

object BountyEvents {

    fun onAdvancement(player: ServerPlayer, advancement: Advancement) {
        if (advancement.id.path.startsWith("recipes/")) return
        if (advancement.display?.shouldShowToast() != true) return
        val announced = advancement.display?.shouldAnnounceChat() == true

        val alreadyUnlocked = player.teammates(false).any {
            it.advancements.getOrStartProgress(advancement).isDone
        }

        if (!alreadyUnlocked) Bounty.ADVANCEMENT.gain(player, if (announced) 1.0 else 0.5)
    }

    fun onBlockBreak(player: ServerPlayer, state: BlockState) {
        val entry = player.server.registryAccess().registryOrThrow(Registries.BLOCK).getHolderOrThrow(state.block)

        val match = when {
            entry.isIn(BlockTags.COAL_ORES) -> Bounty.MINED_COAL
            entry.isIn(BlockTags.IRON_ORES) -> Bounty.MINED_IRON
            entry.isIn(BlockTags.GOLD_ORES) -> Bounty.MINED_GOLD
            entry.isIn(BlockTags.DIAMOND_ORES) -> Bounty.MINED_DIAMOND
            entry.isIn(BlockTags.EMERALD_ORES) -> Bounty.MINED_EMERALD
            Blocks.ANCIENT_DEBRIS == state.block -> Bounty.MINED_NETHERITE
            else -> null
        }

        Mission.FIND.filterKeys { it(state) }
            .map { it.value.getValue(null, null) }
            .forEach { it.fulfill(player) }

        match?.gain(player)
    }
}