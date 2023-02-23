package possible_triangle.divide.data

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState
import possible_triangle.divide.DivideMod

open class PerTeamData<Value, T : NbtElement>(
    private val key: String,
    private val initial: Value,
    private val serializer: (Value) -> T,
    private val deserializer: (T) -> Value,
) {

    operator fun get(server: MinecraftServer): Data {
        return server.overworld.persistentStateManager.getOrCreate(
            { load(server.overworld, it) },
            { Data() },
            "${DivideMod.ID}_${key}"
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun load(world: ServerWorld, nbt: NbtCompound): Data {
        val data = Data()
        nbt.keys.forEach {
            val team = world.scoreboard.getPlayerTeam(it)
            if (team != null) {
                val tag = nbt.get(it)
                data.values[team] = if (tag != null) deserializer(tag as T) else initial
            }
        }
        return data
    }

    inner class Data : PersistentState() {
        internal val values = hashMapOf<Team?, Value>()

        operator fun get(team: Team?): Value {
            return values.getOrPut(team) { initial }
        }

        operator fun set(team: Team?, value: Value) {
            values[team] = value
            isDirty = true
        }

        override fun writeNbt(nbt: NbtCompound) = nbt.apply {
            values.forEach { (team, value) ->
                nbt.put(team?.name ?: "", serializer(value))
            }
        }
    }

}