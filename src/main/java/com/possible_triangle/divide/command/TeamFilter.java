package com.possible_triangle.divide.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.Team;

import java.util.function.Predicate;

public interface TeamFilter {

    Predicate<Team> get(CommandContext<CommandSource> ctx) throws CommandSyntaxException;

}
