package possible_triangle.divide.crates.loot

import net.minecraft.ChatFormatting
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style.EMPTY
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.effect.MobEffects.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import kotlin.random.Random
import java.util.Random as JavaRandom

enum class LootFunction(private val consumer: (ItemStack) -> Unit, val canApply: (ItemStack) -> Boolean = { true }) {

    ENCHANT({
        val level = Random.nextInt(1, 3)
        if (level > 0) EnchantmentHelper.enchantItem(JavaRandom(), it, level, true)
    }, { it.isEnchantable }),

    VANISH({ it.enchant(Enchantments.VANISHING_CURSE, 1) }, { it.isEnchantable }),

    BREAK({ it.damageValue = it.maxDamage - 10 }, { it.isDamageableItem && it.damageValue == 0 }),

    DAMAGE(
        { it.damageValue = (it.maxDamage * Random.nextDouble(0.3, 0.7)).toInt() },
        { it.isDamageableItem && it.damageValue == 0 }
    ),

    BREW_GOOD({ stack ->
        val effect = Companion.getEffect { it.isBeneficial }
        PotionUtils.setCustomEffects(stack, listOf(effect))
    }),

    BREW_BAD({ stack ->
        val effect = Companion.getEffect { !it.isBeneficial }
        PotionUtils.setCustomEffects(stack, listOf(effect))
    }),

    BREW_RANDOM({ stack ->
        val effects = listOf(Companion.getEffect { it.isBeneficial }, Companion.getEffect { !it.isBeneficial })
            .onEach { it.curativeItems = listOf() }

        stack.orCreateTag.putInt("HideFlags", 32)
        stack.hoverName = TextComponent("Random Potion").withStyle(EMPTY.withItalic(false))

        val lore = effects.map { Companion.fakePotionLine(it) }
            .map { Component.Serializer.toJson(it) }
            .mapTo(ListTag()) { StringTag.valueOf(it) }

        stack.orCreateTag.getCompound("display").put("Lore", lore)
        PotionUtils.setCustomEffects(stack, effects)
    });

    fun apply(stack: ItemStack) {
        if (canApply(stack)) consumer(stack)
    }

    companion object {
        private val EFFECTS = listOf(
            ABSORPTION,
            BLINDNESS,
            CONFUSION,
            DAMAGE_RESISTANCE,
            DIG_SLOWDOWN,
            DIG_SPEED,
            FIRE_RESISTANCE,
            GLOWING,
            WEAKNESS,
            WATER_BREATHING,
            SLOW_FALLING,
            REGENERATION,
            POISON,
            SATURATION,
            JUMP,
            LEVITATION,
            HUNGER,
            MOVEMENT_SLOWDOWN,
            MOVEMENT_SLOWDOWN,
            NIGHT_VISION
        )

        fun fakePotionLine(effect: MobEffectInstance): Component {
            val text = if (effect.effect.isBeneficial) "Something Good" else "Something Bad"
            val color = if (effect.effect.isBeneficial) ChatFormatting.BLUE else ChatFormatting.RED
            val style = EMPTY.withItalic(false).withColor(color)
            val base = TextComponent(text)

            val withAmplifier = if (effect.amplifier > 0) TranslatableComponent(
                "potion.withAmplifier", base,
                TranslatableComponent("potion.potency." + effect.amplifier)
            ) else base

            return TranslatableComponent(
                "potion.withDuration",
                withAmplifier,
                MobEffectUtil.formatDuration(effect, 1F)
            ).withStyle(style)
        }

        fun getEffect(filter: (MobEffect) -> Boolean): MobEffectInstance {
            val effect = EFFECTS.filter(filter).random()
            return MobEffectInstance(effect, 20 * Random.nextInt(5, 30), Random.nextInt(0, 3))
        }
    }

}