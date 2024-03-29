package possible_triangle.divide.crates

import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.crates.callbacks.CleanCallback
import possible_triangle.divide.crates.callbacks.FillLootCallback
import possible_triangle.divide.crates.callbacks.MessageCallback
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.events.CycleEvent
import possible_triangle.divide.logic.Teams
import kotlin.random.Random

object CrateEvent : CycleEvent("loot_crates") {

    override val enabled: Boolean
        get() = Config.CONFIG.crate.enabled

    override val startsAfter: Int
        get() = Config.CONFIG.crate.startAfter

    override fun onStop(server: MinecraftServer) {
        CleanCallback.cancel(server)
        FillLootCallback.cancel(server)
        Teams.teams(server).forEach {
            MessageCallback.cancel(server, suffix = it.color.name.lowercase())
        }
    }

    override fun handle(server: MinecraftServer, index: Int): Int {

        val border = server.overworld().worldBorder

        try {
            val y = Config.CONFIG.crate.levels.random()
            val x = Random.nextInt(border.minX.toInt() + 15, border.maxX.toInt() - 15)
            val z = Random.nextInt(border.minZ.toInt() + 15, border.maxZ.toInt() - 15)

            val spawnAt = CrateScheduler.findInRange(server, BlockPos(x, y, z), 20.0)

            val lootTable = CrateLoot.random()

            if (lootTable != null) {
                if (spawnAt != null) CrateScheduler.schedule(server, Config.CONFIG.crate.lockedFor, spawnAt, lootTable)
                else DivideMod.LOGGER.warn("Could not find a crate position around $x/$y/$z")
            } else {
                DivideMod.LOGGER.warn("No loot tables defined")
            }

        } catch (e: IllegalArgumentException) {
            DivideMod.LOGGER.warn("Could not find a crate position because the border is too small")
        }

        return Config.CONFIG.crate.pause.value
    }

}