package possible_triangle.divide.data

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.scores.Team
import possible_triangle.divide.DivideMod

open class PerTeamData<Value, T : Tag>(
    private val key: String,
    private val initial: Value,
    private val serializer: (Value) -> T,
    private val deserializer: (T) -> Value,
) {

    operator fun get(server: MinecraftServer): Data {
        return server.overworld().dataStorage.computeIfAbsent(
            { load(server.overworld(), it) },
            { Data() },
            "${DivideMod.ID}_${key}"
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun load(world: ServerLevel, nbt: CompoundTag): Data {
        val data = Data()
        nbt.allKeys.forEach {
            val team = world.scoreboard.getPlayerTeam(it)
            if (team != null) {
                val tag = nbt.get(it)
                data.values[team] = if (tag != null) deserializer(tag as T) else initial
            }
        }
        return data
    }

    inner class Data : SavedData() {
        internal val values = hashMapOf<Team?, Value>()

        operator fun get(team: Team?): Value {
            return values.getOrPut(team) { initial }
        }

        operator fun set(team: Team?, value: Value) {
            values[team] = value
            setDirty()
        }

        override fun save(nbt: CompoundTag): CompoundTag {
            values.forEach { (team, value) ->
                nbt.put(team?.name ?: "", serializer(value))
            }

            return nbt
        }
    }

}