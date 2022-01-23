package possible_triangle.divide.actions

import possible_triangle.divide.command.admin.CrateCommand.NO_CRATE_POS
import possible_triangle.divide.command.admin.CrateCommand.NO_LOOT_DEFINED
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.loot.CrateLoot
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.RewardContext

object OrderLootCrate : Action() {

    override fun prepare(ctx: RewardContext) {
        val table = CrateLoot.random() ?: throw NO_LOOT_DEFINED.create()
        val center = ctx.player.blockPosition()
        val pos = CrateScheduler.findInRange(ctx.server, center, 10.0) ?: throw NO_CRATE_POS.create(center)

        val seconds = ctx.reward.charge ?: 0
        val due = CrateScheduler.prepare(ctx.server, seconds, pos, table)

        val teams = Teams.teams(ctx.server)
        teams.forEach { CrateScheduler.scheduleMessage(ctx.server, 1, pos, due, it) }
    }

}