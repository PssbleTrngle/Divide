package possible_triangle.divide.logic

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.Team
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object TeamLogic {

    private val NOT_PLAYING = SimpleCommandExceptionType(TextComponent("You are not playing"))

    fun teamOf(player: Player): Team? {
        if (!isPlayer(player)) return null
        return player.team
    }

    fun teamOf(ctx: CommandContext<CommandSourceStack>): Team {
        return teamOf(ctx.source.playerOrException) ?: throw NOT_PLAYING.create()
    }

    fun isSpectator(player: Player): Boolean {
        return player.tags.contains("spectator")
    }

    fun isPlayer(player: Player): Boolean {
        return !isSpectator(player)
    }

    fun spectators(world: ServerLevel): List<ServerPlayer> {
        return world.players().filter { isSpectator(it) }
    }

    fun players(world: ServerLevel): List<ServerPlayer> {
        return world.players().filter { isPlayer(it) }
    }

    fun teams(world: ServerLevel): List<Team> {
        val players = players(world)
        return players.mapNotNull { it.team }.distinctBy { it.name }
    }

    @SubscribeEvent
    fun tick(event: TickEvent.WorldTickEvent) {
        val world = event.world
        if (world.isClientSide || (world !is ServerLevel)) return

        spectators(world).forEach {
            if (it.team != null) world.scoreboard.removePlayerFromTeam(it.scoreboardName)
            it.setGameMode(GameType.SPECTATOR)
        }
    }

}