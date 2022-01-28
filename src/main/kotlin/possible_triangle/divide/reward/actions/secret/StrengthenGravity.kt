package possible_triangle.divide.reward.actions.secret

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION
import possible_triangle.divide.DivideMod
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext
import possible_triangle.divide.reward.actions.BaseBuff
import java.util.*

object StrengthenGravity : Action() {

    private val ID = UUID.fromString("8b29dcbc-51a3-45a5-bd1f-6a85409b21b8")

    private fun attribute(server: MinecraftServer): Attribute? {
        return server.registryAccess().registry(Registry.ATTRIBUTE_REGISTRY).map {
            it.get(ResourceLocation("forge", "entity_gravity"))
        }.orElse(null)
    }

    override fun <T> start(ctx: RewardContext<T>) {
        val attribute = attribute(ctx.server) ?: return
        val targets = ctx.targetPlayers()
        if (targets.isEmpty()) return

        val unaffected = ctx.targetPlayers().filter {
            val instance = it.attributes.getInstance(attribute) ?: return@filter true
            if (instance.modifiers.any { it.id == ID }) true
            else {
                instance.addPermanentModifier(AttributeModifier(ID, "${DivideMod.ID}_gravity", 0.02, ADDITION))
                false
            }
        }

        if (unaffected.size == targets.size) throw BaseBuff.ALREADY_BUFFED.create(unaffected.first().scoreboardName)
    }

    override fun <T> stop(ctx: RewardContext<T>) {
        val attribute = attribute(ctx.server) ?: return
        ctx.targetPlayers().forEach {
            it.attributes.getInstance(attribute)?.removeModifier(ID)
        }
    }

}