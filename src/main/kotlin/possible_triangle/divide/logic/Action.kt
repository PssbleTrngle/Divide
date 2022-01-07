package possible_triangle.divide.logic

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.saveddata.SavedData
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext

fun interface Action {

    fun start(ctx: RewardContext)

    fun stop(ctx: RewardContext) {}

    fun tick(ctx: RewardContext) {}

    @Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    companion object {

        private fun getData(server: MinecraftServer): Data {
            return server.overworld().dataStorage.computeIfAbsent(
                { load(it, server.overworld()) },
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

                val rewardId = it.getString(("reward"))
                val reward = Reward[rewardId] ?: throw NullPointerException("Missing $rewardId")

                if (team != null && player is ServerPlayer && target is ServerPlayer) {
                    val ctx = RewardContext(team, world.server, player, target, reward)
                    data.running.add(ctx to it.getLong("time"))
                }
            }
            return data
        }

        fun run(action: Action, ctx: RewardContext, duration: Int?) {
            val data = getData(ctx.server)
            action.start(ctx)

            DivideMod.LOGGER.info("Started '${ctx.reward.display}'")

            if (duration != null) {
                val until = ctx.server.overworld().gameTime + (duration * 20)
                data.running.add(ctx to until)
            }

            data.setDirty()

        }

        fun isRunning(
            server: MinecraftServer,
            reward: Reward,
            predicate: (ctx: RewardContext) -> Boolean = { true }
        ): Boolean {
            val data = getData(server)
            return data.running.any { (ctx) -> ctx.reward == reward && predicate(ctx) }
        }

        @SubscribeEvent
        fun tick(event: TickEvent.WorldTickEvent) {
        val server = event.world.server ?: return

            val data = getData(server)
            val now = server.overworld().gameTime
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

        val running = arrayListOf<Pair<RewardContext, Long>>()

        override fun save(nbt: CompoundTag): CompoundTag {
            val list = ListTag()
            running.forEach { (ctx, time) ->
                val tag = CompoundTag()
                tag.putLong("time", time)
                tag.putString("team", ctx.team.name)
                tag.putUUID("player", ctx.player.uuid)
                tag.putUUID("target", ctx.target.uuid)
                tag.putString("reward", Reward.idOf(ctx.reward))
                list.add(tag)
            }
            nbt.put("values", list)
            return nbt
        }

    }

}