package possible_triangle.divide.logic

import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Chat
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Bounty
import possible_triangle.divide.data.Reward
import possible_triangle.divide.logic.actions.Buff
import java.util.*
import kotlin.random.Random

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object DeathLogic {

    private val STORED = hashMapOf<UUID, List<ItemStack>>()
    private const val STARTER_GEAR_BREAK = 20 * 60 * 5

    @SubscribeEvent
    fun playerJoin(event: EntityJoinWorldEvent) {
        val player = event.entity
        if (player is ServerPlayer) player.awardRecipes(player.server.recipeManager.recipes)
    }

    fun starterGear(player: ServerPlayer, updateDeathTime: Boolean = true): List<ItemStack> {
        if (!player.persistentData.contains(ServerPlayer.PERSISTED_NBT_TAG)) player.persistentData.put(
            ServerPlayer.PERSISTED_NBT_TAG,
            CompoundTag()
        )
        val persistent = player.persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG)
        val lastDeath = persistent.getLong("${DivideMod.ID}_last_death")
        val now = player.level.gameTime
        val timeSince = now - lastDeath

        if (updateDeathTime) persistent.putLong("${DivideMod.ID}_last_death", now)
        if (timeSince < STARTER_GEAR_BREAK) return listOf()

        val teamColor = player.team?.color ?: ChatFormatting.WHITE

        return listOf(
            ItemStack(Items.JUNGLE_PLANKS, 10),
            ItemStack(Items.BREAD, 6),
            ItemStack(Blocks.WHITE_BED),
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
        stacks?.map { degrade(it) }
            ?.filterNot { event.player.addItem(it) }
            ?.forEach { event.player.spawnAtLocation(it, 0F) }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPlayerDrops(event: LivingDropsEvent) {
        val player = event.entity
        if (player !is ServerPlayer) return
        if (!TeamLogic.isPlayer(player)) return

        val killerEntity = event.source.entity
        val killer = if (killerEntity is ServerPlayer && killerEntity.team != player.team)
            killerEntity
        else
            null

        if (killer != null) BountyEvents.gain(killer, Bounty.PLAYER_KILL)

        var keepPercent = Random.nextDouble(0.2, 0.8)
        if (killer != null && Buff.isBuffed(killer, Reward.BUFF_LOOT)) keepPercent += 0.2

        event.drops.filter { it.item.orCreateTag.getBoolean("starter_gear") }
            .forEach { it.setRemoved(Entity.RemovalReason.DISCARDED) }

        val drops = event.drops.filterNot { it.isRemoved || it.item.orCreateTag.getBoolean("starter_gear") }
        val keepAmount = (drops.size * keepPercent).toInt()
        val keep = drops.filterNot { it.isRemoved }.shuffled().take(keepAmount)

        Chat.sendMessage(player, "You keep $keepPercent% of your items ($keepAmount / ${drops.size})")

        STORED[player.uuid] = starterGear(player) + keep.map {
            val taken = if (it.item.count > 1) Random.nextInt(it.item.count / 2, it.item.count) else it.item.count
            if (taken >= it.item.count) {
                it.makeFakeItem()
                it.setRemoved(Entity.RemovalReason.DISCARDED)
                it.item
            } else {
                it.item.split(taken)
            }
        }

    }

}