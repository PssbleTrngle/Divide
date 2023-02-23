package possible_triangle.divide.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.bounty.Bounty
import java.util.*

object SellCommand {

    val HEARTS_UUID = UUID.fromString("20f16cb6-013c-423f-8464-486f4274bd1d")
    private val SOLD_MAX =
        DynamicCommandExceptionType { Text.literal("you have already sold the maximum amount of $it") }
    private val NO_CHILD =
        SimpleCommandExceptionType(Text.literal("bro you don't have any children"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("sell").requires { it.isActiveParticipant() }
                .then(literal("heart").executes(::sellHeart))
                .then(literal("child").executes { throw NO_CHILD.create() })
        )
    }

    fun resetHearts(player: ServerPlayerEntity) {
        val attribute = player.getAttributeInstance(GENERIC_MAX_HEALTH)!!
        attribute.removeModifier(HEARTS_UUID)
    }

    private fun sellHeart(ctx: CommandContext<ServerCommandSource>): Int {
        val attribute = ctx.source.playerOrThrow.getAttributeInstance(GENERIC_MAX_HEALTH)!!

        val current = attribute.getModifier(HEARTS_UUID)?.value ?: 0.0
        val next = current - 2

        if (next > Config.CONFIG.minHearts) throw SOLD_MAX.create("hearts")

        attribute.removeModifier(HEARTS_UUID)
        attribute.addPersistentModifier(
            EntityAttributeModifier(
                HEARTS_UUID,
                "${DivideMod.ID}:hearts_sold",
                next,
                EntityAttributeModifier.Operation.ADDITION
            )
        )

        Bounty.SOLD_HEART.gain(ctx.source.playerOrThrow)

        val health = ctx.source.playerOrThrow.health
        if (health > attribute.value) ctx.source.playerOrThrow.damage(
            DamageSource.OUT_OF_WORLD,
            (health - attribute.value).toFloat()
        )

        return next.toInt()
    }

}