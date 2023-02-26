package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
import net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.bounty.Bounty
import java.util.*

object SellCommand {

    val HEARTS_UUID = UUID.fromString("20f16cb6-013c-423f-8464-486f4274bd1d")
    private val SOLD_MAX =
        DynamicCommandExceptionType { Component.literal("you have already sold the maximum amount of $it") }
    private val NO_CHILD = SimpleCommandExceptionType(Component.literal("bro you don't have any children"))

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal("sell").requires { it.isActiveParticipant() }
            .then(literal("heart").executes(::sellHeart)).then(literal("child").executes { throw NO_CHILD.create() })
        )
    }

    fun resetHearts(player: ServerPlayer) {
        val attribute = player.getAttribute(MAX_HEALTH)!!
        attribute.removeModifier(HEARTS_UUID)
    }

    private fun sellHeart(ctx: CommandContext<CommandSourceStack>): Int {
        val attribute = ctx.source.playerOrException.getAttribute(MAX_HEALTH)!!

        val current = attribute.getModifier(HEARTS_UUID)?.amount ?: 0.0
        val next = current - 2

        if (next > Config.CONFIG.minHearts) throw SOLD_MAX.create("hearts")

        attribute.removeModifier(HEARTS_UUID)
        attribute.addPermanentModifier(
            AttributeModifier(
                HEARTS_UUID,
                "${DivideMod.ID}:hearts_sold",
                next,
                Operation.ADDITION
            )
        )

        Bounty.SOLD_HEART.gain(ctx.source.playerOrException)

        val health = ctx.source.playerOrException.health
        if (health > attribute.value) ctx.source.playerOrException.hurt(
            DamageSource.OUT_OF_WORLD, (health - attribute.value).toFloat()
        )

        return next.toInt()
    }

}