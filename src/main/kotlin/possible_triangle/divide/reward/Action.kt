package possible_triangle.divide.reward

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import possible_triangle.divide.DivideMod
import possible_triangle.divide.GameData
import possible_triangle.divide.data.ModSavedData

abstract class Action {

    open fun <T> start(ctx: RewardContext<T>) {}

    open fun <T> prepare(ctx: RewardContext<T>) {}

    open fun <T> stop(ctx: RewardContext<T>) {}

    open fun <T> tick(ctx: RewardContext<T>) {}

    companion object {

        val NOT_ONLINE = SimpleCommandExceptionType(Text.literal("Target is not online"))

        fun <T> run(ctx: RewardContext<T>, duration: Int? = ctx.reward.duration, charge: Int? = ctx.reward.charge) {
            ctx.prepare()
            if (charge == null) ctx.start()

            val realDuration = (duration ?: 0) + (charge ?: 0)
            if (realDuration > 0) {
                val now = ctx.server.overworld.time
                val until = now + (realDuration * 20)
                val chargedAt = charge?.times(20)?.plus(now)
                DATA.modify(ctx.server) {
                    add(ActionContext(ctx, until, chargedAt))
                }
            }
        }

        fun isRunning(
            server: MinecraftServer,
            reward: Reward,
            ifCharged: Boolean = false,
            predicate: (ctx: RewardContext<*>) -> Boolean = { true },
        ): Boolean {
            return DATA[server].any {
                it.ctx.reward == reward
                        && predicate(it.ctx)
                        && (!ifCharged || (it.chargedAt ?: 0) <= server.overworld.time)
            }
        }

        fun running(server: MinecraftServer): List<ActionContext<*>> {
            return DATA[server].toList()
        }

        init {
            ServerTickEvents.START_SERVER_TICK.register { server ->
                if (server.overworld.time % 20 != 0L) return@register
                if (GameData.DATA[server].paused) return@register

                val running = DATA[server]
                val now = server.overworld.time
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
        }

        data class ActionContext<T>(val ctx: RewardContext<T>, val until: Long, val chargedAt: Long?)

        private val DATA = object : ModSavedData<MutableList<ActionContext<*>>>("actions") {
            override fun save(nbt: NbtCompound, value: MutableList<ActionContext<*>>) {
                nbt.put("values", value.mapTo(NbtList()) { (ctx, time, chargedAt) ->
                    NbtCompound().apply {
                        putLong("time", time)
                        ActionTarget.serialize(ctx, this)
                        if (chargedAt != null) putLong("chargedAt", chargedAt)
                        putString("team", ctx.team.name)
                        putUuid("player", ctx.rawPlayer)
                        putString("reward", ctx.reward.id)
                    }
                })
            }

            private fun load(
                nbt: NbtCompound,
                server: MinecraftServer,
                reward: Reward,
            ): ActionContext<*>? {
                val team = server.scoreboard.getPlayerTeam(nbt.getString("team")) ?: return null

                val ctx = nbt.getCompound("target").let { tag ->
                    val type = ActionTarget[tag.getString("type")] ?: return null

                    fun <T> createContext(type: ActionTarget<T>): RewardContext<T>? {
                        val target = type.deserialize(tag) ?: return null
                        return RewardContext(team, server, nbt.getUuid("player"), target, reward, type)
                    }

                    createContext(type)
                } ?: return null

                val chargedAt = if (nbt.contains("chargedAt")) nbt.getLong("chargedAt") else null

                return ActionContext(ctx, nbt.getLong("time"), chargedAt)
            }

            override fun load(nbt: NbtCompound, server: MinecraftServer): MutableList<ActionContext<*>> {
                val list = nbt.getList("values", 10)
                return list.filterIsInstance<NbtCompound>().mapNotNull {
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