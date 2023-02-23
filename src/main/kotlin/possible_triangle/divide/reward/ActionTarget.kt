package possible_triangle.divide.reward

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.NbtCompound
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import possible_triangle.divide.command.arguments.DividePlayerArgument
import possible_triangle.divide.command.arguments.DivideTeamArgument
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.logic.Teams.isParticipantTeam
import possible_triangle.divide.logic.Teams.participants
import java.util.*

class ActionTarget<Target> private constructor(
    val id: String,
    val suggestions: () -> SuggestionProvider<ServerCommandSource>?,
    val fromContext: (CommandContext<ServerCommandSource>, String) -> Target,
    val validate: (Target, ServerPlayerEntity) -> Unit,
    val fromString: (String) -> Target,
    val deserialize: (NbtCompound) -> Target?,
    val serialize: (NbtCompound, Target) -> Unit,
    val toEvent: (Target, MinecraftServer) -> EventTarget?,
    val team: (RewardContext<Target>) -> Team? = { _ -> null },
    val players: (RewardContext<Target>) -> List<ServerPlayerEntity> = { ctx ->
        team(ctx)?.participants(ctx.server) ?: emptyList()
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
            suggestions = { null },
            fromContext = { _, _ -> },
            deserialize = { },
            serialize = { _, _ -> },
            toEvent = { _, _ -> null },
            validate = { _, _ -> },
            fromString = {},
            team = { ctx -> ctx.team },
            players = { ctx -> listOfNotNull(ctx.player) },
        )

        val PLAYER = ActionTarget(
            id = "player",
            suggestions = { DividePlayerArgument.suggestions(otherTeam = true) },
            fromContext = { it, name -> DividePlayerArgument.getPlayer(it, name).uuid },
            validate = { uuid, user -> DividePlayerArgument.validate(user.server.playerManager.getPlayer(uuid), user) },
            deserialize = { it.getUuid("player") },
            serialize = { nbt, uuid -> nbt.putUuid("player", uuid) },
            toEvent = { it, server -> server.playerManager.getPlayer(it)?.let { EventTarget.of(it) } },
            fromString = { UUID.fromString(it) },
            players = { ctx -> listOfNotNull(ctx.server.playerManager.getPlayer(ctx.target)) },
        )

        val TEAM = ActionTarget(
            id = "team",
            suggestions = { DivideTeamArgument.suggestions(true) },
            fromContext = { it, name -> DivideTeamArgument.getTeam(it, name).name },
            deserialize = { it.getString("team") },
            validate = { team, user ->
                DivideTeamArgument.validate(user.scoreboard.getPlayerTeam(team),
                    user,
                    otherTeam = true)
            },
            serialize = { nbt, name -> nbt.putString("team", name) },
            toEvent = { it, server -> server.scoreboard.getPlayerTeam(it)?.let { EventTarget.of(it) } },
            fromString = { it },
            team = { ctx -> ctx.server.scoreboard.getPlayerTeam(ctx.target)?.takeIf { it.isParticipantTeam() } },
        )

        fun <T> serialize(ctx: RewardContext<T>, nbt: NbtCompound) {
            nbt.put("target", NbtCompound().apply {
                ctx.targetType.serialize(this, ctx.target)
                putString("type", ctx.targetType.id)
            })
        }

    }

}