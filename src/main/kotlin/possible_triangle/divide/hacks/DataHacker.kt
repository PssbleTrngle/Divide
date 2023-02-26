package possible_triangle.divide.hacks

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.extensions.time
import java.util.*

object DataHacker {

    enum class Type(val flag: Int) { GLOWING(6), INVISIBLE(5) }

    data class Reason(
        val type: Type,
        val target: UUID,
        val appliedTo: List<UUID>,
        val until: Long,
        val clearOnDeath: Boolean,
        val clearInBase: Boolean,
        val id: String?,
    )

    fun clearReasons(player: ServerPlayer) {
        Data[player.server].removeIf {
            it.clearOnDeath && it.target == player.uuid
        }
    }

    fun hasReason(type: Type, target: UUID, player: ServerPlayer): Boolean {
        val now = player.server.time()
        return Data[player.server].any { reason ->
            reason.type == type
                    && reason.until >= now
                    && reason.target == target
                    && reason.appliedTo.any { it == player.uuid }
        }
    }

    fun removeReason(server: MinecraftServer, predicate: (Reason) -> Boolean): Boolean {
        return Data.modify(server) {
            val removed = filter(predicate)
            removed.forEach { PacketIntercepting.updateData(it.target, server) }
            removeAll(removed)
            removed.isNotEmpty()
        }
    }

    fun addReason(
        type: Type,
        target: Entity,
        appliedTo: List<ServerPlayer>,
        duration: Int,
        clearOnDeath: Boolean = false,
        clearInBase: Boolean = false,
        id: String? = null,
    ) {
        if (appliedTo.isEmpty()) return
        val server = appliedTo.first().server
        Data.modify(server) {
            add(
                Reason(
                    type,
                    target.uuid,
                    appliedTo.map { it.uuid },
                    server.time() + duration * 20,
                    clearOnDeath,
                    clearInBase,
                    id
                )
            )
        }
        PacketIntercepting.updateData(target, server)
    }

    val Data = object : ModSavedData<MutableList<Reason>>("entity_data_hacks") {
        override fun save(nbt: CompoundTag, value: MutableList<Reason>) {
            val list = ListTag()
            value.forEach { reason ->
                val tag = CompoundTag()
                tag.putUUID("target", reason.target)
                tag.putLong("until", reason.until)
                tag.putString("type", reason.type.name)
                tag.put("appliedTo", reason.appliedTo.mapTo(ListTag()) {
                    CompoundTag().apply {
                        putUUID("uuid", it)
                    }
                })
                list.add(tag)
            }
            nbt.put("reasons", list)
        }

        override fun load(nbt: CompoundTag, server: MinecraftServer): MutableList<Reason> {
            val now = server.time()
            return nbt.getList("reasons", 10).map { it as CompoundTag }.map { tag ->
                val target = tag.getUUID("target")
                val appliedTo = tag.getList("appliedTo", 10)
                    .map { it as CompoundTag }
                    .map { it.getUUID("uuid") }
                val until = tag.getLong("until")
                val id = if (tag.contains("id")) tag.getString("id") else null
                val type = Type.valueOf(tag.getString("type"))
                Reason(
                    type,
                    target,
                    appliedTo,
                    until,
                    tag.getBoolean("clearOnDeath"),
                    tag.getBoolean("clearInBase"),
                    id
                )
            }.filter { it.until > now }.toMutableList()
        }

        override fun default(): MutableList<Reason> {
            return mutableListOf()
        }
    }

}