package possible_triangle.divide.reward

import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.command.arguments.DividePlayerArgument
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.data.EventPlayer
import java.util.*

class ActionTarget<Raw, Target> private constructor(
    val id: String,
    val argument: () -> RequiredArgumentBuilder<CommandSourceStack, *>?,
    val fromContext: (CommandContext<CommandSourceStack>) -> Raw,
    val fromString: (String) -> Raw,
    val deserialize: (CompoundTag) -> Raw?,
    val serialize: (CompoundTag, Raw) -> Unit,
    val fetch: (Raw, MinecraftServer) -> Target?,
    val toEvent: (Target) -> EventPlayer?,
) {

    companion object {

        val NONE = ActionTarget(
            id = "none",
            argument = { null },
            fromContext = { },
            deserialize = { },
            serialize = { _, _ -> },
            fetch = { _, _ -> },
            toEvent = { null },
            fromString = {},
        )

        val PLAYER = ActionTarget(
            id = "player",
            argument = {
                Commands.argument("target", EntityArgument.player())
                    .suggests(DividePlayerArgument.suggestions(otherTeam = true))
            },
            fromContext = { DividePlayerArgument.getPlayer(it, "target").uuid },
            deserialize = { it.getUUID("player") },
            serialize = { nbt, uuid -> nbt.putUUID("player", uuid) },
            fetch = { uuid, server -> server.playerList.getPlayer(uuid) },
            toEvent = { EventPlayer.of(it) },
            fromString = { UUID.fromString(it) },
        )

        val TEAM = ActionTarget(
            id = "team",
            argument = {
                Commands.argument("team", TeamArgument.team()).suggests(DivideTeamArgument.suggestions(true))
            },
            fromContext = { DivideTeamArgument.getTeam(it, "team", otherTeam = true).name },
            deserialize = { it.getString("team") },
            serialize = { nbt, name -> nbt.putString("team", name) },
            fetch = { name, server -> server.scoreboard.getPlayerTeam(name) },
            toEvent = { EventPlayer.of(it) },
            fromString = { it }
        )

        fun <R, T> serialize(ctx: RewardContext<R, T>, nbt: CompoundTag) {
            nbt.put("target", CompoundTag().apply {
                ctx.action.target.serialize(this, ctx.rawTarget)
                putString("type", ctx.action.target.id)
            })
        }

        fun <R, T> deserialize(action: Action<R, T>, nbt: CompoundTag): R? {
            return nbt.getCompound("target").let {
                val type = it.getString("type")
                if (type != action.target.id) null
                else action.target.deserialize(it)
            }
        }

    }

}