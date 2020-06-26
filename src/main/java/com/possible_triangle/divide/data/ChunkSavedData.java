package com.possible_triangle.divide.data;

import com.google.common.collect.Maps;
import com.possible_triangle.divide.Divide;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ChunkSavedData extends WorldSavedData {

    private static final String UUID = Divide.MODID + ":chunks";
    private final HashMap<ChunkPos, ChunkProtection> DATA = Maps.newHashMap();

    public ChunkSavedData() {
        super(UUID);
    }

    public static Stream<Map.Entry<ChunkPos, ChunkProtection>> getChunks(ServerWorld world) {
        return getInstance(world).DATA.entrySet().stream();
    }

    private static ChunkSavedData getInstance(ServerWorld world) {
        return world.getSavedData().getOrCreate(ChunkSavedData::new, UUID);
    }

    public static Optional<ChunkProtection> getProtection(ServerWorld world, ChunkPos pos) {
        return Optional.ofNullable(getInstance(world).DATA.get(pos));
    }

    public static boolean createProtection(ServerWorld world, ChunkPos pos, ChunkProtection protection) {
        ChunkSavedData instance = getInstance(world);
        if(!instance.DATA.containsKey(pos)) {
            instance.DATA.put(pos, protection);
            instance.markDirty();
            return true;
        } else return false;
    }

    public static boolean removeProtection(ServerWorld world, ChunkPos pos) {
        ChunkSavedData instance = getInstance(world);
        if(instance.DATA.containsKey(pos)) {
            instance.DATA.remove(pos);
            instance.markDirty();
            return true;
        } else return false;
    }

    @Override
    public void read(CompoundNBT nbt) {
        DATA.clear();

        nbt.getList("chunks", 10).forEach(tag -> {
            CompoundNBT entry = (CompoundNBT) tag;
            ChunkPos pos = new ChunkPos(entry.getInt("x"), entry.getInt("z"));
            DATA.put(pos, ChunkProtection.deserializeNBT(entry));
        });
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        DATA.forEach((pos, data) -> {
            CompoundNBT nbt = data.serializeNBT();
            nbt.putInt("x", pos.x);
            nbt.putInt("z", pos.z);
            list.add(nbt);
        });
        compound.put("chunks", list);
        return compound;
    }

}
