package possible_triangle.divide.logic

import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.actions.Buff
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.command.SellCommand
import possible_triangle.divide.data.PlayerData
import possible_triangle.divide.events.PlayerBountyEvent
import possible_triangle.divide.reward.Reward
import java.util.*
import kotlin.math.min
import kotlin.random.Random

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object DeathEvents {

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

    @SubscribeEvent
    fun playerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val server = event.player.server ?: return
        event.player.awardRecipes(server.recipeManager.recipes)
    }

    fun getDeathPos(player: ServerPlayer): BlockPos? {
        val persistent = PlayerData.persistentData(player)
        val i = persistent.getIntArray(DEATH_POS_TAG)
        return if (i.size == 3)
            BlockPos(i[0], i[1], i[2])
        else null
    }

    fun timeSinceDeath(player: ServerPlayer): Long {
        val persistent = PlayerData.persistentData(player)

        val lastDeath = persistent.getLong(DEATH_TIME_TAG)
        return player.level.gameTime - lastDeath
    }

    fun starterGear(player: ServerPlayer, updateDeathTime: Boolean = true): List<ItemStack> {
        val timeSince = timeSinceDeath(player)
        val persistent = PlayerData.persistentData(player)

        if (updateDeathTime) persistent.putLong(DEATH_TIME_TAG, player.level.gameTime)
        if (timeSince < (Config.CONFIG.starterGearBreak * 20)) return listOf()

        return listOf(
            ItemStack(Items.JUNGLE_PLANKS, 10),
            ItemStack(Items.BREAD, 6),
            ItemStack(bed(player.team?.color)),
        ).map {
            it.orCreateTag.putBoolean("starter_gear", true)
            it
        }
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

    fun degrade(stack: ItemStack): ItemStack {
        val item = stack.item
        if (Random.nextBoolean()) return stack
        val tier = TIERED.find { it.indexOf(item) > 0 }
        return if (tier != null) {
            val lower = tier.take(tier.indexOf(item)).last()
            val downgraded = ItemStack(lower, stack.count)
            downgraded.tag = stack.tag
            return downgraded
        } else
            stack
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val stacks = STORED[event.player.uuid]
        stacks?.map(::degrade)
            ?.filterNot { event.player.addItem(it) }
            ?.forEach { event.player.spawnAtLocation(it, 0F) }
    }

    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        with(SellCommand.HEARTS_UUID) {
            val attribute = event.original.getAttribute(Attributes.MAX_HEALTH) ?: return
            val modifier = attribute.getModifier(this) ?: return
            event.player.getAttribute(Attributes.MAX_HEALTH)?.addPermanentModifier(modifier)
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPlayerDrops(event: LivingDropsEvent) {
        val player = event.entity
        if (player !is ServerPlayer) return
        if (!Teams.isPlayer(player)) return
        if (player.server.gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return

        val killerEntity = event.source.entity
        val killer = if (killerEntity is ServerPlayer && killerEntity.team != player.team)
            killerEntity
        else
            null

        val wasBounty = PlayerBountyEvent.checkBounty(player, killer)
        if (killer != null && !wasBounty) {
            val livedFor = timeSinceDeath(player)
            val modifier = min(5.0, livedFor / 20.0 / 60 / 10) + 1.0
            DivideMod.LOGGER.info("$livedFor $modifier")
            val bounty = if (event.source.isExplosion)
                Bounty.BLOWN_UP
            else
                Bounty.PLAYER_KILL
            bounty.gain(killer, modifier)
        }

        var keepPercent = Random.nextDouble(0.2, 0.8)
        if (killer != null && Buff.isBuffed(killer, Reward.BUFF_LOOT)) keepPercent += 0.2

        event.drops.filter { it.item.tag?.getBoolean("starter_gear") ?: false }
            .forEach { it.setRemoved(Entity.RemovalReason.DISCARDED) }

        val drops = event.drops.filterNot { it.isRemoved || it.item.tag?.getBoolean("starter_gear") ?: false }
        val keepAmount = (drops.size * keepPercent).toInt()
        val keep = drops.filterNot { it.isRemoved }.shuffled().take(keepAmount)

        STORED[player.uuid] = starterGear(player) + keep.map {
            val taken = if (it.item.count > 1) Random.nextInt(it.item.count / 2, it.item.count + 1) else it.item.count
            if (taken >= it.item.count) {
                it.makeFakeItem()
                it.setRemoved(Entity.RemovalReason.DISCARDED)
                it.item
            } else {
                it.item.split(taken)
            }
        }

        val pos = listOf(player.blockPosition().x, player.blockPosition().y, player.blockPosition().z)
        PlayerData.persistentData(player).putIntArray(DEATH_POS_TAG, pos)

    }

}