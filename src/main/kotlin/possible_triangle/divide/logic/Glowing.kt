package possible_triangle.divide.logic

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

object Glowing {

    data class Reason(val target: Int, val visibleTo: List<UUID>, val until: Long)

    private val REASONS = arrayListOf<Reason>()

    private fun reasons(world: ServerLevel): List<Reason> {
        return REASONS.filter { it.until >= world.gameTime }
    }

    fun isGlowingFor(target: Int, player: ServerPlayer): Boolean {
        return reasons(player.getLevel()).any { reason -> reason.target == target && reason.visibleTo.any { it == player.uuid } }
    }

    fun addReason(target: Entity, players: List<ServerPlayer>, duration: Int) {
        if (players.isEmpty()) return
        val server = players.first().server
        REASONS.add(Reason(target.id, players.map { it.uuid }, server.overworld().gameTime + duration * 20))
        updateGlowingData(target, server)
    }

    private fun <T> assign(cloned: SynchedEntityData, it: SynchedEntityData.DataItem<T>) {
        cloned.define(it.accessor, it.value)
    }

    fun transformPacket(packet: Packet<*>, recipient: ServerPlayer): Packet<*> {
        if (packet !is ClientboundSetEntityDataPacket) return packet
        if (!reasons(recipient.getLevel()).any { it.target == packet.id }) return packet

        val cloned = SynchedEntityData(recipient.getLevel().getEntity(packet.id) ?: throw  NullPointerException())
        packet.unpackedData?.filterNotNull()?.forEach { assign(cloned, it) }

        val glowing = isGlowingFor(packet.id, recipient)
        val current = cloned.get(Entity.DATA_SHARED_FLAGS_ID)
        cloned.set(
            Entity.DATA_SHARED_FLAGS_ID,
            if (glowing)
                ((current or 1).toInt() shl 6).toByte()
            else
                current and (1 shl 6).inv()
        )

        return ClientboundSetEntityDataPacket(packet.id, cloned, false)
    }

    fun updateGlowingData(entity: Entity, server: MinecraftServer) {
        if (!reasons(server.overworld()).any { it.target == entity.id }) return

        val data = entity.entityData
        val cloned = SynchedEntityData(entity)

        data.all?.filterNotNull()?.forEach { assign(cloned, it) }

        server.playerList.players.forEach {
            val glowing = isGlowingFor(entity.id, it)
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

}