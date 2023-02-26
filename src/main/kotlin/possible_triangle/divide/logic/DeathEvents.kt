package possible_triangle.divide.logic

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.command.SellCommand
import possible_triangle.divide.data.EventPos
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.Util.persistentData
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.extensions.*
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Teams.isParticipant
import possible_triangle.divide.missions.Mission
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.actions.BaseBuff
import java.util.*
import kotlin.math.min
import kotlin.random.Random

@Serializable
private data class Event(
    val player: EventTarget,
    val killer: EventTarget? = null,
    val pos: EventPos,
    val source: String,
)

object DeathEvents {

    private val LOGGER = EventLogger("death", { Event.serializer() }) { always() }

    private val STORED = hashMapOf<UUID, List<ItemStack>>()
    private const val DEATH_POS_TAG = "${DivideMod.ID}_death_pos"
    private const val DEATH_TIME_TAG = "${DivideMod.ID}_last_death"

    private fun bed(color: ChatFormatting?): ItemLike {
        return when (color) {
            WHITE -> Blocks.WHITE_BED
            RED -> Blocks.RED_BED
            BLUE -> Blocks.BLUE_BED
            GREEN -> Blocks.GREEN_BED
            YELLOW -> Blocks.YELLOW_BED
            GOLD -> Blocks.ORANGE_BED
            LIGHT_PURPLE -> Blocks.MAGENTA_BED
            BLACK -> Blocks.BLACK_BED
            DARK_BLUE -> Blocks.BLUE_BED
            DARK_GREEN -> Blocks.GREEN_BED
            DARK_AQUA -> Blocks.CYAN_BED
            DARK_RED -> Blocks.RED_BED
            DARK_PURPLE -> Blocks.PURPLE_BED
            GRAY -> Blocks.LIGHT_GRAY_BED
            DARK_GRAY -> Blocks.GRAY_BED
            AQUA -> Blocks.CYAN_BED
            else -> Blocks.WHITE_BED
        }
    }

    fun ServerPlayer.getDeathPos() = persistentData().readBlockPos(DEATH_POS_TAG)

    fun timeSinceDeath(player: ServerPlayer): Long {
        val persistent = player.persistentData()
        if (!persistent.contains(DEATH_TIME_TAG)) return 0L
        val lastDeath = persistent.getLong(DEATH_TIME_TAG)
        return player.level.time() - lastDeath
    }

    fun startedGear(player: ServerPlayer): List<ItemStack> {
        return respawnGear(player, false) + ItemStack(bed(player.team?.color))
    }

    private fun respawnGear(player: ServerPlayer, checkPause: Boolean = true): List<ItemStack> {
        val compass = listOfNotNull(Bases.createCompass(player))

        if (checkPause) {
            val timeSince = timeSinceDeath(player)
            if (timeSince < (Config.CONFIG.deaths.starterGearBreak * 20)) return compass
        }

        player.persistentData().putLong(DEATH_TIME_TAG, player.level.time())

        return listOf(
            ItemStack(Items.JUNGLE_PLANKS, 10),
            ItemStack(Items.BREAD, 6),
        ) + compass
    }

    private val TIERED = listOf(
        listOf<Item>(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE),
        listOf<Item>(Items.WOODEN_AXE, Items.GOLDEN_AXE),

        listOf<Item>(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE),
        listOf<Item>(Items.WOODEN_PICKAXE, Items.GOLDEN_PICKAXE),

        listOf<Item>(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL),
        listOf<Item>(Items.WOODEN_SHOVEL, Items.GOLDEN_SHOVEL),

        listOf<Item>(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE),
        listOf<Item>(Items.WOODEN_HOE, Items.GOLDEN_HOE),

        listOf<Item>(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD),
        listOf<Item>(Items.WOODEN_SWORD, Items.GOLDEN_SWORD),

        listOf<Item>(Items.LEATHER_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET),
        listOf<Item>(Items.LEATHER_HELMET, Items.GOLDEN_HELMET),
        listOf<Item>(Items.IRON_HELMET, Items.CHAINMAIL_HELMET),

        listOf<Item>(
            Items.LEATHER_CHESTPLATE,
            Items.IRON_CHESTPLATE,
            Items.DIAMOND_CHESTPLATE,
            Items.NETHERITE_CHESTPLATE
        ),
        listOf<Item>(Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE),
        listOf<Item>(Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE),

        listOf<Item>(Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS),
        listOf<Item>(Items.LEATHER_LEGGINGS, Items.GOLDEN_LEGGINGS),
        listOf<Item>(Items.IRON_LEGGINGS, Items.CHAINMAIL_LEGGINGS),

        listOf<Item>(
            Items.LEATHER_CHESTPLATE,
            Items.IRON_CHESTPLATE,
            Items.DIAMOND_CHESTPLATE,
            Items.NETHERITE_CHESTPLATE
        ),
        listOf<Item>(Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE),
        listOf<Item>(Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE),

        listOf<Item>(Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS),
        listOf<Item>(Items.LEATHER_BOOTS, Items.GOLDEN_BOOTS),
        listOf<Item>(Items.IRON_BOOTS, Items.CHAINMAIL_BOOTS),
    )

