package possible_triangle.divide.crates.loot

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionUtils.setPotion
import net.minecraft.world.item.enchantment.EnchantmentHelper
import java.util.*

enum class LootFunction(val apply: (ItemStack) -> Unit) {

    ENCHANT({
        val level =  kotlin.random.Random.nextInt(0, 3)
        if (it.isEnchantable && level > 0) {
            EnchantmentHelper.enchantItem(Random(), it, level, true)
        }
    }),

    BREW({ stack ->
        val beneficial = Companion.EFFECTS.filter { it.isBeneficial }.random()
        val bad = Companion.EFFECTS.filterNot { it.isBeneficial }.random()
        val effects = kotlin.collections.listOf(bad, beneficial).map { MobEffectInstance(it, 20 * 10) }
        val potion = Potion("Test Potion", *effects.toTypedArray())
        setPotion(stack, potion)
    });

    companion object {
        private val EFFECTS = listOf(
            MobEffects.ABSORPTION,
            MobEffects.BLINDNESS,
            MobEffects.CONFUSION,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.DIG_SPEED,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.GLOWING,
            MobEffects.WEAKNESS,
            MobEffects.WATER_BREATHING,
            MobEffects.SLOW_FALLING,
            MobEffects.REGENERATION,
            MobEffects.POISON,
            MobEffects.SATURATION,
            MobEffects.JUMP,
            MobEffects.LEVITATION,
            MobEffects.HUNGER,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.NIGHT_VISION
        )
    }

}