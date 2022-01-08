package possible_triangle.divide.crates.callbacks

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot
import kotlin.math.min
import kotlin.random.Random

class FillLootCallback(val pos: BlockPos, val table: CrateLoot, val orders: List<ItemStack>) :
    TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val loot = table.generate() + orders
        val crate = CrateScheduler.crateAt(server, pos) ?: return

        val grouped = loot.fold(hashMapOf<ItemStack, Int>()) { map, stack ->
            val match = map.keys.find { stack.sameItem(it) }
            if (match != null) map[match] = stack.count + match.count
            else map[stack] = stack.count
            map
        }

        val split = grouped.map { (stack, total) ->
            var remaining = total
            val counts = mutableListOf<Int>()
            while(remaining > 0) {
                val max = min(total, stack.maxStackSize)
                val count = if(max == 1) 1
                else Random.nextInt(1, max)
                remaining -= count
                counts.add(count)
            }
            counts.map {
                val clone = stack.copy()
                clone.count = it
                clone
            }
        }.flatten()

        CrateScheduler.setLock(crate, null)
        val slots = (0 until crate.containerSize).toList().shuffled()
        if(split.size > crate.containerSize)  DivideMod.LOGGER.warn("too much loot to fit into barrel")

        slots.forEachIndexed { i, slot ->
            crate.setItem(slot, split.getOrElse(i) { ItemStack.EMPTY })
        }

        server.playerList.players.forEach {
            server.overworld()
                .sendParticles(
                    it, ParticleTypes.FIREWORK, false,
                    pos.x + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5,
                    20, 0.5, 0.5, 0.5, 0.1
                )
        }
    }

    object Serializer :
        TimerCallback.Serializer<MinecraftServer, FillLootCallback>(
            ResourceLocation(DivideMod.ID, "crates"),
            FillLootCallback::class.java
        ) {

        override fun serialize(nbt: CompoundTag, callback: FillLootCallback) {
            with(callback) {
                nbt.put("pos", NbtUtils.writeBlockPos(pos))
                nbt.putString("table", CrateLoot.idOf(table))

                val list = ListTag()
                orders.forEach {
                    val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it)
                    encoded.get().ifLeft(list::add)
                }
                nbt.put("orders", list)
            }
        }

        override fun deserialize(nbt: CompoundTag): FillLootCallback {
            val pos = NbtUtils.readBlockPos(nbt.getCompound("pos"))
            val list = nbt.getList("orders", 10)
            val orders = list
                .map { ItemStack.CODEC.parse(NbtOps.INSTANCE, it) }
                .map { it.get().left() }
                .filter { it.isPresent }
                .map { it.get() }

            val tableName = nbt.getString("table")
            val table = CrateLoot[tableName] ?: throw NullPointerException("missing crate loot table $tableName")

            return FillLootCallback(pos, table, orders)
        }
    }

}