package possible_triangle.divide.reward

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.logic.Teams.participingTeams

object SecretRewards {

    fun isVisible(server: MinecraftServer, team: Team, reward: Reward): Boolean {
        return DATA[server][team]?.contains(reward.id) == true
    }

    fun choose(server: MinecraftServer) {
        val teams = server.participingTeams()
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

    private val DATA = object : ModSavedData<Map<Team, List<String>>>("secret_rewards") {
        override fun save(nbt: NbtCompound, value: Map<Team, List<String>>) {
            nbt.put("values", value.mapTo(NbtList()) { (team, rewards) ->
                NbtCompound().apply {
                    putString("team", team.name)
                    put("rewards", rewards.mapTo(NbtList()) { NbtString.of(it) })
                }
            })
        }

        override fun load(nbt: NbtCompound, server: MinecraftServer): Map<Team, List<String>> {
            return nbt.getList("values", 10).map { it as NbtCompound }.mapNotNull { tag ->
                val team = server.scoreboard.getPlayerTeam(tag.getString("team")) ?: return@mapNotNull null
                team to tag.getList("rewards", 8).map { it.asString() }
            }.associate { it }
        }

        override fun default(): Map<Team, List<String>> {
            return mapOf()
        }
    }

}