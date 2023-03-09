package possible_triangle.divide.hacks

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import possible_triangle.divide.extensions.mainWorld
import possible_triangle.divide.extensions.players
import possible_triangle.divide.mixins.EntityAccessor
import java.util.*
import kotlin.experimental.or

object PacketIntercepting {

    private val SHARED_FLAGS get() = EntityAccessor.getDATA_SHARED_FLAGS_ID()

    private fun shouldModify(server: MinecraftServer, target: Entity): Boolean {
        return target.tags.contains("invisible") || DataHacker.Data[server].any { it.target == target.uuid }
    }

    private fun List<DataValue<*>>.modifyData(target: Entity, recipient: ServerPlayer) =
        map { value ->
            if (value.id == SHARED_FLAGS.id) {

                val flags = DataHacker.Type.values().associateWith {
                    target.tags.contains(it.name.lowercase())
                            || DataHacker.hasReason(it, target.uuid, recipient)
                }.mapKeys { it.key.flag }

                val current = target.entityData.get(EntityAccessor.getDATA_SHARED_FLAGS_ID())
                val byte =
                    flags.filterValues { it }.keys.fold(current) { byte, flag -> byte or ((1 shl flag).toByte()) }
                DataValue.create(SHARED_FLAGS, byte)

            } else {
                value
            }

            /*
            val cloned = DataTracker(target)
            target.dataTracker.changedEntries?.filterNotNull()?.forEach { assign(cloned, it) }

            val flags = DataHacker.Type.values()
                .associateWith {
                    target.tags.contains(it.name.lowercase()) || DataHacker.hasReason(
                        it,
                        target.uuid,
                        recipient
                    )
                }.mapKeys { it.key.flag }

            val current = cloned.get(Entity.DATA_SHARED_FLAGS_ID)
            val byte = flags.filterValues { it }.keys.fold(current) { byte, flag -> byte or ((1 shl flag).toByte()) }

            cloned.set(Entity.DATA_SHARED_FLAGS_ID, byte)
            return cloned
            */
        }

    fun transformPacket(packet: Packet<*>, recipient: ServerPlayer): Packet<*> {
        if (packet !is ClientboundSetEntityDataPacket) return packet
        val target = recipient.level.getEntity(packet.id) ?: return packet
        if (target !is ServerPlayer) return packet
        if (!shouldModify(recipient.server, target)) return packet

        return try {
            val cloned = packet.packedItems.modifyData(target, recipient)
            ClientboundSetEntityDataPacket(packet.id, cloned)
        } catch (e: NullPointerException) {
            packet
        }
    }

    fun updateData(uuid: UUID, server: MinecraftServer) {
        updateData(server.mainWorld().getEntity(uuid) ?: return, server)
    }

    fun updateData(entity: Entity, server: MinecraftServer) {
        //if (!shouldModify(server, entity)) return
        val data = entity.entityData.packDirty() ?: arrayListOf()

        if (data.none { it.id == SHARED_FLAGS.id }) {
            data.add(DataValue.create(SHARED_FLAGS, 0))
        }

        server.players().forEach {
            it.connection.send(ClientboundSetEntityDataPacket(entity.id, data.modifyData(entity, it)), null)
        }
    }

}