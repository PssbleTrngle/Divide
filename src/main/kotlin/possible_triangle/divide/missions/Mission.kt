package possible_triangle.divide.missions

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.HoeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.m
import possible_triangle.divide.missions.Mission.Type.FAIL
import possible_triangle.divide.missions.Mission.Type.SUCCEED

@Serializable
data class Mission(val description: String, val type: Type, val fine: Int, val time: Int) {

    enum class Type { FAIL, SUCCEED }

    @Transient
    lateinit var id: String
        private set

    companion object : DefaultedResource<Mission>("missions", { Mission.serializer() }) {

        val FIND = listOf<Pair<String, (BlockState) -> Boolean>>(
            "any stone" to { it.isIn(BlockTags.BASE_STONE_NETHER) },
            "any log" to { it.isIn(BlockTags.LOGS) },
            "a crafting table" to { it.isOf(Blocks.CRAFTING_TABLE) },
            "a copper ore" to { it.isIn(BlockTags.COPPER_ORES) },
            "a diamond ore" to { it.isIn(BlockTags.DIAMOND_ORES) },
        ).mapIndexed { i, (key, tag) ->
            tag to defaulted("mine_${key}") { Mission("Mine $key", SUCCEED, 300 - i * 50, (i + 1) * 10) }
        }.associate { it }

        val CRAFT = listOf<Pair<String, (ItemStack) -> Boolean>>(
            "a crafting table" to { it.isOf(Items.CRAFTING_TABLE) },
            "a hoe" to { it.item is HoeItem },
            "a fishing rod" to { it.isOf(Items.FISHING_ROD) },
            "a smoker" to { it.isOf(Items.SMOKER) },
            "a repeater" to { it.isOf(Items.REPEATER) },
            "an observer" to { it.isOf(Items.OBSERVER) },
            "a daylight sensor" to { it.isOf(Items.DAYLIGHT_DETECTOR) },
        ).mapIndexed { i, (key, predicate) ->
            predicate to defaulted("craft_${key}") {
                Mission(
                    "Craft $key",
                    SUCCEED,
                    200 - i * 60,
                    15 + i * 10
                )
            }
        }.associate { it }

        val KILL_PLAYER by defaulted("kill_player") { Mission("Kill another player", SUCCEED, 100, 5.m) }
        val SLAY_MONSTER by defaulted("slay_monster") { Mission("Slay a Monster", SUCCEED, 200, 5.m) }
        val SLEEP by defaulted("sleep") { Mission("Go sleepy sleepy", SUCCEED, 100, 30) }

        val DROWN by defaulted("drown") { Mission("Drown", SUCCEED, 100, 1.m) }
        val EXPLODE by defaulted("explode") { Mission("Explode", SUCCEED, 100, 2.m) }
        val FALL by defaulted("fall") { Mission("Fall to your demise", SUCCEED, 200, 30) }
        val BURN by defaulted("burn") { Mission("Burn alive", SUCCEED, 150, 1.m) }

        val FOOD by defaulted("food") { Mission("Don't eat", FAIL, 150, 5.m) }
        val CRAFTING by defaulted("crafting") { Mission("Don't craft", FAIL, 200, 10.m) }
        val DIMENSIONAL_TRAVEL by defaulted("dimensional_travel") {
            Mission(
                "Don't switch dimensions",
                FAIL,
                300,
                15.m
            )
        }
        val JUMP by defaulted("jump") { Mission("Don't jump", FAIL, 150, 1.m) }
        val SNEAK by defaulted("sneak") { Mission("Don't sneak", FAIL, 200, 5.m) }

        override fun populate(entry: Mission, server: MinecraftServer?, id: String) {
            entry.id = id
        }

        init {
            EntitySleepEvents.STOP_SLEEPING.register { entity, _ ->
                if (entity is PlayerEntity) SLEEP.fulfill(entity)
            }
        }

        fun onEat(entity: LivingEntity, stack: ItemStack) {
            if (stack.isFood && entity is ServerPlayerEntity) FOOD.fulfill(entity)
        }

        fun onCrafted(player: PlayerEntity, stack: ItemStack) {
            if (stack.isEmpty) return

            CRAFTING.fulfill(player)

            CRAFT.filterKeys { it(stack) }
                .map { it.value.getValue(null, null) }
                .forEach { it.fulfill(player) }
        }

        init {
            ServerLivingEntityEvents.AFTER_DEATH.register { entity, source ->
                val killer = source.attacker

                if (entity is ServerPlayerEntity) {
                    if (source.isExplosive) EXPLODE.fulfill(entity)
                    if (source.isFire) BURN.fulfill(entity)
                    if (source == DamageSource.DROWN) DROWN.fulfill(entity)
                    if (source == DamageSource.FALL) FALL.fulfill(entity)
                }

                if (entity is MobEntity && killer is ServerPlayerEntity) {
                    SLAY_MONSTER.fulfill(killer)
                }

            }
        }
    }

    fun fulfill(by: PlayerEntity) {
        if (by !is ServerPlayerEntity) return
        MissionEvent.fulfill(by.server, by.participantTeam() ?: return, this)
    }

}
