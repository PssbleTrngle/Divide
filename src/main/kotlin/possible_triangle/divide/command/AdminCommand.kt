package possible_triangle.divide.command

import net.minecraft.commands.Commands.literal
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.command.admin.CrateCommand
import possible_triangle.divide.command.admin.EventsCommand
import possible_triangle.divide.command.admin.ResetCommand
import possible_triangle.divide.command.admin.TeamCommand
import possible_triangle.divide.logic.Teams

@Mod.EventBusSubscriber
object AdminCommand {

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        val base = literal(DivideMod.ID).requires(Teams::isAdmin)

        TeamCommand.register(base)
        CrateCommand.register(base)
        EventsCommand.register(base)
        ResetCommand.register(base)

        event.dispatcher.register(base)
    }

}