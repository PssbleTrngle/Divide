package com.possible_triangle.divide.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.possible_triangle.divide.data.PlayerData;
import com.possible_triangle.divide.data.TeamSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.TeamArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Optional;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;

public class FlagCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("flag")
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(FlagCommand::get)
                        )
                ).then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(FlagCommand::clear)
                        )
                );
    }

    public static int get(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        Optional<String> team = PlayerData.carries(player).map(Pair::getSecond);
        player.sendMessage(team
                .map(name -> new TranslationTextComponent("commands.divide.flag.has", player.getDisplayName(), name))
                .orElseGet(() -> new TranslationTextComponent("commands.divide.flag.none", player.getDisplayName()))
        );
        return team.isPresent() ? 1 : 0;
    }

    public static int clear(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        return PlayerData.clearFlag(player, true) ? 1 : 0;
    }

}
