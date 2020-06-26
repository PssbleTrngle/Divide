package com.possible_triangle.divide.block;

import com.mojang.datafixers.util.Pair;
import com.possible_triangle.divide.block.tile.FlagTile;
import com.possible_triangle.divide.data.PlayerData;
import com.possible_triangle.divide.data.ChunkSavedData;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Flag extends TileHolder<FlagTile> {

    private static final VoxelShape SHAPE = Block.makeCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_0_15;
    public static final BooleanProperty MISSING = BooleanProperty.create("missing");
    //public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public Flag() {
        super(Properties.from(Blocks.ORANGE_BANNER)
                .hardnessAndResistance(1F, 3600000.0F)
                .noDrops()
        );
        setDefaultState(this.getStateContainer().getBaseState().with(ROTATION, 0).with(MISSING, false));
    }

    public int getBaseRank() {
        return 1;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(ROTATION, MISSING);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void miningSpeed(PlayerEvent.BreakSpeed event) {
        PlayerEntity player = event.getPlayer();
        Block block = event.getState().getBlock();
        if (!(block instanceof Flag)) return;
        Flag flag = (Flag) block;

        flag.getTE(player.getEntityWorld(), event.getPos())
                .filter(tile -> !tile.canBeBrokenBy(player))
                .ifPresent($ -> event.setNewSpeed(0));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
        return getTE(world, pos).filter(tile -> {

            Optional<Pair<BlockPos,String>> carries = PlayerData.carries(player);

            return carries.filter($ -> tile.isInTeam(player)).filter(c -> {
                Team team = world.getScoreboard().getTeam(c.getSecond());
                return getTE(world, c.getFirst()).filter(stolen -> {

                    stolen.stolenBy(player);
                    return true;

                }).isPresent();
            }).isPresent();

        }).map($ -> ActionResultType.SUCCESS).orElse(ActionResultType.PASS);
    }

    @SubscribeEvent
    public static void playerDestroyed(BlockEvent.BreakEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null) return;

        Block block = event.getState().getBlock();
        if (!(block instanceof Flag)) return;
        Flag flag = (Flag) block;

        flag.getTE(event.getWorld(), event.getPos()).ifPresent(te -> {
            if (!player.isCreative()) {
                if(te.canBeBrokenBy(player)) te.brokenBy(player);
                event.setCanceled(true);
            }
        });

    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FlagTile(getBaseRank());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if(world instanceof ServerWorld) ChunkSavedData.getProtection((ServerWorld) world, new ChunkPos(pos))
                .ifPresent($ -> placer.sendMessage(new TranslationTextComponent("message.divide.flag_overwritten")));

        if (placer != null && placer.getTeam() != null) getTE(world, pos).ifPresent(te -> te.setTeam(placer.getTeam()));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).getMaterial().isSolid();
    }


    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(ROTATION, MathHelper.floor((double) ((180.0F + context.getPlacementYaw()) * 16.0F / 360.0F) + 0.5D) & 15);
    }

    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !state.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(state, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean canSpawnInBlock() {
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
