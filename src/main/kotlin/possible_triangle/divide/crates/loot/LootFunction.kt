package possible_triangle.divide.crates.loot

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffectUtil
import net.minecraft.entity.effect.StatusEffects.*
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.potion.PotionUtil
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.random.Random
import net.minecraft.util.math.random.Random as MCRandom

enum class LootFunction(private val consumer: (ItemStack) -> Unit, val canApply: (ItemStack) -> Boolean = { true }) {

    ENCHANT({
        val level = Random.nextInt(1, 3)
        if (level > 0) EnchantmentHelper.enchant(MCRandom.create(), it, level, true)
    }, { it.isEnchantable }),

    VANISH({ it.addEnchantment(Enchantments.VANISHING_CURSE, 1) }, { it.isEnchantable }),

    BREAK({ it.damage = it.maxDamage - 10 }, { it.isDamageable && it.damage == 0 }),

    DAMAGE(
        { it.damage = (it.maxDamage * Random.nextDouble(0.3, 0.7)).toInt() },
        { it.isDamageable && it.damage == 0 }
    ),

    BREW_GOOD({ stack ->
        val effect = Companion.getEffect { it.isBeneficial }
        PotionUtil.setCustomPotionEffects(stack, listOf(effect))
    }),

    BREW_BAD({ stack ->
        val effect = Companion.getEffect { !it.isBeneficial }
        PotionUtil.setCustomPotionEffects(stack, listOf(effect))
    }),

    BREW_RANDOM({ stack ->
        val effects = listOf(Companion.getEffect { it.isBeneficial }, Companion.getEffect { !it.isBeneficial })
            .onEach { it }

        stack.orCreateNbt.putInt("HideFlags", 32)
        stack.setCustomName(Text.literal("Random Potion").styled { it.withItalic(false) })

        val lore = effects.map { Companion.fakePotionLine(it) }
            .map { Text.Serializer.toJson(it) }
            .mapTo(NbtList()) { NbtString.of(it) }

        stack.orCreateNbt.getCompound("display").put("Lore", lore)
        PotionUtil.setCustomPotionEffects(stack, effects)
    });

    fun apply(stack: ItemStack) {
        if (canApply(stack)) consumer(stack)
    }

    companion object {
        private val EFFECTS = listOf(
            ABSORPTION,
            BLINDNESS,
            NAUSEA,
            RESISTANCE,
            MINING_FATIGUE,
            HASTE,
            FIRE_RESISTANCE,
            GLOWING,
            WEAKNESS,
            WATER_BREATHING,
            SLOW_FALLING,
            REGENERATION,
            POISON,
            SATURATION,
            JUMP_BOOST,
            LEVITATION,
            HUNGER,
            SLOWNESS,
            NIGHT_VISION
        )

        fun fakePotionLine(effect: StatusEffectInstance): Text {
            val text = if (effect.effectType.isBeneficial) "Something Good" else "Something Bad"
            val color = if (effect.effectType.isBeneficial) Formatting.BLUE else Formatting.RED
            val style = Style.EMPTY.withItalic(false).withColor(color)
            val base = Text.literal(text)

            val withAmplifier = if (effect.amplifier > 0) Text.translatable(
                "potion.withAmplifier", base,
                Text.translatable("potion.potency." + effect.amplifier)
            ) else base

            return Text.translatable(
                "potion.withDuration",
                withAmplifier,
                StatusEffectUtil.durationToString(effect, 1F)
            ).setStyle(style)
        }

        fun getEffect(filter: (StatusEffect) -> Boolean): StatusEffectInstance {
            val effect = EFFECTS.filter(filter).random()
            return StatusEffectInstance(effect, 20 * Random.nextInt(5, 30), Random.nextInt(0, 3))
        }
    }

}