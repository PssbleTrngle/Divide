package possible_triangle.divide.bounty

import net.minecraft.advancement.Advancement
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.missions.Mission

object BountyEvents {

    fun onAdvancement(player: ServerPlayerEntity, advancement: Advancement) {
        if (advancement.id.path.startsWith("recipes/")) return
        if (advancement.display?.shouldShowToast() != true) return
        val announced = advancement.display?.shouldAnnounceToChat() == true

        val alreadyUnlocked = player.teammates(false).any {
            it.advancementTracker.getProgress(advancement).isDone
        }

        if (!alreadyUnlocked) Bounty.ADVANCEMENT.gain(player, if (announced) 1.0 else 0.5)
    }

    fun onBlockBreak(player: ServerPlayerEntity, state: BlockState) {
        val entry = player.server.registryManager.get(RegistryKeys.BLOCK).getEntry(state.block)

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