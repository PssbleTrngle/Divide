package possible_triangle.divide.data

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData
import possible_triangle.divide.DivideMod

abstract class ModSavedData<T>(private val id: String) {

    abstract fun save(nbt: CompoundTag, value: T)

    abstract fun load(nbt: CompoundTag, server: MinecraftServer): T

    abstract fun default(): T

    private fun getData(server: MinecraftServer): Data {
        return server.overworld().dataStorage.computeIfAbsent({ Data(load(it, server)) }, ::Data, "${DivideMod.ID}_$id")
    }

    operator fun get(server: MinecraftServer): T {
        return getData(server).value
    }

    operator fun set(server: MinecraftServer, value: T) {
        val data = getData(server)
        data.value = value
        data.setDirty()
    }

    fun <V> modify(server: MinecraftServer, consumer: T.() -> V): V {
        val data = getData(server)
        val out = consumer(data.value)
        data.setDirty()
        return out
    }

    inner class Data() : SavedData() {

        internal var value: T = default()

        constructor(initial: T) : this() {
            value = initial
        }

        override fun save(nbt: CompoundTag): CompoundTag {
            save(nbt, value)
            return nbt
        }

    }

}