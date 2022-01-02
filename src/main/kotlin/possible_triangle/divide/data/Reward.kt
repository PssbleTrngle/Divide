package possible_triangle.divide.data

import kotlinx.serialization.Serializable
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team.Visibility
import possible_triangle.divide.logic.Action
import possible_triangle.divide.logic.CashLogic
import possible_triangle.divide.logic.actions.TrackPlayer

@Serializable
enum class Reward(
    val display: String,
    val price: Int,
    val duraction: Int,
    val action: Action,
    val requiresTarget: Boolean = false
) {

    TRACK_PLAYER("Track Player", 1000, 10, TrackPlayer, requiresTarget = true),

    HIDE_NAMES("Hide Nametags", 500, 10, { (team) ->
        team.nameTagVisibility = Visibility.HIDE_FOR_OTHER_TEAMS
    }),

    PEACE_TIME("Peace Time", 500, 10, {

    });

    data class Context(
        val team: PlayerTeam,
        val world: ServerLevel,
        val player: ServerPlayer,
        val target: ServerPlayer,
        val reward: Reward
    )

    fun buy(ctx: Context): Boolean {
        val canBuy = CashLogic.get(ctx.world, ctx.team) >= price
        if (!canBuy) return false

        Action.run(action, ctx, duraction)
        return CashLogic.modify(ctx.world, ctx.team, -price)
    }


}

