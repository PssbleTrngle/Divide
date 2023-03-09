package possible_triangle.divide.missions

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.data.DurationAsInt
import possible_triangle.divide.extensions.isIn
import possible_triangle.divide.extensions.isOf
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.missions.Mission.Type.FAIL
import possible_triangle.divide.missions.Mission.Type.SUCCEED
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class Mission(val description: String, val type: Type, val fine: Int, val time: DurationAsInt) {

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
            tag to defaulted("mine_${key}") { Mission("Mine $key", SUCCEED, 300 - i * 50, 10.seconds * (i + 1)) }
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
                    15.seconds + 10.seconds * i
                )
            }
        }.associate { it }

        val KILL_PLAYER by defaulted("kill_player") { Mission("Kill another player", SUCCEED, 100, 5.minutes) }
        val SLAY_MONSTER by defaulted("slay_monster") { Mission("Slay a Monster", SUCCEED, 200, 5.minutes) }
        val SLEEP by defaulted("sleep") { Mission("Go sleepy sleepy", SUCCEED, 100, 30.seconds) }

        val DROWN by defaulted("drown") { Mission("Drown", SUCCEED, 100, 1.minutes) }
        val EXPLODE by defaulted("explode") { Mission("Explode", SUCCEED, 100, 2.minutes) }
        val FALL by defaulted("fall") { Mission("Fall to your demise", SUCCEED, 200, 30.seconds) }
        val BURN by defaulted("burn") { Mission("Burn alive", SUCCEED, 150, 1.minutes) }

        val FOOD by defaulted("food") { Mission("Don't eat", FAIL, 150, 5.minutes) }
        val CRAFTING by defaulted("crafting") { Mission("Don't craft", FAIL, 200, 10.minutes) }
        val DIMENSIONAL_TRAVEL by defaulted("dimensional_travel") {
            Mission(
                "Don't switch dimensions",
                FAIL,
                300,
                15.minutes
            )
        }
        val JUMP by defaulted("jump") { Mission("Don't jump", FAIL, 150, 1.minutes) }
        val SNEAK by defaulted("sneak") { Mission("Don't sneak", FAIL, 200, 5.minutes) }

        override fun populate(entry: Mission, id: String) {
            entry.id = id
        }

        fun onSleep(player: ServerPlayer) {
            SLEEP.fulfill(player)
        }

        fun onEat(entity: LivingEntity, stack: ItemStack) {
            if (stack.isEdible && entity is ServerPlayer) FOOD.fulfill(entity)
        }

        fun onCrafted(player: Player, stack: ItemStack) {
            if (stack.isEmpty) return

            CRAFTING.fulfill(player)

            CRAFT.filterKeys { it(stack) }
                .map { it.value.getValue(null, null) }
                .forEach { it.fulfill(player) }
        }

        fun onDeath(entity: LivingEntity, source: DamageSource) {
            val killer = source.entity

            if (entity is ServerPlayer) {
                if (source.isExplosion) EXPLODE.fulfill(entity)
                if (source.isFire) BURN.fulfill(entity)
                if (source == DamageSource.DROWN) DROWN.fulfill(entity)
                if (source == DamageSource.FALL) FALL.fulfill(entity)
            }

            if (entity is Enemy && killer is ServerPlayer) {
                SLAY_MONSTER.fulfill(killer)
            }
        }
    }

    fun fulfill(by: Player) {
        if (by !is ServerPlayer) return
        MissionEvent.fulfill(by.server, by.participantTeam() ?: return, this)
    }

}
