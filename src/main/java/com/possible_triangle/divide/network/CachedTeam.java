package com.possible_triangle.divide.network;

import com.possible_triangle.divide.data.TeamSavedData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.stream.IntStream;

public final class CachedTeam {

    public final ITextComponent display;
    public final String name;
    public final int rank;
    public final CachedFlag[] flags;
    public final int color;

    public CachedTeam(ScorePlayerTeam team, ServerWorld world) {
        this(
                team.getName(), team.getDisplayName(),
                TeamSavedData.getRank(world, team), Optional.ofNullable(team.getColor().getColor()).orElse(0),
                TeamSavedData.getFlags(world, team).map(CachedFlag::new).toArray(CachedFlag[]::new)
        );
    }

    private CachedTeam(String name, ITextComponent display, int rank, int color, CachedFlag[] flags) {
        this.display = display;
        this.name = name;
        this.rank = rank;
        this.color = color;
        this.flags = flags;
    }

    public void write(PacketBuffer buffer) {

        buffer.writeInt(flags.length);
        for(CachedFlag flag : flags) flag.write(buffer);

        buffer.writeString(this.name);
        buffer.writeTextComponent(this.display);
        buffer.writeInt(this.rank);
        buffer.writeInt(this.color);
    }

    public static CachedTeam read(PacketBuffer buffer) {

        int length = buffer.readInt();
        CachedFlag[] teams = IntStream.range(0, length).mapToObj($ -> CachedFlag.read(buffer)).toArray(CachedFlag[]::new);

        return new CachedTeam(
                buffer.readString(),
                buffer.readTextComponent(),
                buffer.readInt(),
                buffer.readInt(),
                teams
        );
    }
}
