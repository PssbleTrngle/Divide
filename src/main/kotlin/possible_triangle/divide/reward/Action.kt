package possible_triangle.divide.reward

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.GameData
import possible_triangle.divide.data.EventPlayer
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util
import possible_triangle.divide.logging.EventLogger

abstract class Action(val targets: Target? = null) {

    enum class Target { PLAYER, TEAM }

    open fun start(ctx: RewardContext) {}

    open fun prepare(ctx: RewardContext) {}

    open fun stop(ctx: RewardContext) {}

    open fun tick(ctx: RewardContext) {}

    @Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    companion object {

        private val LOGGER = EventLogger("reward_stop") { Reward.Event.serializer() }

        fun run(action: Action, ctx: RewardContext, duration: Int?) {
            action.prepare(ctx)
            if (ctx.reward.charge == null) action.start(ctx)

            val realDuration = (duration ?: 0) + (ctx.reward.charge ?: 0)
            if (realDuration > 0) {
                val now = ctx.server.overworld().gameTime
                val until = now + (realDuration * 20)
                val chargedAt = ctx.reward.charge?.times(20)?.plus(now)
                DATA.modify(ctx.server) {
                    add(ActionContext(ctx, until, chargedAt))
                }
            }
        }

        fun isRunning(
            server: MinecraftServer,
            reward: Reward,
            predicate: (ctx: RewardContext) -> Boolean = { true }
        ): Boolean {
            return DATA[server].any { (ctx) -> ctx.reward == reward && predicate(ctx) }
        }

        fun running(server: MinecraftServer): List<ActionContext> {
            return DATA[server].toList()
        }

        @SubscribeEvent
        fun tick(event: TickEvent.WorldTickEvent) {
            if (Util.shouldSkip(event, { it.world }, ticks = 1)) return

            val server = event.world.server ?: return
            if (GameData.DATA[server].paused) return

            val running = DATA[server]
            val now = server.overworld().gameTime
            val due = running.filter { (_, time) -> time < now }

            running.forEach { (ctx, _, chargedAt) ->
                try {
                    ctx.reward.action.tick(ctx)
                    if (chargedAt != null && chargedAt == now) {
                        ctx.reward.action.start(ctx)
                    }
                } catch (e: Exception) {
                    DivideMod.LOGGER.error("Exception occurred with action ${ctx.reward.id}: ${e.message}")
                }
            }

            if (due.isNotEmpty()) {
                due.forEach { (ctx) ->
                    LOGGER.log(
                        server, Reward.Event(
                            ctx.reward.id, EventPlayer.of(ctx.player),
                            EventPlayer.of(ctx.target).takeIf { ctx.target != ctx.player },
                        )
                    )
                    ctx.reward.action.stop(ctx)
                }

                DATA.modify(server) {
                    removeAll(due)
                }
            }
        }

        data class ActionContext(val ctx: RewardContext, val until: Long, val chargedAt: Long?)

        private val DATA = object : ModSavedData<MutableList<ActionContext>>("actions") {
            override fun save(nbt: CompoundTag, value: MutableList<ActionContext>) {
                nbt.put("values", value.mapTo(ListTag()) { (ctx, time, chargedAt) ->
                    val tag = CompoundTag()
                    tag.putLong("time", time)
                    if (chargedAt != null) tag.putLong("chargedAt", chargedAt)
                    tag.putString("team", ctx.team.name)
                    tag.putUUID("player", ctx.player.uuid)
                    tag.putUUID("target", ctx.target.uuid)
                    tag.putString("reward", ctx.reward.id)
                    tag
                })
            }

            override fun load(nbt: CompoundTag, server: MinecraftServer): MutableList<ActionContext> {
                val list = nbt.getList("values", 10)
                return list.filterIsInstance<CompoundTag>().mapNotNull {
                    val team = server.scoreboard.getPlayerTeam(it.getString("team"))
                    val player = server.playerList.getPlayer(it.getUUID("player"))
                    val target = server.playerList.getPlayer(it.getUUID("target"))

                    val reward = Reward.getOrThrow(it.getString("reward"))

                    if (team != null && player is ServerPlayer && target is ServerPlayer) {
                        val ctx = RewardContext(team, server, player, target, reward)
                        val chargedAt = if (it.contains("chargedAt")) it.getLong("chargedAt") else null
                        ActionContext(ctx, it.getLong("time"), chargedAt)
                    } else {
                        null
                    }
                }.toMutableList()
            }

            override fun default(): MutableList<ActionContext> {
                return mutableListOf()
            }
        }
    }

}