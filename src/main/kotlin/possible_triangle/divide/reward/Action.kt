package possible_triangle.divide.reward

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.GameData
import possible_triangle.divide.data.EventPlayer
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util
import possible_triangle.divide.logging.EventLogger

abstract class Action<Raw, Target>(val target: ActionTarget<Raw, Target>) {

    open fun start(ctx: RewardContext<Raw, Target>) {}

    open fun prepare(ctx: RewardContext<Raw, Target>) {}

    open fun stop(ctx: RewardContext<Raw, Target>) {}

    open fun tick(ctx: RewardContext<Raw, Target>) {}

    @Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    companion object {

        val NOT_ONLINE = SimpleCommandExceptionType(TextComponent("Target is not online"))

        private val LOGGER = EventLogger("reward_stop") { Reward.Event.serializer() }

        fun <R, T> run(ctx: RewardContext<R, T>, duration: Int?) {
            ctx.prepare()
            if (ctx.reward.charge == null) ctx.start()

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

        fun <R,T> isRunning(
            server: MinecraftServer,
            action: Action<R,T>,
            predicate: (ctx: RewardContext<R,T>) -> Boolean = { true }
        ): Boolean {
            return DATA[server].any { (ctx) -> ctx.action == action && predicate(ctx as RewardContext<R,T>) }
        }

        fun <R,T> isRunning(
            server: MinecraftServer,
            reward: Reward,
            predicate: (ctx: RewardContext<R,T>) -> Boolean = { true }
        ): Boolean {
            return DATA[server].any { (ctx) -> ctx.reward == reward && predicate(ctx as RewardContext<R,T>) }
        }

        fun running(server: MinecraftServer): List<ActionContext<*,*>> {
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
                    ctx.tick()
                    if (chargedAt != null && chargedAt == now) {
                        ctx.start()
                    }
                } catch (e: Exception) {
                    DivideMod.LOGGER.error("Exception occurred with action ${ctx.reward.id}: ${e.message}")
                }
            }

            if (due.isNotEmpty()) {
                due.forEach { (ctx) ->
                    LOGGER.log(
                        server, Reward.Event(
                            ctx.reward.id, EventPlayer.optional(ctx.player),
                            ctx.targetEvent(),
                        )
                    )
                    ctx.stop()
                }

                DATA.modify(server) {
                    removeAll(due)
                }
            }
        }

        data class ActionContext<R, T>(val ctx: RewardContext<R, T>, val until: Long, val chargedAt: Long?)

        private val DATA = object : ModSavedData<MutableList<ActionContext<*, *>>>("actions") {
            override fun save(nbt: CompoundTag, value: MutableList<ActionContext<*, *>>) {
                nbt.put("values", value.mapTo(ListTag()) { (ctx, time, chargedAt) ->
                    CompoundTag().apply {
                        putLong("time", time)
                        ActionTarget.serialize(ctx, this)
                        if (chargedAt != null) putLong("chargedAt", chargedAt)
                        putString("team", ctx.team.name)
                        putUUID("player", ctx.rawPlayer)
                        putString("reward", ctx.reward.id)
                    }
                })
            }

            private fun <R, T> load(
                nbt: CompoundTag,
                server: MinecraftServer,
                action: Action<R, T>,
                reward: Reward
            ): ActionContext<R, T>? {
                val team = server.scoreboard.getPlayerTeam(nbt.getString("team")) ?: return null
                val target = ActionTarget.deserialize(action, nbt) ?: return null
                val ctx = RewardContext<R, T>(team, server, nbt.getUUID("player"), target, reward)
                val chargedAt = if (nbt.contains("chargedAt")) nbt.getLong("chargedAt") else null
                return ActionContext(ctx, nbt.getLong("time"), chargedAt)
            }

            override fun load(nbt: CompoundTag, server: MinecraftServer): MutableList<ActionContext<*, *>> {
                val list = nbt.getList("values", 10)
                return list.filterIsInstance<CompoundTag>().mapNotNull {
                    val reward = Reward.getOrThrow(it.getString("reward"))
                    load(it, server, reward.action, reward)
                }.toMutableList()
            }

            override fun default(): MutableList<ActionContext<*, *>> {
                return mutableListOf()
            }
        }
    }

}