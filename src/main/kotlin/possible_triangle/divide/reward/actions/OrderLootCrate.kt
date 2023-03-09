package possible_triangle.divide.reward.actions

import possible_triangle.divide.command.admin.CrateCommand.NO_CRATE_POS
import possible_triangle.divide.command.admin.CrateCommand.NO_LOOT_DEFINED
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.extensions.ticks
import possible_triangle.divide.logic.Teams.participingTeams
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext
import kotlin.time.Duration

object OrderLootCrate : Action() {

    override fun <T> prepare(ctx: RewardContext<T>) {
        val center = ctx.targetPlayer()?.blockPosition() ?: return
        val table = CrateLoot.random() ?: throw NO_LOOT_DEFINED.create()
        val pos = CrateScheduler.findInRange(ctx.server, center, 10.0) ?: throw NO_CRATE_POS.create(center)

        val duration = ctx.reward.charge ?: Duration.ZERO
        val due = CrateScheduler.prepare(ctx.server, duration, pos, table, withoutOrders = true)

        val teams = ctx.server.participingTeams()
        teams.forEach { CrateScheduler.scheduleMessage(ctx.server, 1.ticks, pos, due, it) }
    }

}