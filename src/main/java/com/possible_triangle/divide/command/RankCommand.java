package com.possible_triangle.divide.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.possible_triangle.divide.data.TeamSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.TeamArgument;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.command.EnumArgument;

import java.util.function.*;

public class RankCommand {

    /*
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.divide.rank.failed"));

    public enum Operation {
        set((a, b) -> b),
        add(Integer::sum),
        remove((a, b) -> a - b),
        multiply((a, b) -> a * b),
        divide((a, b) -> a / b);

        public final IntBinaryOperator func;

        Operation(IntBinaryOperator func) {
            this.func = func;
        }
    }
    */

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("rank")
                /*.then(Commands.literal("*")
                        .then(modify(ctx -> t -> true))
                )*/.then(Commands.argument("team", TeamArgument.team())
                        //.then(modify(ctx -> TeamArgument.getTeam(ctx, "team")::isSameTeam))
                        .then(Commands.literal("get").executes(RankCommand::get))
                        .then(Commands.literal("update").executes(RankCommand::update))
                );
    }

    public static int get(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Team team = TeamArgument.getTeam(ctx, "team");
        int rank = TeamSavedData.getRank(ctx.getSource().getWorld(), team);
        ctx.getSource().sendFeedback(new TranslationTextComponent("command.divide.team_rank", team.getName(), rank), true);
        return rank;
    }

    public static int update(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Team team = TeamArgument.getTeam(ctx, "team");
        TeamSavedData.updateRank(ctx.getSource().getWorld(), team);
        return 1;
    }

    /*
    private static ArgumentBuilder<CommandSource, ?> modify(TeamFilter filter) {
        return Commands.argument("operation", EnumArgument.enumArgument(Operation.class))
                .then(Commands.argument("value", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                        .executes(ctx -> modify(ctx, filter.get(ctx)))
                );
    }

    public static int modify(CommandContext<CommandSource> ctx, Predicate<Team> targeting) throws CommandSyntaxException {
        Operation operation = ctx.getArgument("operation", Operation.class);
        int value = IntegerArgumentType.getInteger(ctx, "value");
        ServerWorld world = ctx.getSource().getWorld();

        int affected = (int) world.getScoreboard().getTeams().stream()
                .filter(targeting)
                .peek(team -> TeamSavedData.modifyRank(world, team, h -> operation.func.applyAsInt(h, value))).count();

        if (affected == 0) throw FAILED_EXCEPTION.create();

        return affected;
    }
    */

}
