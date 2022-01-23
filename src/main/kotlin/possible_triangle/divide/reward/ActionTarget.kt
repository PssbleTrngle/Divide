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

class ActionTarget<Raw, Target> private constructor(
    val id: String,
    val argument: () -> RequiredArgumentBuilder<CommandSourceStack, *>?,
    val fromContext: (CommandContext<CommandSourceStack>) -> Raw,
    val deserialize: (CompoundTag) -> Raw?,
    val serialize: (CompoundTag, Raw) -> Unit,
    val fetch: (Raw, MinecraftServer) -> Target?,
    val toEvent: (Target) -> EventPlayer?,
) {

    companion object {

        val NONE = ActionTarget(
            "none",
            { null },
            { },
            { },
            { _, _ -> },
            { _, _ -> },
            { null }
        )

        val PLAYER = ActionTarget(
            "player",
            {
                Commands.argument("target", EntityArgument.player())
                    .suggests(DividePlayerArgument.suggestions(otherTeam = true))
            },
            { DividePlayerArgument.getPlayer(it, "target").uuid },
            { it.getUUID("player") },
            { nbt, uuid -> nbt.putUUID("player", uuid) },
            { uuid, server -> server.playerList.getPlayer(uuid) },
            { EventPlayer.of(it) },
        )

        val TEAM = ActionTarget(
            "team",
            { Commands.argument("team", TeamArgument.team()).suggests(DivideTeamArgument.suggestions(true)) },
            { DivideTeamArgument.getTeam(it, "team", otherTeam = true).name },
            { it.getString("team") },
            { nbt, name -> nbt.putString("team", name) },
            { name, server -> server.scoreboard.getPlayerTeam(name) },
            { EventPlayer(it.name) },
        )

        fun <R,T> serialize(ctx: RewardContext<R,T>, nbt: CompoundTag) {
            nbt.put("target", CompoundTag().apply {
                ctx.action.target.serialize(this, ctx.rawTarget)
                putString("type", ctx.action.target.id)
            })
        }

        fun <R,T> deserialize(action: Action<R, T>, nbt: CompoundTag): R? {
            return nbt.getCompound("target").let {
                val type = it.getString("type")
                if (type != action.target.id) null
                else action.target.deserialize(it)
            }
        }

    }

}