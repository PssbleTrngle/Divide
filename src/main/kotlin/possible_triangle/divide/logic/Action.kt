package possible_triangle.divide.logic

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.saveddata.SavedData
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Reward
import possible_triangle.divide.data.Reward.Context

fun interface Action {

    fun start(ctx: Context)

    fun stop(ctx: Context) {}

    fun tick(ctx: Context) {}

    @Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    companion object {

        private fun getData(world: ServerLevel): Data {
            return world.server.overworld().dataStorage.computeIfAbsent(
                { load(it, world) },
                { Data() },
                "${DivideMod.ID}_actions"
            )
        }

        private fun load(nbt: CompoundTag, world: ServerLevel): Data {
            val data = Data()
            val list = nbt.getList("values", 10)
            list.filterIsInstance<CompoundTag>().forEach {
                val team = world.scoreboard.getPlayerTeam(it.getString("team"))
                val player = world.getPlayerByUUID(it.getUUID("player"))
                val target = world.getPlayerByUUID(it.getUUID("target"))
                val reward = Reward.valueOf("reward")

                if (team != null && player is ServerPlayer && target is ServerPlayer) {
                    val ctx = Context(team, world, player, target, reward)
                    data.running.add(ctx to it.getLong("time"))
                }
            }
            return data
        }

        fun run(action: Action, ctx: Context, duration: Int?) {
            val data = getData(ctx.world)
            action.start(ctx)

            DivideMod.LOGGER.info("Started '${ctx.reward.display}'")

            if (duration != null) {
                val until = ctx.world.gameTime + (duration * 20)
                data.running.add(ctx to until)
            }

            data.setDirty()

        }

        fun isRunning(world: ServerLevel, reward: Reward, predicate: (ctx: Context) -> Boolean = { true }): Boolean {
            val data = getData(world)
            return data.running.any { (ctx) -> ctx.reward == reward && predicate(ctx) }
        }

        @SubscribeEvent
        fun tick(event: TickEvent.WorldTickEvent) {
            val world = event.world
            if (world !is ServerLevel) return

            val data = getData(world)
            val now = world.gameTime
            val due = data.running.filter { (_, time) -> time < now }

            data.running.forEach { (ctx) ->
                ctx.reward.action.tick(ctx)
            }

            if (due.isNotEmpty()) {
                due.forEach { (ctx) ->
                    DivideMod.LOGGER.info("Stopped '${ctx.reward.display}'")
                    ctx.reward.action.stop(ctx)
                }

                data.running.removeAll(due)
                data.setDirty()
            }
        }

    }

    private class Data : SavedData() {

        val running = arrayListOf<Pair<Context, Long>>()

        override fun save(nbt: CompoundTag): CompoundTag {
            val list = ListTag()
            running.forEach { (ctx, time) ->
                val tag = CompoundTag()
                tag.putLong("time", time)
                tag.putString("team", ctx.team.name)
                tag.putUUID("player", ctx.player.uuid)
                tag.putUUID("target", ctx.target.uuid)
                tag.putString("reward", ctx.reward.name)
                list.add(tag)
            }
            nbt.put("values", list)
            return nbt
        }

    }

}