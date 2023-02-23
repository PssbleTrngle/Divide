package possible_triangle.divide.data

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import possible_triangle.divide.DivideMod

abstract class ModSavedData<T>(private val id: String) {

    abstract fun save(nbt: NbtCompound, value: T)

    abstract fun load(nbt: NbtCompound, server: MinecraftServer): T

    abstract fun default(): T

    private fun getData(server: MinecraftServer): Data {
        return server.overworld.persistentStateManager.getOrCreate(
            { Data(load(it, server)) },
            { Data() },
            "${DivideMod.ID}_$id"
        )
    }

    operator fun get(server: MinecraftServer): T {
        return getData(server).value
    }

    operator fun set(server: MinecraftServer, value: T) {
        val data = getData(server)
        data.value = value
        data.isDirty = true
    }

    fun <V> modify(server: MinecraftServer, consumer: T.() -> V): V {
        val data = getData(server)
        val out = consumer(data.value)
        data.isDirty = true
        return out
    }

    inner class Data() : PersistentState() {

        internal var value: T = default()

        constructor(initial: T) : this() {
            value = initial
        }

        override fun writeNbt(nbt: NbtCompound): NbtCompound {
            save(nbt, value)
            return nbt
        }

    }

}