package com.possible_triangle.divide.data;

import com.google.common.collect.Maps;
import com.possible_triangle.divide.Divide;
import com.possible_triangle.divide.block.tile.FlagTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

public class TeamSavedData extends WorldSavedData {

    public static final int START_RANK = 10;

    private static final String UUID = Divide.MODID + ":teams";
    private HashMap<String,TeamData> DATA = Maps.newHashMap();

    public TeamSavedData() {
        super(UUID);
    }

    private static TeamSavedData getInstance(ServerWorld world) {
        return world.getSavedData().getOrCreate(TeamSavedData::new, UUID);
    }

    public TeamData getData(Team team) {
        TeamData data = DATA.computeIfAbsent(team.getName(), $ -> new TeamData(START_RANK));
        markDirty();
        return data;
    }

    public static int getRank(ServerWorld world, Team team) {
        TeamSavedData instance = getInstance(world);
        return instance.getData(team).getRank();
    }

    public static Stream<FlagTile> getFlags(ServerWorld world, Team team) {
        return world.loadedTileEntityList.stream()
                .filter(FlagTile.class::isInstance)
                .map(tile -> (FlagTile) tile)
                .filter(tile -> tile.getTeam(world).filter(t -> t.isSameTeam(team)).isPresent());
    }

    public static void updateRank(ServerWorld world, Team team) {
        int rank = getFlags(world, team)
                .map(FlagTile::getRank)
                .reduce(Integer::sum)
                .orElse(0);

        modifyRank(world, team, $ -> rank);
    }

    public static void modifyRank(ServerWorld world, Team team, IntUnaryOperator operation) {
        TeamSavedData instance = getInstance(world);
        TeamData data = instance.getData(team);

        int newRank = Math.max(0, operation.applyAsInt(data.getRank()));
        data.setRank(newRank);
        instance.markDirty();
    }

    @Override
    public void read(CompoundNBT nbt) {
        DATA.clear();

        nbt.getList("data", 10).forEach(tag -> {
            String team = ((CompoundNBT) tag).getString("team");
            int rank = ((CompoundNBT) tag).getInt("rank");
            DATA.put(team, new TeamData(rank));
        });
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        DATA.forEach((team, data) -> {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("team", team);
            nbt.putInt("rank", data.getRank());
            list.add(nbt);
        });
        compound.put("data", list);
        return compound;
    }
}
