package possible_triangle.divide.logic

import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Chat
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Bounty


@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object BountyEvents {

    private val BOUNTY_COUNTS = PerTeamData("bounties")

    fun gain(player: Player, bounty: Bounty, modifier: Double = 1.0) {
        val team = TeamLogic.teamOf(player)

        if (player is ServerPlayer && team != null) {
            val bounties = BOUNTY_COUNTS.get(player.getLevel())
            val alreadyDone = bounties[team]
            val cashGained = (bounty.amount(alreadyDone) * modifier).toInt()

            if (cashGained > 0) {
                CashLogic.modify(player.getLevel(), team, cashGained)

                TeamLogic.teammates(player).forEach { teammate ->
                    //it.sendMessage(TextComponent("You're team gained $cashGained"), ChatType.GAME_INFO, it.uuid)
                    Chat.subtitle(
                        teammate,
                        TextComponent(bounty.display).withStyle { it.withItalic(true) }
                    )
                    Chat.title(teammate, "+$cashGained")
                }
            }

            bounties[team] = alreadyDone + 1
        }
    }

    @SubscribeEvent
    fun onAdvancement(event: AdvancementEvent) {
        val player = event.player
        if (player !is ServerPlayer) return

        if (event.advancement.id.path.startsWith("recipes/")) return

        val alreadyUnlocked = TeamLogic.teammates(player, false).any {
            it.advancements.getOrStartProgress(event.advancement).isDone
        }

        if (!alreadyUnlocked) gain(event.player, Bounty.ADVANCEMENT)
    }

    @SubscribeEvent
    fun onAdvancement(event: BlockEvent.BreakEvent) {

        val match = listOf(
            BlockTags.COAL_ORES::contains to Bounty.MINED_COAL,
            BlockTags.IRON_ORES::contains to Bounty.MINED_IRON,
            BlockTags.GOLD_ORES::contains to Bounty.MINED_GOLD,
            BlockTags.DIAMOND_ORES::contains to Bounty.MINED_DIAMOND,
            BlockTags.EMERALD_ORES::contains to Bounty.MINED_EMERALD,
            Blocks.ANCIENT_DEBRIS::equals to Bounty.MINED_NETHERITE,
        ).find { it.first(event.state.block) }

        if (match != null) gain(event.player, match.second)

    }

}