    private fun degrade(stack: ItemStack): ItemStack {
        val item = stack.item
        if (Random.nextDouble() >= Config.CONFIG.deaths.downgradeProbability) return stack
        val tier = TIERED.find { it.indexOf(item) > 0 }
        return if (tier != null) {
            val lower = tier.take(tier.indexOf(item)).last()
            val downgraded = ItemStack(lower, stack.count)
            downgraded.tag = stack.tag
            return downgraded
        } else
            stack
    }

    private fun playerAndKiller(target: LivingEntity, source: DamageSource): Pair<ServerPlayer, ServerPlayer?>? {
        if (target !is ServerPlayer) return null
        if (!target.isParticipant()) return null
        if (target.server.gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return null

        val killer =
            source.entity.takeIf { it is ServerPlayer && !it.isTeammate(target) } as ServerPlayer?
        return target to killer
    }

    fun restoreItems(player: ServerPlayer) {
        val stacks = STORED[player.uuid]
        stacks?.map(::degrade)
            ?.filterNot { player.addItem(it) }
            ?.forEach { player.drop(it, false) }
    }

    fun copyHeartModifier(original: ServerPlayer, cloned: ServerPlayer) {
        cloned.setRespawnPosition(
            original.respawnDimension,
            original.respawnPosition,
            original.respawnAngle,
            original.isRespawnForced,
            false
        )

        val attribute = original.getAttribute(MAX_HEALTH) ?: return
        val modifier = attribute.getModifier(SellCommand.HEARTS_UUID) ?: return
        cloned.getAttribute(MAX_HEALTH)?.addPermanentModifier(modifier)
    }

    fun onDeath(target: LivingEntity, source: DamageSource) {
        val (player, killer) = playerAndKiller(target, source) ?: return

        LOGGER.log(
            player.server,
            Event(
                EventTarget.of(player),
                EventTarget.optional(killer),
                EventPos.of(player.blockPosition()),
                source.msgId
            )
        )

        val wasBounty = PlayerBountyEvent.checkBounty(player, killer)
        if (killer != null && !wasBounty) {
            val livedFor = timeSinceDeath(player)
            val modifier = min(5.0, livedFor / 20.0 / 60 / 10) + 1.0

            Mission.KILL_PLAYER.fulfill(killer)

            val bounty = if (source.isExplosion)
                Bounty.BLOWN_UP
            else
                Bounty.PLAYER_KILL

            bounty.gain(killer, modifier)
        }

        player.persistentData().putBlockPos(DEATH_POS_TAG, player.blockPosition())
    }

    fun modifyPlayerDrops(target: LivingEntity, source: DamageSource): Boolean {
        val (player, killer) = playerAndKiller(target, source) ?: return false

        var keepPercent = Config.CONFIG.deaths.keepPercent.value
        if (killer != null && BaseBuff.isBuffed(killer, Reward.BUFF_LOOT)) keepPercent += 0.2

        val items = player.inventory.items()
            .filterNot { EnchantmentHelper.hasVanishingCurse(it) }
            .filterNot { it.tag?.getBoolean("starter_gear") == true }
            .filterNot { it.tag?.getBoolean(Bases.COMPASS_TAG) == true }

        val keepAmount = (items.size * keepPercent).toInt()
        val keep = items.shuffled().take(keepAmount)

        STORED[player.uuid] = respawnGear(player) + keep.map {
            val taken =
                if (it.count > 1) Random.nextInt(it.count / 2, it.count + 1)
                else it.count
            if (taken >= it.count) {
                it.copy()
            } else {
                it.split(taken)
            }
        }

        items.filterNot { keep.contains(it) }.forEach {
            player.drop(it, true, false)
        }

        return true
    }

}