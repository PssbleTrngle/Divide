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
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Team
import net.minecraftforge.event.TickEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.hacks.PacketIntercepting
import java.util.stream.Collectors

@Mod.EventBusSubscriber
object Util {

    fun persistentData(player: Player): CompoundTag {
        if (!player.persistentData.contains(Player.PERSISTED_NBT_TAG)) player.persistentData.put(
            Player.PERSISTED_NBT_TAG,
            CompoundTag()
        )
        return player.persistentData.getCompound(Player.PERSISTED_NBT_TAG)
    }

    fun blocksIn(aabb: AABB): List<BlockPos> {
        return BlockPos.betweenClosedStream(aabb)
            .map { BlockPos(it) }
            .collect(Collectors.toList())
    }

    fun encodePos(pos: BlockPos, player: ServerPlayer?): MutableComponent {
        return ComponentUtils.wrapInSquareBrackets(
            TranslatableComponent("chat.coordinates", pos.x, pos.y, pos.z)
        ).withStyle(ChatFormatting.GOLD).withStyle {
            if (player?.hasPermissions(2) == true)
                it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent("teleport to position")))
                    .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp ${pos.x} ${pos.y} ${pos.z}"))
            else it
        }
    }

    fun withoutCollision(entity: Entity, server: MinecraftServer, team: Team? = null) {
        var teamName = "${DivideMod.ID}_nocollision"
        val color = team?.color.takeIf { it != ChatFormatting.RESET }
        if (color != null) teamName += "_${color.name.lowercase()}"

        val markerTeam = server.scoreboard.getPlayerTeam(teamName) ?: server.scoreboard.addPlayerTeam(teamName)
        markerTeam.collisionRule = Team.CollisionRule.NEVER
        if (color != null) markerTeam.color = color
        server.scoreboard.addPlayerToTeam(entity.scoreboardName, markerTeam)
    }

    fun <T : Entity> spawnMarker(
        type: EntityType<T>,
        level: ServerLevel,
        pos: BlockPos,
        additionalData: (CompoundTag) -> Unit = {}
    ): T {
        return spawnMarker(type, level, Vec3(pos.x + 0.5, pos.y + 0.25, pos.z + 0.5), additionalData)
    }

    fun <T : Entity> spawnMarker(
        type: EntityType<T>,
        level: ServerLevel,
        pos: Vec3,
        additionalData: (CompoundTag) -> Unit = {}
    ): T {
        val marker = type.create(level) ?: throw NullPointerException()
        val nbt = CompoundTag()
        nbt.putBoolean("NoAI", true)
        nbt.putBoolean("NoGravity", true)
        nbt.putBoolean("Invulnerable", true)
        nbt.putBoolean("PersistenceRequired", true)

        additionalData(nbt)

        marker.deserializeNBT(nbt)
        marker.moveTo(pos)
        level.addFreshEntity(marker)

        if (marker is LivingEntity) {
            marker.tags.add("invisible")
            PacketIntercepting.updateData(marker, level.server)
        }

        withoutCollision(marker, level.server)

        return marker
    }

    fun <T : TickEvent> shouldSkip(
        event: T,
        getLevel: (T) -> Level,
        ticks: Int = 20,
        onlyOverworld: Boolean = true
    ): Boolean {
        val level = getLevel(event)
        return level !is ServerLevel
                || event.phase != TickEvent.Phase.START
                || (ticks > 1 && level.gameTime % ticks != 0L)
                || (onlyOverworld && level.server.overworld() != level)
    }

}