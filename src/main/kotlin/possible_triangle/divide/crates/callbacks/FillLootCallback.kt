package possible_triangle.divide.crates.callbacks

import kotlinx.serialization.Serializable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.timer.Timer
import net.minecraft.world.timer.TimerCallback
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.data.EventPos
import possible_triangle.divide.events.CallbackHandler
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

        override fun serialize(nbt: NbtCompound, callback: FillLootCallback) {
            with(callback) {
                nbt.put("pos", NbtHelper.fromBlockPos(pos))
                nbt.putString("table", table.id)

                val list = NbtList()
                orders.forEach {
                    val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it)
                    encoded.get().ifLeft(list::add)
                }
                nbt.put("orders", list)
                nbt.putUuid("uuid", uuid)
            }
        }

        override fun deserialize(nbt: NbtCompound): FillLootCallback {
            val pos = NbtHelper.toBlockPos(nbt.getCompound("pos"))
            val list = nbt.getList("orders", 10)
            val orders = list
                .map { ItemStack.CODEC.parse(NbtOps.INSTANCE, it) }
                .map { it.get().left() }
                .filter { it.isPresent }
                .map { it.get() }

            val tableName = nbt.getString("table")
            val table = CrateLoot.getOrThrow(tableName)
            val uuid = nbt.getUuid("uuid")

            return FillLootCallback(pos, table, orders, uuid)
        }
    }

    override fun call(server: MinecraftServer, events: Timer<MinecraftServer>, time: Long) {
        val loot = orders + table.generate()
        val crate = CrateScheduler.crateAt(server, pos, uuid = uuid) ?: return

        val shuffled = if (Config.CONFIG.crate.splitAndShuffle) {
            val grouped = loot.fold(hashMapOf<ItemStack, Int>()) { map, stack ->
                val match = map.keys.find { ItemStack.canCombine(stack, it) }
                if (match != null) map[match] = map[match]!! + stack.count
                else map[stack] = stack.count
                map
            }

            grouped.map { (stack, total) ->
                var remaining = total
                val counts = mutableListOf<Int>()
                while (remaining > 0) {
                    val max = min(total, stack.maxCount)
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
        val slots = (0 until crate.size()).toList()
        val shuffledSlots = if (Config.CONFIG.crate.splitAndShuffle) slots.shuffled() else slots
        if (shuffled.size > crate.size()) DivideMod.LOGGER.warn("too much loot to fit into barrel")

        shuffledSlots.forEachIndexed { i, slot ->
            crate.setStack(slot, shuffled.getOrElse(i) { ItemStack.EMPTY })
        }

        LOGGER.log(server, Event(EventPos.of(pos), table.id, orders.size))

        val vec = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        server.playerManager.playerList.forEach {

            Chat.sound(it, Identifier("entity.experience_orb.pickup"), vec, pitch = 0.1F)
            server.overworld.spawnParticles(
                it, ParticleTypes.FIREWORK, false,
                vec.x, vec.y, vec.z,
                20, 0.5, 0.5, 0.5, 0.1
            )
        }
    }

}