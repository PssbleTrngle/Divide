package possible_triangle.divide.extensions

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.Score

fun Inventory.items(): List<ItemStack> {
    return listOf(items, armor, offhand).flatten()
}

fun Player.isTeammate(other: Player): Boolean {
    return team == other.team
}

fun ServerPlayer.getScore(objective: Objective): Score {
    return server.scoreboard.getOrCreatePlayerScore(scoreboardName, objective)
}

fun Player.persistentData(): CompoundTag {
    return extraCustomData
}