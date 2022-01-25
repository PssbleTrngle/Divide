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
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util

abstract class Action {

    open fun <T> start(ctx: RewardContext<T>) {}

    open fun <T> prepare(ctx: RewardContext<T>) {}

    open fun <T> stop(ctx: RewardContext<T>) {}

    open fun <T> tick(ctx: RewardContext<T>) {}

    @Mod.EventBusSubscriber(modid = DivideMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    companion object {

        val NOT_ONLINE = SimpleCommandExceptionType(TextComponent("Target is not online"))

        fun <T> run(ctx: RewardContext<T>, duration: Int?) {
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

        fun isRunning(
            server: MinecraftServer,
            reward: Reward,
            predicate: (ctx: RewardContext<*>) -> Boolean = { true }
        ): Boolean {
            return DATA[server].any { (ctx) -> ctx.reward == reward && predicate(ctx) }
        }

        fun running(server: MinecraftServer): List<ActionContext<*>> {
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
                    ctx.stop()
                }

                DATA.modify(server) {
                    removeAll(due)
                }
            }
        }

        data class ActionContext<T>(val ctx: RewardContext<T>, val until: Long, val chargedAt: Long?)

        private val DATA = object : ModSavedData<MutableList<ActionContext<*>>>("actions") {
            override fun save(nbt: CompoundTag, value: MutableList<ActionContext<*>>) {
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

            private fun load(
                nbt: CompoundTag,
                server: MinecraftServer,
                reward: Reward
            ): ActionContext<*>? {
                val team = server.scoreboard.getPlayerTeam(nbt.getString("team")) ?: return null

                val ctx = nbt.getCompound("target").let { tag ->
                    val type = ActionTarget[tag.getString("type")] ?: return null

                    fun <T> createContext(type: ActionTarget<T>): RewardContext<T>? {
                        val target = type.deserialize(tag) ?: return null
                        return RewardContext(team, server, nbt.getUUID("player"), target, reward, type)
                    }

                    createContext(type)
                } ?: return null

                val chargedAt = if (nbt.contains("chargedAt")) nbt.getLong("chargedAt") else null

                return ActionContext(ctx, nbt.getLong("time"), chargedAt)
            }

            override fun load(nbt: CompoundTag, server: MinecraftServer): MutableList<ActionContext<*>> {
                val list = nbt.getList("values", 10)
                return list.filterIsInstance<CompoundTag>().mapNotNull {
                    val reward = Reward.getOrThrow(it.getString("reward"))
                    load(it, server, reward)
                }.toMutableList()
            }

            override fun default(): MutableList<ActionContext<*>> {
                return mutableListOf()
            }
        }
    }

}