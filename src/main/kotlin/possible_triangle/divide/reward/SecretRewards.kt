package possible_triangle.divide.reward

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.logic.Teams

object SecretRewards {

    fun isVisible(server: MinecraftServer, team: PlayerTeam, reward: Reward): Boolean {
        return DATA[server][team]?.contains(reward.id) == true
    }

    fun choose(server: MinecraftServer) {
        val teams = Teams.teams(server)
        if (teams.isEmpty()) return

        val rewards = Reward.values.filter { it.secret }.shuffled()
        val perTeam = rewards.size / teams.size

        if (perTeam <= 0) DivideMod.LOGGER.warn("Not enough secret rewards for ${teams.size} teams")
        else {
            DATA[server] = teams.mapIndexed { i, team ->
                team to rewards.subList(i * perTeam, (i + 1) * perTeam).map { it.id }
            }.associate { it }
        }
    }

    private val DATA = object : ModSavedData<Map<PlayerTeam, List<String>>>("secret_rewards") {
        override fun save(nbt: CompoundTag, value: Map<PlayerTeam, List<String>>) {
            nbt.put("values", value.mapTo(ListTag()) { (team, rewards) ->
                CompoundTag().apply {
                    putString("team", team.name)
                    put("rewards", rewards.mapTo(ListTag()) { StringTag.valueOf(it) })
                }
            })
        }

        override fun load(nbt: CompoundTag, server: MinecraftServer): Map<PlayerTeam, List<String>> {
            return nbt.getList("values", 10).map { it as CompoundTag }.mapNotNull { tag ->
                val team = server.scoreboard.getPlayerTeam(tag.getString("team")) ?: return@mapNotNull null
                team to tag.getList("rewards", 8).map { it.asString }
            }.associate { it }
        }

        override fun default(): Map<PlayerTeam, List<String>> {
            return mapOf()
        }
    }

}