package possible_triangle.divide.crates.callbacks

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.timers.TimerCallback
import net.minecraft.world.level.timers.TimerQueue
import net.minecraft.world.phys.Vec3
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class FillLootCallback(val pos: BlockPos, val table: CrateLoot, val orders: List<ItemStack>, val uuid: UUID) :
    TimerCallback<MinecraftServer> {

    override fun handle(server: MinecraftServer, queue: TimerQueue<MinecraftServer>, time: Long) {
        val loot = orders + table.generate()
        val crate = CrateScheduler.crateAt(server, pos, uuid = uuid) ?: return

        val shuffled = if (Config.CONFIG.crate.splitAndShuffle) {
            val grouped = loot.fold(hashMapOf<ItemStack, Int>()) { map, stack ->
                val match = map.keys.find { stack.sameItem(it) }
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

        val vec = Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val soundPacket = ClientboundCustomSoundPacket(
            ResourceLocation("entity.experience_orb.pickup"),
            SoundSource.MASTER,
            vec,
            1F,
            0.1F
        )
        server.playerList.players.forEach {

            it.connection.send(soundPacket)

            server.overworld()
                .sendParticles(
                    it, ParticleTypes.FIREWORK, false,
                    vec.x, vec.y, vec.z,
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
                nbt.putUUID("uuid", uuid)
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
            val table = CrateLoot.getOrThrow(tableName)
            val uuid = nbt.getUUID("uuid")

            return FillLootCallback(pos, table, orders, uuid)
        }
    }

}