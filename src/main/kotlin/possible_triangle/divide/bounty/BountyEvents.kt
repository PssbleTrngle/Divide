package possible_triangle.divide.bounty

import io.github.fabricators_of_create.porting_lib.event.common.AdvancementCallback
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.missions.Mission

object BountyEvents {

    init {
        AdvancementCallback.EVENT.register { player, advancement ->
            if (player !is ServerPlayerEntity) return@register

            if (advancement.id.path.startsWith("recipes/")) return@register
            if (advancement.display?.shouldShowToast() != true) return@register
            val announced = advancement.display?.shouldAnnounceToChat() == true

            val alreadyUnlocked = player.teammates(false).any {
                it.advancementTracker.getProgress(advancement).isDone
            }

            if (!alreadyUnlocked) Bounty.ADVANCEMENT.gain(player, if (announced) 1.0 else 0.5)
        }

        BlockEvents.BLOCK_BREAK.register { event ->
            val player = event.player
            if (player !is ServerPlayerEntity) return@register

            val entry = player.server.registryManager.get(RegistryKeys.BLOCK).getEntry(event.state.block)

            val match = when {
                entry.isIn(BlockTags.COAL_ORES) -> Bounty.MINED_COAL
                entry.isIn(BlockTags.IRON_ORES) -> Bounty.MINED_IRON
                entry.isIn(BlockTags.GOLD_ORES) -> Bounty.MINED_GOLD
                entry.isIn(BlockTags.DIAMOND_ORES) -> Bounty.MINED_DIAMOND
                entry.isIn(BlockTags.EMERALD_ORES) -> Bounty.MINED_EMERALD
                Blocks.ANCIENT_DEBRIS == event.state.block -> Bounty.MINED_NETHERITE
                else -> null
            }

            Mission.FIND.filterKeys { it(event.state) }
                .map { it.value.getValue(null, null) }
                .forEach { it.fulfill(player) }


            match?.gain(player)
        }
    }

}