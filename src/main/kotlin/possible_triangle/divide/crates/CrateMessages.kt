package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerCallbacks
import net.minecraft.world.level.timers.TimerQueue
import net.minecraft.world.phys.AABB
import net.minecraft.world.scores.Team
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.logic.Glowing
import java.util.*
import java.util.stream.Collectors

object CrateMessages {

    init {
        TimerCallbacks.SERVER_CALLBACKS.register(Serializer)
    }


    fun scheduleMessage(server: MinecraftServer, seconds: Int, pos: BlockPos, time: Long, team: Team) {
        server.worldData.overworldData().scheduledEvents.schedule(
            "${DivideMod.ID}:crates",
            server.overworld().gameTime + seconds * 20,
            Callback(team.name, pos, time)
        )
    }

    class Callback(val teamName: String, val pos: BlockPos, val time: Long) : TimerCallback<MinecraftServer> {

        override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {

        }

    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, Callback>(
            ResourceLocation(DivideMod.ID, "crates"),
            Callback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: Callback) {
            nbt.put("pos", NbtUtils.writeBlockPos(callback.pos))
            nbt.putString("team", callback.teamName)
            nbt.putLong("time", callback.time)
        }

        override fun deserialize(nbt: CompoundTag): Callback {
            val pos = NbtUtils.readBlockPos(nbt.getCompound("pos"))
            val teamName = nbt.getString("team")
            val time = nbt.getLong("time")
            return Callback(teamName, pos, time)
        }
    }

}