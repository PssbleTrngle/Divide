package com.possible_triangle.divide.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.possible_triangle.divide.data.ChunkProtection;
import com.possible_triangle.divide.data.ChunkSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.TeamArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Optional;

public class ProtectionCommand {

    private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(pos -> new TranslationTextComponent("commands.divide.protection.not_found", pos));
    private static final DynamicCommandExceptionType ALREADY_EXISTS = new DynamicCommandExceptionType(pos -> new TranslationTextComponent("commands.divide.protection.already_exists", pos));

    public static ArgumentBuilder<CommandSource, ?> register() {
        return withOrWithoutPos(Commands.literal("protection").then(withOrWithoutPos(Commands.argument("pos", BlockPosArgument.blockPos()))));
    }

    private static ArgumentBuilder<CommandSource, ?> withOrWithoutPos(ArgumentBuilder<CommandSource, ?> builder) {
        return builder
                .then(Commands.literal("create")
                        .then(Commands.argument("team", TeamArgument.team())
                                .then(Commands.argument("type", EnumArgument.enumArgument(ChunkProtection.Type.class))
                                        .executes(ProtectionCommand::create))))
                .then(Commands.literal("get").executes(ProtectionCommand::get))
                .then(Commands.literal("remove").executes(ProtectionCommand::remove));
    }

    public static ChunkPos getPos(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        try {
            BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
            return new ChunkPos(pos);
        } catch (IllegalArgumentException ex) {
            return new ChunkPos(new BlockPos(ctx.getSource().getPos()));
        }
    }

    public static int remove(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ChunkPos pos = getPos(ctx);
        if (!ChunkSavedData.removeProtection(ctx.getSource().getWorld(), pos)) throw NOT_FOUND.create(pos);
        return 1;
    }

    public static int create(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ChunkPos pos = getPos(ctx);
        ChunkProtection protection = new ChunkProtection(ctx.getArgument("type", ChunkProtection.Type.class), TeamArgument.getTeam(ctx, "team").getName());
        if (!ChunkSavedData.createProtection(ctx.getSource().getWorld(), pos, protection))
            throw ALREADY_EXISTS.create(pos);
        return 1;
    }

    public static int get(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ChunkPos pos = getPos(ctx);
        Optional<ChunkProtection> protection = ChunkSavedData.getProtection(ctx.getSource().getWorld(), pos);
        protection.ifPresent(p -> ctx.getSource().sendFeedback(new TranslationTextComponent("command.divide.protection.get", p.team, p.type.name().toLowerCase()), true));
        if (!protection.isPresent()) throw NOT_FOUND.create(pos);
        return 1;
    }

}
