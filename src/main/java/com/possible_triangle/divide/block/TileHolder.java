package com.possible_triangle.divide.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Optional;

public abstract class TileHolder<T extends TileEntity> extends Block  {

    public TileHolder(Properties props) {
        super(props.doesNotBlockMovement());
    }

    public final Optional<T> getTE(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        try {
            return Optional.ofNullable((T) te);
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    public boolean eventReceived(BlockState state, World world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        return getTE(world, pos).map(te -> te.receiveClientEvent(id, param)).orElse(false);
    }

}
