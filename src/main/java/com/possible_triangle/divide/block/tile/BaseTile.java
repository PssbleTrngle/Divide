package com.possible_triangle.divide.block.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import java.util.Optional;

public class BaseTile extends TileEntity {

    public BaseTile(TileEntityType<?> type) {
        super(type);
    }

    public Optional<ServerWorld> getServerWorld() {
        if(world instanceof ServerWorld) return Optional.of((ServerWorld) world);
        return Optional.empty();
    }

    @Nonnull
    @Override
    public final CompoundNBT write(CompoundNBT tag) {
        CompoundNBT ret = super.write(tag);
        writePacketNBT(ret);
        return ret;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public final void read(CompoundNBT tag) {
        super.read(tag);
        readPacketNBT(tag);
    }

    public void writePacketNBT(CompoundNBT cmp) {}

    public void readPacketNBT(CompoundNBT cmp) {}

    @Override
    public final SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = new CompoundNBT();
        writePacketNBT(tag);
        return new SUpdateTileEntityPacket(pos, -999, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        super.onDataPacket(net, packet);
        readPacketNBT(packet.getNbtCompound());
    }

}
