package possible_triangle.divide.crates.loot

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.makeWeightedDecition


@Serializable
data class CrateLoot(val weight: Int, val pools: List<LootPools>) {

    companion object : DefaultedResource<CrateLoot>("crate_loot", { CrateLoot.serializer() }) {
        init {
            defaulted("common") {
                CrateLoot(
                    10, listOf(
                        LootPools(
                            rolls = 3,
                            listOf(
                                LootEntry(Items.JUNGLE_PLANKS, 30, listOf(5, 20)),
                                LootEntry(Items.RAW_IRON, 30, listOf(2, 6)),
                                LootEntry(Items.RAW_GOLD, 20, listOf(2, 6)),
                                LootEntry(Items.RAW_COPPER, 10, listOf(2, 6)),
                            )
                        ), LootPools(
                            rolls = 1,
                            listOf(
                                LootEntry(Items.REDSTONE, 30, listOf(2, 6)),
                                LootEntry(Items.SLIME_BALL, 30, listOf(2, 6)),
                                LootEntry(Items.GUNPOWDER, 30, listOf(2, 6)),
                                LootEntry(Items.OBSERVER, 5, listOf(1, 2)),
                                LootEntry(Items.REPEATER, 5, listOf(2, 3)),
                                LootEntry(Items.PISTON, 5, listOf(2, 3)),
                                LootEntry(Items.POINTED_DRIPSTONE, 10, listOf(1, 4)),
                            )
                        ), LootPools(
                            rolls = 1,
                            listOf(
                                LootEntry(Items.DIAMOND, 10, listOf(1, 3)),
                                LootEntry(Items.ENDER_PEARL, 5, listOf(1, 2)),
                                LootEntry(Items.SCUTE, 5, listOf(1, 2)),
                                LootEntry(Items.EXPERIENCE_BOTTLE, 10, listOf(2, 6)),
                                LootEntry(Items.MUSIC_DISC_13, 1),
                                LootEntry(Items.MUSIC_DISC_STAL, 1),
                                LootEntry(Items.CHORUS_FRUIT, 5, listOf(3, 5)),
                            )
                        ), LootPools(
                            rolls = 3,
                            listOf(
                                LootEntry(Items.BREAD, 20, listOf(3, 11)),
                                LootEntry(Items.COOKED_CHICKEN, 5, listOf(3, 11)),
                                LootEntry(Items.COOKED_BEEF, 5, listOf(3, 11)),
                                LootEntry(Items.COOKED_PORKCHOP, 5, listOf(3, 11)),
                                LootEntry(Items.CARROT, 10, listOf(3, 11)),
                                LootEntry(Items.POTATO, 10, listOf(3, 11)),
                                LootEntry(Items.APPLE, 20, listOf(3, 11)),
                            )
                        ), LootPools(
                            rolls = 1,
                            listOf(
                                LootEntry(Items.COW_SPAWN_EGG, 5, listOf(1, 2)),
                                LootEntry(Items.PIG_SPAWN_EGG, 5, listOf(1, 2)),
                                LootEntry(Items.SHEEP_SPAWN_EGG, 5, listOf(1, 2)),
                                LootEntry(Items.CHICKEN_SPAWN_EGG, 5, listOf(1, 2)),
                                LootEntry(Items.HORSE_SPAWN_EGG, 5),
                                LootEntry(Items.WOLF_SPAWN_EGG, 5),
                            )
                        ), LootPools(
                            rolls = 1,
                            listOf(
                                LootEntry(Items.POTION, 10, functions = listOf(LootFunction.BREW_RANDOM)),
                                LootEntry(Items.POTION, 10, functions = listOf(LootFunction.BREW_GOOD)),
                                LootEntry(Items.POTION, 10, functions = listOf(LootFunction.BREW_BAD)),
                                LootEntry(Items.SPLASH_POTION, 5, functions = listOf(LootFunction.BREW_RANDOM)),
                                LootEntry(Items.SPLASH_POTION, 5, functions = listOf(LootFunction.BREW_GOOD)),
                                LootEntry(Items.SPLASH_POTION, 5, functions = listOf(LootFunction.BREW_BAD)),
                                LootEntry(Items.LINGERING_POTION, 2, functions = listOf(LootFunction.BREW_RANDOM)),
                                LootEntry(Items.LINGERING_POTION, 2, functions = listOf(LootFunction.BREW_GOOD)),
                                LootEntry(Items.LINGERING_POTION, 2, functions = listOf(LootFunction.BREW_BAD)),
                                LootEntry(Items.TIPPED_ARROW, 1, functions = listOf(LootFunction.BREW_RANDOM)),
                                LootEntry(Items.TIPPED_ARROW, 1, functions = listOf(LootFunction.BREW_GOOD)),
                                LootEntry(Items.TIPPED_ARROW, 1, functions = listOf(LootFunction.BREW_BAD)),
                            )
                        ), LootPools(
                            rolls = 2,
                            listOf(
                                LootEntry(Items.IRON_PICKAXE, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.IRON_SHOVEL, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.IRON_AXE, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.IRON_SWORD, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.CHAINMAIL_BOOTS, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.CHAINMAIL_CHESTPLATE, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.CHAINMAIL_LEGGINGS, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.CHAINMAIL_HELMET, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.CROSSBOW, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.BOW, 5, functions = listOf(LootFunction.ENCHANT)),
                                LootEntry(Items.ARROW, 5, listOf(3, 7)),
                                LootEntry(Items.POWDER_SNOW_BUCKET, 5),
                                LootEntry(Items.PUFFERFISH_BUCKET, 5),
                                LootEntry(Items.SADDLE, 5),
                                LootEntry(Items.SHIELD, 5),
                            )
                        )
                    )
                )
            }

            defaulted("trash") {
                CrateLoot(
                    1, listOf(
                        LootPools(
                            rolls = 10,
                            listOf(
                                LootEntry(Items.COBWEB, 10),
                                LootEntry(Items.BONE, 10, listOf(1, 6)),
                                LootEntry(Items.BONE, 10, listOf(1, 4)),
                                LootEntry(Items.SKELETON_SKULL, 10),
                                LootEntry(Items.ROTTEN_FLESH, 20, listOf(2, 5)),
                                LootEntry(Items.POISONOUS_POTATO, 1, listOf(1, 3)),
                                LootEntry(Items.BAT_SPAWN_EGG, 2),
                            )
                        )
                    )
                )
            }
        }

        fun random(): CrateLoot {
            return makeWeightedDecition(values.associateWith { it.weight })
        }

        override fun populate(entry: CrateLoot, server: MinecraftServer) {
            super.populate(entry, server)
            entry.pools.forEach { it.populate(server) }
        }

    }

    fun generate(): List<ItemStack> {
        return pools.map { it.generate() }.flatten()
    }

}