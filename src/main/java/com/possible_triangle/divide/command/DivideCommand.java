package com.possible_triangle.divide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.possible_triangle.divide.Divide;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class DivideCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal(Divide.MODID)
                        .then(RankCommand.register())
                        .then(FlagCommand.register())
                        .then(DeliveryCommand.register())
                        .then(ProtectionCommand.register())
        );
    }

}
