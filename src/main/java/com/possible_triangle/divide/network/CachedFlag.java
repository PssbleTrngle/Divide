package com.possible_triangle.divide.network;

import com.possible_triangle.divide.block.tile.FlagTile;
import com.possible_triangle.divide.data.TeamSavedData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;

public final class CachedFlag {

    public final int rank;
    public final int protection;

    public CachedFlag(FlagTile tile) {
        this(tile.getRank(), tile.getProtection());
    }

    private CachedFlag(int rank, int protection) {
        this.rank = rank;
        this.protection = protection;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeInt(this.rank);
        buffer.writeInt(this.protection);
    }

    public static CachedFlag read(PacketBuffer buffer) {
        return new CachedFlag(
                buffer.readInt(),
                buffer.readInt()
        );
    }
}
