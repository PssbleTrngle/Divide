package possible_triangle.divide.hacks

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.*
import kotlin.experimental.or

object PacketIntercepting {

    private fun <T> assign(data: SynchedEntityData, it: SynchedEntityData.DataItem<T>) {
        data.define(it.accessor, it.value)
    }

    private fun shouldModify(server: MinecraftServer, target: Entity): Boolean {
        return target.tags.contains("invisible") || DataHacker.Data[server].any { it.target == target.uuid }
    }

    private fun modifyData(target: Entity, recipient: ServerPlayer): SynchedEntityData {
        val cloned = SynchedEntityData(target)
        target.entityData.all?.filterNotNull()?.forEach { assign(cloned, it) }

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
    }

    fun transformPacket(packet: Packet<*>, recipient: ServerPlayer): Packet<*> {
        if (packet !is ClientboundSetEntityDataPacket) return packet
        val target = recipient.level.getEntity(packet.id) ?: return packet
        if (!shouldModify(recipient.server, target)) return packet

        return try {
            val cloned = modifyData(target, recipient)
            ClientboundSetEntityDataPacket(packet.id, cloned, false)
        } catch (e: NullPointerException) {
            packet
        }
    }

    fun updateData(uuid: UUID, server: MinecraftServer) {
        updateData(server.overworld().getEntity(uuid) ?: return, server)
    }

    fun updateData(entity: Entity, server: MinecraftServer) {
        if (!shouldModify(server, entity)) return
        server.playerList.players.forEach {
            val cloned = modifyData(entity, it)
            it.connection.send(ClientboundSetEntityDataPacket(entity.id, cloned, true), null)
        }
    }

}