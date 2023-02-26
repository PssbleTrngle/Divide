package possible_triangle.divide.data

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.DivideMod
import possible_triangle.divide.extensions.mainWorld

open class PerTeamData<Value, T : Tag>(
    private val key: String,
    private val initial: Value,
    private val serializer: (Value) -> T,
    private val deserializer: (T) -> Value,
) {

    operator fun get(server: MinecraftServer): Data {
        return server.mainWorld().dataStorage.computeIfAbsent(
            { load(server.mainWorld(), it) },
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
        internal val values = hashMapOf<PlayerTeam?, Value>()

        operator fun get(team: PlayerTeam?): Value {
            return values.getOrPut(team) { initial }
        }

        operator fun set(team: PlayerTeam?, value: Value) {
            values[team] = value
            isDirty = true
        }

        override fun save(nbt: CompoundTag) = nbt.apply {
            values.forEach { (team, value) ->
                nbt.put(team?.name ?: "", serializer(value))
            }
        }
    }

}