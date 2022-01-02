package possible_triangle.divide.logic

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.scores.Team
import possible_triangle.divide.DivideMod

class PerTeamData(private val key: String, private val initial: Int = 0) {

    fun get(world: ServerLevel): Data {
        return world.server.overworld().dataStorage.computeIfAbsent({ load(world, it) }, { Data() }, "${DivideMod.ID}_${key}")
    }

    private fun load(world: ServerLevel, nbt: CompoundTag): Data {
        val data = Data()
        nbt.allKeys.forEach {
            val team = world.scoreboard.getPlayerTeam(it)
            if(team != null) data.values[team] = nbt.getInt(it)
        }
        return data
    }

    inner class Data : SavedData() {
        internal val values = hashMapOf<Team, Int>()

        operator fun get(team: Team): Int {
            return values.getOrDefault(team, initial)
        }

        operator fun set(team: Team, value: Int) {
            values[team] = value
            setDirty()
        }

        override fun save(nbt: CompoundTag): CompoundTag {
            values.forEach { (team, value) ->
                nbt.putInt(team.name, value)
            }

            return nbt
        }
    }

}