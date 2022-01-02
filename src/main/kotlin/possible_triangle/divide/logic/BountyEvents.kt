package possible_triangle.divide.logic

import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Bounty


@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object BountyEvents {

    private val BOUNTY_COUNTS = PerTeamData("bounties")

    fun gain(player: Player, bounty: Bounty) {
        val team = TeamLogic.teamOf(player)
        val world = player.level

        if (world is ServerLevel && team != null) {
        val bounties = BOUNTY_COUNTS.get(world)
            val alreadyDone = bounties[team]
            val cashGained = bounty.amount(alreadyDone)

            TeamLogic.players(world).filter { it.team == team }.forEach {
                //it.sendMessage(TextComponent("You're team gained $cashGained"), ChatType.GAME_INFO, it.uuid)
                it.connection.send(ClientboundSetTitleTextPacket(TextComponent("+$cashGained")))
                it.connection.send(
                    ClientboundSetSubtitleTextPacket(
                        TextComponent(bounty.display).setStyle(
                            Style.EMPTY.withItalic(
                                true
                            )
                        )
                    )
                )
            }

            bounties[team] = alreadyDone + 1
        }
    }

    @SubscribeEvent
    fun onAdvancement(event: AdvancementEvent) {
        val world = event.player.level
        if (world !is ServerLevel) return

        val alreadyUnlocked = TeamLogic.players(world)
            .filter { it != event.player }
            .filter { it.team == event.player.team }
            .any { it.advancements.getOrStartProgress(event.advancement).isDone }

        if (!alreadyUnlocked) gain(event.player, Bounty.ADVANCEMENT)
    }

    @SubscribeEvent
    fun onAdvancement(event: BlockEvent.BreakEvent) {
        if (event.state.`is`(Blocks.IRON_ORE)) gain(event.player, Bounty.MINED_IRON)
        if (event.state.`is`(Blocks.DIAMOND_ORE)) gain(event.player, Bounty.MINED_DIAMOND)
    }

}