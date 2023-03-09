package possible_triangle.divide.gui

import eu.pb4.sgui.api.elements.GuiElementInterface
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.extensions.*
import possible_triangle.divide.logic.Chat
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.participants
import possible_triangle.divide.logic.Teams.participingTeams
import possible_triangle.divide.logic.Teams.teamOrThrow
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

class RewardGui(player: ServerPlayer) : ClearableGui(MenuType.GENERIC_9x4, player) {

    companion object {
        private val NOT_ENOUGH_POINTS = Component.literal("Not enough points :/").withStyle(ChatFormatting.RED).noItalic()
    }

    val team get() = player.participantTeam()

    val points get() = team?.let { Points.get(player.server, it) } ?: 0

    fun showRewards() = withCleared {
        title = Component.literal("Trade points ")
            .append(Component.literal("[$points points]").withStyle(ChatFormatting.GOLD))
        Reward.values.filter { Reward.isVisible(it, team, player.server) }.forEachIndexed { i, reward ->
            setSlot(i, RewardElement(reward))
        }
    }

    fun showTeams(reward: Reward) = withCleared {
        title = Component.literal("Select the targeted team")
        player.server.participingTeams().filterNot { it == player.team }.forEachIndexed { i, team ->
            setSlot(i, TeamTargetElement(reward, team))
        }
    }

    fun showPlayers(reward: Reward) = withCleared {
        title = Component.literal("Select the targeted player")
        player.server.participants().filterNot { it.isTeammate(player) }.forEachIndexed { i, player ->
            setSlot(i, PlayerTargetElement(reward, player))
        }
    }

    fun <T> buy(reward: Reward, targetType: ActionTarget<T>, target: T) = runCatching {
        val success = Reward.buy(
            RewardContext(
                player.teamOrThrow(),
                player.server,
                player.uuid,
                target,
                reward,
                targetType,
            )
        )
        if (success) {
            close()
        } else {
            title = NOT_ENOUGH_POINTS
        }
    }

    inner class TeamTargetElement(private val reward: Reward, private val team: PlayerTeam) : GuiElementInterface {

        private val icon = ItemStack(team.color.toIcon()).apply {
            hoverName = team.formattedDisplayName
        }

        override fun getItemStack() = icon

        override fun getGuiCallback(): GuiElementInterface.ClickCallback {
            return GuiElementInterface.ClickCallback { _, _, _, _ ->
                buy(reward, ActionTarget.TEAM, team.name)
            }
        }
    }

    inner class PlayerTargetElement(private val reward: Reward, private val targetPlayer: ServerPlayer) :
        GuiElementInterface {

        private val icon = ItemStack(Items.PLAYER_HEAD).apply {
            orCreateTag.putString("SkullOwner", targetPlayer.scoreboardName)
        }

        override fun getItemStack() = icon

        override fun getGuiCallback(): GuiElementInterface.ClickCallback {
            return GuiElementInterface.ClickCallback { _, _, _, _ ->
                buy(reward, ActionTarget.PLAYER, targetPlayer.uuid)
            }
        }
    }

    inner class RewardElement(private val reward: Reward) : GuiElementInterface {

        private val canBuy get() = points >= reward.price

        private val icon = ItemStack(reward.icon).apply {
            hoverName = Component.literal(reward.display).noItalic()
            setLore(
                listOfNotNull(
                    NOT_ENOUGH_POINTS.takeUnless { canBuy },
                    lore("costs ${reward.price} points"),
                    reward.charge?.let { lore("takes ${it / 2} minutes to charge up") },
                    reward.duration?.let { lore("lasts ${it / 2} minutes") },
                )
            )
        }

        override fun getItemStack() = icon

        override fun getGuiCallback(): GuiElementInterface.ClickCallback {
            return GuiElementInterface.ClickCallback { _, _, _, _ ->
                if (canBuy) when (reward.target) {
                    ActionTarget.TEAM -> showTeams(reward)
                    ActionTarget.PLAYER -> showPlayers(reward)
                    ActionTarget.NONE -> buy(reward, ActionTarget.NONE, Unit)
                    else -> Chat.warn(player.server, "Unknown target type '${reward.target.id}'")
                }
            }
        }
    }

}

fun ServerPlayer.openRewardGui() {
    RewardGui(this).apply {
        showRewards()
        open()
    }
}