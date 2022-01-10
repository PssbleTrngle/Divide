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
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.enchantment.EnchantmentHelper
import kotlin.random.Random
import java.util.Random as JavaRandom

enum class LootFunction(val apply: (ItemStack) -> Unit) {

    ENCHANT({
        val level = Random.nextInt(0, 3)
        if (it.isEnchantable && level > 0) {
            EnchantmentHelper.enchantItem(JavaRandom(), it, level, true)
        }
    }),

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