package possible_triangle.divide.reward

import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.command.arguments.DividePlayerArgument
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logic.Teams
import java.util.*

class ActionTarget<Target> private constructor(
    val id: String,
    val argument: () -> RequiredArgumentBuilder<CommandSourceStack, *>?,
    val fromContext: (CommandContext<CommandSourceStack>) -> Target,
    val fromString: (String) -> Target,
    val deserialize: (CompoundTag) -> Target?,
    val serialize: (CompoundTag, Target) -> Unit,
    val toEvent: (Target, MinecraftServer) -> EventTarget?,
    val team: (MinecraftServer, Target) -> PlayerTeam? = { _, _ -> null },
    val players: (MinecraftServer, Target) -> List<ServerPlayer> = { s, t ->
        team(s, t)?.let { Teams.players(s, it) } ?: emptyList()
    },
) {

    object Serializer : KSerializer<String> {

        override val descriptor = PrimitiveSerialDescriptor("ActionTarget", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): String {
            val id = decoder.decodeString()
            return id.takeIf { get(id) != null }
                ?: throw SerializationException("Unknown ActionTarget $id")
        }

        override fun serialize(encoder: Encoder, value: String) {
            encoder.encodeString(value)
        }
    }

    init {
        REGISTRY[id] = this
    }

    companion object {

        private val REGISTRY = hashMapOf<String, ActionTarget<*>>()

        val values
            get() = REGISTRY.values.toList()

        operator fun get(id: String): ActionTarget<*>? {
            return REGISTRY[id]
        }

        val NONE = ActionTarget(
            id = "none",
            argument = { null },
            fromContext = { },
            deserialize = { },
            serialize = { _, _ -> },
            toEvent = { _, _ -> null },
            fromString = {},
        )

        val PLAYER = ActionTarget(
            id = "player",
            argument = {
                Commands.argument("target", EntityArgument.player())
                    .suggests(DividePlayerArgument.suggestions(otherTeam = true))
            },
            fromContext = { DividePlayerArgument.getPlayer(it, "target", otherTeam = true).uuid },
            deserialize = { it.getUUID("player") },
            serialize = { nbt, uuid -> nbt.putUUID("player", uuid) },
            toEvent = { it, server -> server.playerList.getPlayer(it)?.let { EventTarget.of(it) } },
            fromString = { UUID.fromString(it) },
            players = { server, uuid -> listOfNotNull(server.playerList.getPlayer(uuid)) },
        )

        val TEAM = ActionTarget(
            id = "team",
            argument = {
                Commands.argument("team", TeamArgument.team()).suggests(DivideTeamArgument.suggestions(true))
            },
            fromContext = { DivideTeamArgument.getTeam(it, "team", otherTeam = true).name },
            deserialize = { it.getString("team") },
            serialize = { nbt, name -> nbt.putString("team", name) },
            toEvent = { it, server -> server.scoreboard.getPlayerTeam(it)?.let { EventTarget.of(it) } },
            fromString = { it },
            team = { server, name -> server.scoreboard.getPlayerTeam(name)?.takeIf { Teams.isPlayingTeam(it) } },
        )

        fun <T> serialize(ctx: RewardContext<T>, nbt: CompoundTag) {
            nbt.put("target", CompoundTag().apply {
                ctx.targetType.serialize(this, ctx.target)
                putString("type", ctx.targetType.id)
            })
        }

    }

}