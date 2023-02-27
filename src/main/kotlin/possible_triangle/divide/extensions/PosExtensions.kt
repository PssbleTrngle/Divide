package possible_triangle.divide.extensions

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.AABB
import java.util.stream.Collectors

fun AABB.blocksIn(): List<BlockPos> {
    return BlockPos.betweenClosedStream(this)
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