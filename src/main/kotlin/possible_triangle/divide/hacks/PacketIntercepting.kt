package possible_triangle.divide.hacks

import net.minecraft.entity.Entity
import net.minecraft.entity.data.DataTracker
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.extensions.players
import java.util.*

object PacketIntercepting {

    private fun <T> assign(data: DataTracker, it: DataTracker.Entry<T>) {
        data.set(it.data, it.get())
    }

    private fun shouldModify(server: MinecraftServer, target: Entity): Boolean {
        return target.scoreboardTags.contains("invisible") || DataHacker.Data[server].any { it.target == target.uuid }
    }

    private fun List<DataTracker.SerializedEntry<*>>.modifyData(target: Entity, recipient: ServerPlayerEntity) = map {
        it

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

    fun transformPacket(packet: Packet<*>, recipient: ServerPlayerEntity): Packet<*> {
        if (packet !is EntityTrackerUpdateS2CPacket) return packet
        val target = recipient.world.getEntityById(packet.id) ?: return packet
        if (!shouldModify(recipient.server, target)) return packet

        return try {
            val cloned = packet.trackedValues.modifyData(target, recipient)
            EntityTrackerUpdateS2CPacket(packet.id, cloned)
        } catch (e: NullPointerException) {
            packet
        }
    }

    fun updateData(uuid: UUID, server: MinecraftServer) {
        updateData(server.overworld.getEntity(uuid) ?: return, server)
    }

    fun updateData(entity: Entity, server: MinecraftServer) {
        if (!shouldModify(server, entity)) return
        val data = entity.dataTracker.changedEntries ?: return
        server.players().forEach {
            it.networkHandler.sendPacket(EntityTrackerUpdateS2CPacket(entity.id, data), null)
        }
    }

}