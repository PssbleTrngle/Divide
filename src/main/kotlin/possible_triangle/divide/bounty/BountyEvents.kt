package possible_triangle.divide.bounty

import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.missions.Mission


@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object BountyEvents {

    @SubscribeEvent
    fun onAdvancement(event: AdvancementEvent) {
        val player = event.player
        if (player !is ServerPlayer) return

        if (event.advancement.id.path.startsWith("recipes/")) return
        if (event.advancement.display?.shouldShowToast() != true) return
        val announced = event.advancement.display?.shouldAnnounceChat() == true

        val alreadyUnlocked = Teams.teammates(player, false).any {
            it.advancements.getOrStartProgress(event.advancement).isDone
        }

        if (!alreadyUnlocked) Bounty.ADVANCEMENT.gain(event.player, if (announced) 1.0 else 0.5)
    }

    @SubscribeEvent
    fun onMined(event: BlockEvent.BreakEvent) {
        val player = event.player
        if (player !is ServerPlayer) return

        val match = listOf(
            BlockTags.COAL_ORES::contains to Bounty.MINED_COAL,
            BlockTags.IRON_ORES::contains to Bounty.MINED_IRON,
            BlockTags.GOLD_ORES::contains to Bounty.MINED_GOLD,
            BlockTags.DIAMOND_ORES::contains to Bounty.MINED_DIAMOND,
            BlockTags.EMERALD_ORES::contains to Bounty.MINED_EMERALD,
            Blocks.ANCIENT_DEBRIS::equals to Bounty.MINED_NETHERITE,
        ).find { it.first(event.state.block) }

        Mission.FIND.filterKeys { it(event.state) }
            .map { it.value.getValue(null, null) }
            .forEach { it.fulfill(player) }


        match?.second?.gain(player)

    }

}