package possible_triangle.divide.logic

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod

@Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object TeamLogic {

    private val NOT_PLAYING = SimpleCommandExceptionType(TextComponent("You are not playing"))

    fun teamOf(player: Player): PlayerTeam? {
        if (!isPlayer(player)) return null
        val team = player.team
        return if (team is PlayerTeam) team
        else null
    }

    fun teamOf(ctx: CommandContext<CommandSourceStack>): PlayerTeam {
        return teamOf(ctx.source.playerOrException) ?: throw NOT_PLAYING.create()
    }

    fun teammates(player: ServerPlayer, includeSelf: Boolean = true): List<ServerPlayer> {
        val team = teamOf(player) ?: return listOf()
        return player.getLevel().players()
            .filter { it.team?.name == team.name }
            .filter { includeSelf || it.uuid != player.uuid }
    }

    fun isAdmin(source: CommandSourceStack): Boolean {
        val entity = source.entity
        return source.hasPermission(2) || entity is ServerPlayer && entity.tags.contains("admin")
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

    fun teams(world: ServerLevel): List<PlayerTeam> {
        val players = players(world)
        return players.mapNotNull { it.team }.distinctBy { it.name }.filterIsInstance(PlayerTeam::class.java)
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