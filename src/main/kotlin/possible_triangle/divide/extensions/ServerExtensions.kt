package possible_triangle.divide.extensions

import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level

fun MinecraftServer.players(): List<ServerPlayer> = playerList.players

fun MinecraftServer.mainWorld() = overworld()!!

fun MinecraftServer.time() = mainWorld().time()

fun Level.time() = gameTime

fun Level.id() = dimension().location()

fun Level.isAir(pos: BlockPos) = isStateAtPosition(pos) { it.isAir }