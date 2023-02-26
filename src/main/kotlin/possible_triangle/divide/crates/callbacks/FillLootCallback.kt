package possible_triangle.divide.crates.callbacks

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import net.minecraft.world.phys.Vec3
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.EventPos
import possible_triangle.divide.events.CallbackHandler
import possible_triangle.divide.extensions.mainWorld
import possible_triangle.divide.extensions.players
import possible_triangle.divide.extensions.putBlockPos
import possible_triangle.divide.extensions.readBlockPos
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Chat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class FillLootCallback(val pos: BlockPos, val table: CrateLoot, val orders: List<ItemStack>, val uuid: UUID) :
    TimerCallback<MinecraftServer> {

    companion object : CallbackHandler<FillLootCallback>("crates", FillLootCallback::class.java) {
        @Serializable
        private data class Event(val pos: EventPos, val table: String, val orders: Int = 0)

        private val LOGGER = EventLogger("loot_crate_filled", { Event.serializer() }) { always() }

        override fun serialize(nbt: CompoundTag, callback: FillLootCallback) {
            with(callback) {
                nbt.putBlockPos("pos", pos)
                nbt.putString("table", table.id)

                val list = ListTag()
                orders.forEach {
                    val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it)
                    encoded.get().ifLeft(list::add)
                }
                nbt.put("orders", list)
                nbt.putUUID("uuid", uuid)
            }
        }

        override fun deserialize(nbt: CompoundTag): FillLootCallback {
            val pos = nbt.readBlockPos("pos")
            val list = nbt.getList("orders", 10)
            val orders = list
                .map { ItemStack.CODEC.parse(NbtOps.INSTANCE, it) }
                .map { it.get().left() }
                .filter { it.isPresent }
                .map { it.get() }

            val tableName = nbt.getString("table")
            val table = CrateLoot.getOrThrow(tableName)
            val uuid = nbt.getUUID("uuid")

            return FillLootCallback(pos, table, orders, uuid)
        }
    }

    override fun handle(server: MinecraftServer, events: TimerQueue<MinecraftServer>, time: Long) {
        val loot = orders + table.generate(Random(server.mainWorld().random.nextLong()))
        val crate = CrateScheduler.crateAt(server, pos, uuid = uuid) ?: return

        val shuffled = if (Config.CONFIG.crate.splitAndShuffle) {
            val grouped = loot.fold(hashMapOf<ItemStack, Int>()) { map, stack ->
                val match = map.keys.find { ItemStack.matches(stack, it) }
                if (match != null) map[match] = map[match]!! + stack.count
                else map[stack] = stack.count
                map
            }

            grouped.map { (stack, total) ->
                var remaining = total
                val counts = mutableListOf<Int>()
                while (remaining > 0) {
                    val max = min(total, stack.maxStackSize)
                    val count = if (max == 1) 1
                    else Random.nextInt(max(1, max / 3), max)
                    remaining -= count
                    counts.add(count)
                }
                counts.map {
                    val clone = stack.copy()
                    clone.count = it
                    clone
                }
            }.flatten()
        } else loot

        CrateScheduler.setLock(crate, null)
        val slots = (0 until crate.containerSize).toList()
        val shuffledSlots = if (Config.CONFIG.crate.splitAndShuffle) slots.shuffled() else slots
        if (shuffled.size > crate.containerSize) DivideMod.LOGGER.warn("too much loot to fit into barrel")

        shuffledSlots.forEachIndexed { i, slot ->
            crate.setItem(slot, shuffled.getOrElse(i) { ItemStack.EMPTY })
        }

        LOGGER.log(server, Event(EventPos.of(pos), table.id, orders.size))

        val vec = Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        server.players().forEach {

            Chat.sound(it, ResourceLocation("entity.experience_orb.pickup"), vec, pitch = 0.1F)
            server.mainWorld().sendParticles(
                it, ParticleTypes.FIREWORK, false,
                vec.x, vec.y, vec.z,
                20, 0.5, 0.5, 0.5, 0.1
            )
        }
    }

}