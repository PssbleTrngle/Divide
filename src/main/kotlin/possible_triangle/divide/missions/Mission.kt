package possible_triangle.divide.missions

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.server.MinecraftServer
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
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.data.DefaultedResource
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.m
import possible_triangle.divide.missions.Mission.Type.FAIL
import possible_triangle.divide.missions.Mission.Type.SUCCEED

@Serializable
data class Mission(val description: String, val type: Type, val fine: Int, val time: Int) {

    enum class Type { FAIL, SUCCEED }

    @Transient
    lateinit var id: String
        private set

    @Mod.EventBusSubscriber
    companion object : DefaultedResource<Mission>("missions", { Mission.serializer() }) {

        val FIND = listOf<Pair<String, (BlockState) -> Boolean>>(
            "any stone" to { it.`is`(BlockTags.BASE_STONE_NETHER) },
            "any log" to { it.`is`(BlockTags.LOGS) },
            "a crafting table" to { it.`is`(Blocks.CRAFTING_TABLE) },
            "a copper ore" to { it.`is`(BlockTags.COPPER_ORES) },
            "a diamond ore" to { it.`is`(BlockTags.DIAMOND_ORES) },
        ).mapIndexed { i, (key, tag) ->
            tag to defaulted("mine_${key}") { Mission("Mine $key", SUCCEED, 300 - i * 50, (i + 1) * 10) }
        }.associate { it }

        val CRAFT = listOf<Pair<String, (ItemStack) -> Boolean>>(
            "a crafting table" to { it.`is`(Items.CRAFTING_TABLE) },
            "a hoe" to { it.item is HoeItem },
            "a fishing rod" to { it.`is`(Items.FISHING_ROD) },
            "a smoker" to { it.`is`(Items.SMOKER) },
            "a repeater" to { it.`is`(Items.REPEATER) },
            "an observer" to { it.`is`(Items.OBSERVER) },
            "a daylight sensor" to { it.`is`(Items.DAYLIGHT_DETECTOR) },
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
        val DIMENSIONAL_TRAVEL by defaulted("dimensional_travel") { Mission("Don't switch dimensions", FAIL, 300, 15.m) }
        val JUMP by defaulted("jump") { Mission("Don't jump", FAIL, 150, 1.m) }
        val SNEAK by defaulted("sneak") { Mission("Don't sneak", FAIL, 200, 5.m) }

        override fun populate(entry: Mission, server: MinecraftServer?, id: String) {
            entry.id = id
        }

        @SubscribeEvent
        fun onSleep(event: PlayerSleepInBedEvent) {
            SLEEP.fulfill(event.player)
        }

        @SubscribeEvent
        fun onDimensionChange(event: PlayerEvent.PlayerChangedDimensionEvent) {
            DIMENSIONAL_TRAVEL.fulfill(event.player)
        }

        fun onEat(entity: LivingEntity, stack: ItemStack) {
            if (stack.isEdible && entity is ServerPlayer) FOOD.fulfill(entity)
        }

        @SubscribeEvent
        fun onCraft(event: PlayerEvent.ItemCraftedEvent) {
            val stack = event.crafting
            if (stack.isEmpty) return

            CRAFTING.fulfill(event.player)

            CRAFT.filterKeys { it(stack) }
                .map { it.value.getValue(null, null) }
                .forEach { it.fulfill(event.player) }
        }

        @SubscribeEvent
        fun onDeath(event: LivingDeathEvent) {
            val killer = event.source.entity
            val entity = event.entityLiving

            if (entity is ServerPlayer) {
                if (event.source.isExplosion) EXPLODE.fulfill(entity)
                if (event.source.isFire) BURN.fulfill(entity)
                if (event.source == DamageSource.DROWN) DROWN.fulfill(entity)
                if (event.source == DamageSource.FALL) FALL.fulfill(entity)
            }

            if (entity is Enemy && killer is ServerPlayer) {
                SLAY_MONSTER.fulfill(killer)
            }
        }
    }

    fun fulfill(by: Player) {
        if (by !is ServerPlayer) return
        MissionEvent.fulfill(by.server, Teams.teamOf(by) ?: return, this)
    }

}
