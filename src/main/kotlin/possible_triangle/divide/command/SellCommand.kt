package possible_triangle.divide.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Bounty
import possible_triangle.divide.logic.BountyEvents
import possible_triangle.divide.logic.TeamLogic
import java.util.*

@Mod.EventBusSubscriber
object SellCommand {

    val HEARTS_UUID = UUID.fromString("20f16cb6-013c-423f-8464-486f4274bd1d")
    private val SOLD_MAX =
        DynamicCommandExceptionType { TextComponent("you have already sold the maximum amount of $it") }
    private val NO_CHILD =
        SimpleCommandExceptionType(TextComponent("bro you don't have any children"))

    private const val MIN_HEARTS = 6

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("sell").requires { TeamLogic.isPlayer(it.playerOrException) }
                .then(literal("heart").executes(::sellHeart))
                .then(literal("child").executes { throw NO_CHILD.create() })
        )
    }

    private fun sellHeart(ctx: CommandContext<CommandSourceStack>): Int {

        val attribute = ctx.source.playerOrException.getAttribute(Attributes.MAX_HEALTH) ?: throw NullPointerException()

        val current = attribute.getModifier(HEARTS_UUID)?.amount ?: 0.0
        val next = current - 2

        if (next > MIN_HEARTS) throw SOLD_MAX.create("hearts")

        attribute.removeModifier(HEARTS_UUID)
        attribute.addPermanentModifier(
            AttributeModifier(
                HEARTS_UUID,
                "${DivideMod.ID}:hearts_sold",
                next,
                AttributeModifier.Operation.ADDITION
            )
        )

        BountyEvents.gain(ctx.source.playerOrException, Bounty.SOLD_HEART)

        val health = ctx.source.playerOrException.health
        if (health > attribute.value) ctx.source.playerOrException.hurt(
            DamageSource.OUT_OF_WORLD,
            (health - attribute.value).toFloat()
        )

        return next.toInt()
    }

}