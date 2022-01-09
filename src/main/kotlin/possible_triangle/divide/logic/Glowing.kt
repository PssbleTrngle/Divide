package possible_triangle.divide.logic

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.saveddata.SavedData
import possible_triangle.divide.DivideMod
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

object Glowing {

    data class Reason(val target: UUID, val visibleTo: List<UUID>, val until: Long)

    private fun isGlowingFor(target: UUID, player: ServerPlayer): Boolean {
        val now = player.level.gameTime
        return Data[player.server].reasons.any { reason ->
            reason.until >= now
                    && reason.target == target
                    && reason.visibleTo.any { it == player.uuid }
        }
    }

    fun addReason(target: Entity, players: List<ServerPlayer>, duration: Int) {
        if (players.isEmpty()) return
        val server = players.first().server
        Data[server].add(Reason(target.uuid, players.map { it.uuid }, server.overworld().gameTime + duration * 20))
        updateGlowingData(target, server)
    }

    private fun <T> assign(cloned: SynchedEntityData, it: SynchedEntityData.DataItem<T>) {
        cloned.define(it.accessor, it.value)
    }

    fun transformPacket(packet: Packet<*>, recipient: ServerPlayer): Packet<*> {
        if (packet !is ClientboundSetEntityDataPacket) return packet
        val target = recipient.level.getEntity(packet.id)?.uuid ?: return packet
        if (!Data[recipient.server].reasons.any { it.target == target }) return packet

        val glowing = isGlowingFor(target, recipient)

        val cloned = SynchedEntityData(recipient.getLevel().getEntity(packet.id) ?: throw NullPointerException())
        packet.unpackedData?.filterNotNull()?.forEach { assign(cloned, it) }

        return try {
            val current = cloned.get(Entity.DATA_SHARED_FLAGS_ID)
            cloned.set(
                Entity.DATA_SHARED_FLAGS_ID,
                if (glowing)
                    ((current or 1).toInt() shl 6).toByte()
                else
                    current and (1 shl 6).inv()
            )

            ClientboundSetEntityDataPacket(packet.id, cloned, false)
        } catch (e: NullPointerException) {
            packet
        }
    }

    fun updateGlowingData(entity: Entity, server: MinecraftServer) {
        if (!Data[server].reasons.any { it.target == entity.uuid }) return

        val data = entity.entityData
        val cloned = SynchedEntityData(entity)

        data.all?.filterNotNull()?.forEach { assign(cloned, it) }

        server.playerList.players.forEach {
            val glowing = isGlowingFor(entity.uuid, it)
            val current = cloned.get(Entity.DATA_SHARED_FLAGS_ID)
            cloned.set(
                Entity.DATA_SHARED_FLAGS_ID,
                if (glowing)
                    ((current or 1).toInt() shl 6).toByte()
                else
                    current and (1 shl 6).inv()
            )

            it.connection.send(ClientboundSetEntityDataPacket(entity.id, cloned, true), null)
        }
    }

    private fun load(nbt: CompoundTag, now: Long): Data {
        return Data(nbt.getList("reasons", 10).map { it as CompoundTag }.map { tag ->
            val target = tag.getUUID("target")
            val visibleTo = tag.getList("visibleTo", 10)
                .map { it as CompoundTag }
                .map { it.getUUID("uuid") }
            val until = tag.getLong("until")
            Reason(target, visibleTo, until)
        }.filter { it.until > now }.toMutableList())
    }

    class Data(private val values: MutableList<Reason> = mutableListOf()) : SavedData() {

        val reasons get() = values.toList()

        fun add(reason: Reason) {
            values.add(reason)
            setDirty()
        }

        override fun save(nbt: CompoundTag): CompoundTag {
            val list = ListTag()
            values.forEach { reason ->
                val tag = CompoundTag()
                tag.putUUID("target", reason.target)
                tag.putLong("until", reason.until)
                tag.put("visibleTo", reason.visibleTo.mapTo(ListTag()) {
                    val tag = CompoundTag()
                    tag.putUUID("uuid", it)
                    tag
                })
                list.add(tag)
            }
            nbt.put("reasons", list)
            return nbt
        }

        companion object {
            operator fun get(server: MinecraftServer): Data {
                return server.overworld().dataStorage.computeIfAbsent(
                    { load(it, server.overworld().gameTime) },
                    ::Data,
                    "${DivideMod.ID}_glowing"
                )
            }
        }

    }

}