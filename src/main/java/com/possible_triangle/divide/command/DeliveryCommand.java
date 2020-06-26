package com.possible_triangle.divide.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.possible_triangle.divide.data.DeliverySavedData;
import com.possible_triangle.divide.data.TeamSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.TeamArgument;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TranslationTextComponent;

public class DeliveryCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("delivery")
                .then(Commands.literal("next").executes(DeliveryCommand::next))
                .then(Commands.literal("set").then(Commands.literal("frequency")
                        .then(Commands.argument("value", IntegerArgumentType.integer(DeliverySavedData.MIN_FREQUENCY, Integer.MAX_VALUE))
                                .executes(DeliveryCommand::setFrequency))));
    }

    public static int setFrequency(CommandContext<CommandSource> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        DeliverySavedData.setFrequency(ctx.getSource().getWorld(), value);
        return value;
    }

    public static int next(CommandContext<CommandSource> ctx) {
        long nextAt = DeliverySavedData.getNext(ctx.getSource().getWorld());
        long until = nextAt - ctx.getSource().getWorld().getGameTime();
        ctx.getSource().sendFeedback(new TranslationTextComponent("command.divide.delivery.next", until), true);
        return (int) until;
    }

}
