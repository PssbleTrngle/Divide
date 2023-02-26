package possible_triangle.divide.data

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team.CollisionRule
import possible_triangle.divide.DivideMod
import possible_triangle.divide.hacks.PacketIntercepting
import java.util.stream.Collectors

object Util {

    fun Player.persistentData(): CompoundTag {
        return extraCustomData
    }

    fun blocksIn(aabb: AABB): List<BlockPos> {
        return BlockPos.betweenClosedStream(aabb)
            .map { BlockPos(it) }
            .collect(Collectors.toList())
    }

    fun BlockPos.toComponent(player: ServerPlayer?): MutableComponent {
        return ComponentUtils.wrapInSquareBrackets(
            Component.translatable("chat.coordinates", x, y, z)
        ).withStyle(ChatFormatting.GOLD).withStyle {
            if (player?.hasPermissions(2) == true)
                it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("teleport to position")))
                    .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp $x $y $z"))
            else it
        }
    }

    fun withoutCollision(entity: Entity, server: MinecraftServer, team: PlayerTeam? = null) {
        var teamName = "${DivideMod.ID}_nocollision"
        val color = team?.color.takeIf { it != ChatFormatting.RESET }
        if (color != null) teamName += "_${color.name.lowercase()}"

        val markerTeam = server.scoreboard.getPlayerTeam(teamName) ?: server.scoreboard.addPlayerTeam(teamName)
        markerTeam.collisionRule = CollisionRule.NEVER
        if (color != null) markerTeam.color = color
        server.scoreboard.addPlayerToTeam(entity.scoreboardName, markerTeam)
    }

    fun <T : Entity> spawnMarker(
        type: EntityType<T>,
        level: ServerLevel,
        pos: BlockPos,
        additionalData: (CompoundTag) -> Unit = {},
    ): T {
        return spawnMarker(type, level, Vec3(pos.x + 0.5, pos.y + 0.25, pos.z + 0.5), additionalData)
    }

    fun <T : Entity> spawnMarker(
        type: EntityType<T>,
        level: ServerLevel,
        pos: Vec3,
        additionalData: (CompoundTag) -> Unit = {},
    ): T {
        val marker = type.create(level) ?: throw NullPointerException()
        val nbt = CompoundTag()
        nbt.putBoolean("NoAI", true)
        nbt.putBoolean("NoGravity", true)
        nbt.putBoolean("Invulnerable", true)
        nbt.putBoolean("PersistenceRequired", true)

        additionalData(nbt)

        marker.load(nbt)
        marker.moveTo(pos)
        marker.isInvisible = true
        level.addFreshEntity(marker)


        if (marker is LivingEntity) {
            marker.tags.add("invisible")
            PacketIntercepting.updateData(marker, level.server)
        }

        withoutCollision(marker, level.server)

        return marker
    }

}