package possible_triangle.divide.crates.loot

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import possible_triangle.divide.crates.loot.LootFunction.*
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.makeWeightedDecision
import kotlin.random.Random


@Serializable
data class CrateLoot(val weight: Double, val pools: List<LootPools>) {

    @Transient
    lateinit var id: String
        private set

    companion object : DefaultedResource<CrateLoot>("crate_loot", { CrateLoot.serializer() }) {

        private fun normalized(list: List<LootEntry>): List<LootEntry> {
            if (list.isEmpty()) return emptyList()
            val max = list.maxOf { it.weight }
            return list.map {
                val copied = it.copy(weight = it.weight / max)
                copied.item = it.item
                copied
            }
        }

        private fun normalized(vararg list: LootEntry): List<LootEntry> {
            return normalized(list.toList())
        }

        private fun applyWeight(loot: List<LootEntry>, factor: Double): List<LootEntry> {
            return normalized(loot).map {
                val copied = it.copy(weight = it.weight * factor)
                copied.item = it.item
                copied
            }
        }

        private fun matrix(loot: List<LootEntry>, matrix: Map<List<LootFunction>, Double>): List<LootEntry> {
            return matrix.map { (functions, factor) ->
                applyWeight(loot, factor).filter { entry -> functions.all { it.canApply(entry.createStack()) } }.map {
                    val merged = functions + (it.functions ?: listOf())
                    val copied = it.copy(functions = if (merged.isEmpty()) null else merged.distinct())
                    copied.item = it.item
                    copied
                }
            }.flatten()
        }

        init {
            val resources = normalized(
                LootEntry(Items.JUNGLE_PLANKS, 30.0, listOf(5, 20)),
                LootEntry(Items.RAW_IRON, 30.0, listOf(2, 6)),
                LootEntry(Items.RAW_GOLD, 20.0, listOf(2, 6)),
                LootEntry(Items.RAW_COPPER, 10.0, listOf(2, 6)),
            )

            val redstone = normalized(
                LootEntry(Items.TNT, 100.0, listOf(3, 5)),
                LootEntry(Items.REDSTONE, 30.0, listOf(2, 6)),
                LootEntry(Items.SLIME_BALL, 30.0, listOf(2, 6)),
                LootEntry(Items.GUNPOWDER, 30.0, listOf(2, 6)),
                LootEntry(Items.OBSERVER, 5.0, listOf(1, 2)),
                LootEntry(Items.REPEATER, 5.0, listOf(2, 3)),
                LootEntry(Items.PISTON, 5.0, listOf(2, 3)),
                LootEntry(Items.POINTED_DRIPSTONE, 10.0, listOf(1, 4)),
            )

            val rarities = normalized(
                LootEntry(Items.DIAMOND, 10.0, listOf(1, 3)),
                LootEntry(Items.ENDER_PEARL, 5.0, listOf(1, 2)),
                LootEntry(Items.SCUTE, 5.0, listOf(1, 2)),
                LootEntry(Items.EXPERIENCE_BOTTLE, 10.0, listOf(2, 6)),
                LootEntry(Items.MUSIC_DISC_13),
                LootEntry(Items.MUSIC_DISC_STAL),
                LootEntry(Items.CHORUS_FRUIT, 5.0, listOf(3, 5)),
            )

            val food = normalized(
                LootEntry(Items.BREAD, 20.0, listOf(3, 11)),
                LootEntry(Items.COOKED_CHICKEN, 5.0, listOf(3, 11)),
                LootEntry(Items.COOKED_BEEF, 5.0, listOf(3, 11)),
                LootEntry(Items.COOKED_PORKCHOP, 5.0, listOf(3, 11)),
                LootEntry(Items.CARROT, 10.0, listOf(3, 11)),
                LootEntry(Items.POTATO, 10.0, listOf(3, 11)),
                LootEntry(Items.APPLE, 20.0, listOf(3, 11)),
            )

            val potions = matrix(
                normalized(
                    LootEntry(Items.POTION, 10.0),
                    LootEntry(Items.SPLASH_POTION, 5.0),
                    LootEntry(Items.LINGERING_POTION, 2.0),
                    LootEntry(Items.TIPPED_ARROW),
                ), mapOf(
                    listOf(BREW_BAD) to 1.0,
                    listOf(BREW_GOOD) to 1.0,
                    listOf(BREW_RANDOM) to 1.0,
                )
            )

            val enchanted = mapOf(
                listOf(ENCHANT, VANISH) to 0.5,
                listOf<LootFunction>() to 1.0,
            )

            val tools = normalized(
                listOf(
                    LootEntry(Items.ARROW, 5.0, listOf(3, 7)),
                    LootEntry(Items.POWDER_SNOW_BUCKET, 5.0),
                    LootEntry(Items.PUFFERFISH_BUCKET, 5.0),
                    LootEntry(Items.SADDLE, 5.0),
                    LootEntry(Items.SHIELD, 5.0),
                    LootEntry(Items.TRIDENT, 1.0, functions = listOf(BREAK)),
                ) + matrix(
                    listOf(
                        LootEntry(Items.CROSSBOW, 5.0),
                        LootEntry(Items.BOW, 5.0),
                    ), enchanted
                )
            )

            val ironTools = matrix(
                normalized(
                    LootEntry(Items.IRON_PICKAXE),
                    LootEntry(Items.IRON_SHOVEL),
                    LootEntry(Items.IRON_AXE),
                    LootEntry(Items.IRON_SWORD),
                    LootEntry(Items.CHAINMAIL_BOOTS),
                    LootEntry(Items.CHAINMAIL_CHESTPLATE),
                    LootEntry(Items.CHAINMAIL_LEGGINGS),
                    LootEntry(Items.CHAINMAIL_HELMET),
                ), enchanted
            )

            val stoneTools = matrix(
                normalized(
                    LootEntry(Items.STONE_PICKAXE),
                    LootEntry(Items.STONE_SHOVEL),
                    LootEntry(Items.STONE_AXE),
                    LootEntry(Items.STONE_SWORD),
                    LootEntry(Items.LEATHER_BOOTS),
                    LootEntry(Items.LEATHER_CHESTPLATE),
                    LootEntry(Items.LEATHER_LEGGINGS),
                    LootEntry(Items.LEATHER_HELMET),
                ), enchanted
            )

            val diamondStuff = normalized(
                LootEntry(Items.DIAMOND_PICKAXE),
                LootEntry(Items.DIAMOND_SHOVEL),
                LootEntry(Items.DIAMOND_AXE),
                LootEntry(Items.DIAMOND_SWORD),
                LootEntry(Items.DIAMOND_BOOTS),
                LootEntry(Items.DIAMOND_CHESTPLATE),
                LootEntry(Items.DIAMOND_LEGGINGS),
                LootEntry(Items.DIAMOND_HELMET),
            )

            defaulted("common") {
                CrateLoot(
                    10.0, listOf(
                        LootPools(rolls = 3, resources),
                        LootPools(rolls = 1, redstone),
                        LootPools(rolls = 1, rarities),
                        LootPools(rolls = 3, food),
                        LootPools(rolls = 1, potions),
                        LootPools(
                            rolls = 2,
                            ironTools + tools + applyWeight(stoneTools, 2.0),
                            functions = listOf(DAMAGE)
                        ),
                    )
                )
            }

            defaulted("rare") {
                CrateLoot(
                    3.0, listOf(
                        LootPools(rolls = 3, resources),
                        LootPools(rolls = 2, rarities),
                        LootPools(rolls = 2, food),
                        LootPools(rolls = 2, potions),
                        LootPools(
                            rolls = 2,
                            matrix(diamondStuff, enchanted) + applyWeight(ironTools + tools, 2.0) + LootEntry(
                                Items.ELYTRA,
                                20.0,
                                functions = listOf(BREAK, VANISH)
                            ), functions = listOf(DAMAGE)
                        ),
                    )
                )
            }

            defaulted("legendary") {
                CrateLoot(
                    1.0, listOf(
                        LootPools(rolls = 3, rarities),
                        LootPools(rolls = 2, food),
                        LootPools(rolls = 3, potions),
                        LootPools(
                            rolls = 3,
                            matrix(diamondStuff, enchanted)+ LootEntry(
                                Items.ELYTRA,
                                20.0,
                                functions = listOf(BREAK, VANISH)
                            ), functions = listOf(DAMAGE)
                        ),
                    )
                )
            }

            defaulted("trash") {
                CrateLoot(
                    1.0, listOf(
                        LootPools(
                            rolls = 1,
                            diamondStuff,
                            functions = listOf(ENCHANT),
                        ),
                        LootPools(
                            rolls = 10,
                            listOf(
                                LootEntry(Items.COBWEB, 10.0),
                                LootEntry(Items.BONE, 10.0, listOf(1, 6)),
                                LootEntry(Items.BONE, 10.0, listOf(1, 4)),
                                LootEntry(Items.SKELETON_SKULL, 10.0),
                                LootEntry(Items.ROTTEN_FLESH, 20.0, listOf(2, 5)),
                                LootEntry(Items.POISONOUS_POTATO, 1.0, listOf(1, 3)),
                                LootEntry(Items.BAT_SPAWN_EGG, 2.0),
                            )
                        )
                    )
                )
            }
        }

        fun random(): CrateLoot? {
            return makeWeightedDecision(values.associateWith { it.weight })
        }

        override fun populate(entry: CrateLoot, server: MinecraftServer?, id: String) {
            entry.id = id
            if (server != null) entry.pools.forEach { it.populate(server) }
        }

    }

    fun generate(random: Random = Random): List<ItemStack> {
        return pools.map { it.generate(random) }.flatten()
    }

}