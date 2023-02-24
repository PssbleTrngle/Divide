package possible_triangle.divide.data

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.scoreboard.AbstractTeam.CollisionRule
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import possible_triangle.divide.DivideMod
import possible_triangle.divide.hacks.PacketIntercepting
import java.util.stream.Collectors

object Util {

    fun PlayerEntity.persistentData(): NbtCompound {
        return extraCustomData
    }

    fun blocksIn(aabb: Box): List<BlockPos> {
        return BlockPos.stream(aabb)
            .map { BlockPos(it) }
            .collect(Collectors.toList())
    }

    fun encodePos(pos: BlockPos, player: ServerPlayerEntity?): MutableText? {
        return Texts.bracketed(
            Text.translatable("chat.coordinates", pos.x, pos.y, pos.z)
        ).styled { it.withColor(Formatting.GOLD) }.styled {
            if (player?.hasPermissionLevel(2) == true)
                it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("teleport to position")))
                    .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp ${pos.x} ${pos.y} ${pos.z}"))
            else it
        }
    }

    fun withoutCollision(entity: Entity, server: MinecraftServer, team: Team? = null) {
        var teamName = "${DivideMod.ID}_nocollision"
        val color = team?.color.takeIf { it != Formatting.RESET }
        if (color != null) teamName += "_${color.name.lowercase()}"

        val markerTeam = server.scoreboard.getPlayerTeam(teamName) ?: server.scoreboard.addTeam(teamName)
        markerTeam.collisionRule = CollisionRule.NEVER
        if (color != null) markerTeam.color = color
        server.scoreboard.addPlayerToTeam(entity.entityName, markerTeam)
    }

    fun <T : Entity> spawnMarker(
        type: EntityType<T>,
        level: ServerWorld,
        pos: BlockPos,
        additionalData: (NbtCompound) -> Unit = {},
    ): T {
        return spawnMarker(type, level, Vec3d(pos.x + 0.5, pos.y + 0.25, pos.z + 0.5), additionalData)
    }

    fun <T : Entity> spawnMarker(
        type: EntityType<T>,
        level: ServerWorld,
        pos: Vec3d,
        additionalData: (NbtCompound) -> Unit = {},
    ): T {
        val marker = type.create(level) ?: throw NullPointerException()
        val nbt = NbtCompound()
        nbt.putBoolean("NoAI", true)
        nbt.putBoolean("NoGravity", true)
        nbt.putBoolean("Invulnerable", true)
        nbt.putBoolean("PersistenceRequired", true)

        additionalData(nbt)

        marker.readNbt(nbt)
        marker.setPosition(pos)
        marker.isInvisible = true
        level.spawnEntity(marker)


        if (marker is LivingEntity) {
            marker.scoreboardTags.add("invisible")
            PacketIntercepting.updateData(marker, level.server)
        }

        withoutCollision(marker, level.server)

        return marker
    }

}