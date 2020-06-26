package com.possible_triangle.divide.data;

import com.mojang.datafixers.util.Pair;
import com.possible_triangle.divide.block.Flag;
import com.possible_triangle.divide.block.tile.FlagTile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.function.Function;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerData {

    static final DataParameter<Optional<BlockPos>> HAS_FLAG_POS = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    static final DataParameter<Optional<String>> HAS_FLAG_TEAM = EntityDataManager.createKey(PlayerEntity.class, ModSerializers.OPTIONAL_STRING);
    static final DataParameter<Optional<String>> IN_CHUNK_BY = EntityDataManager.createKey(PlayerEntity.class, ModSerializers.OPTIONAL_STRING);

    @SubscribeEvent
    public static void playerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        clearFlag(event.getPlayer(), true);
    }

    @SubscribeEvent
    public static void playerDied(LivingDeathEvent event) {
        if(event.getEntity() instanceof PlayerEntity) clearFlag((PlayerEntity) event.getEntity(), true);
    }

    @SubscribeEvent
    public static void entityConstruction(EntityEvent.EntityConstructing event) {
        Entity entity = event.getEntity();
        if (entity instanceof PlayerEntity) {
            entity.getDataManager().register(HAS_FLAG_POS, Optional.empty());
            entity.getDataManager().register(HAS_FLAG_TEAM, Optional.empty());
            entity.getDataManager().register(IN_CHUNK_BY, Optional.empty());
        }
    }

    public static void setFlag(PlayerEntity entity, BlockPos pos) {
        World world = entity.getEntityWorld();
        TileEntity tile = world.getTileEntity(pos);
        clearFlag(entity, true);
        clearFlag(pos, world);
        if (!(tile instanceof FlagTile)) return;

        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(Flag.MISSING, true));

        ((FlagTile) tile).getTeamName().ifPresent(flag -> {
            entity.getDataManager().set(HAS_FLAG_POS, Optional.of(pos));
            entity.getDataManager().set(HAS_FLAG_TEAM, Optional.of(flag));
        });
    }

    public static void clearFlag(BlockPos pos, World world) {
        world.getPlayers().stream()
                .map(Entity::getDataManager)
                .filter(d -> d.get(HAS_FLAG_POS).filter(p -> p.equals(pos)).isPresent())
                .forEach(d -> {
                    d.set(HAS_FLAG_POS, Optional.empty());
                    d.set(HAS_FLAG_TEAM, Optional.empty());
                });
    }

    public static boolean clearFlag(PlayerEntity entity, boolean notify) {
        return entity.getDataManager().get(HAS_FLAG_POS).map(pos -> {
            World world = entity.getEntityWorld();
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof FlagTile) ((FlagTile) tile).returnBanner(notify);
            entity.getDataManager().set(HAS_FLAG_POS, Optional.empty());
            entity.getDataManager().set(HAS_FLAG_TEAM, Optional.empty());
            return true;
        }).orElse(false);
    }

    public static Optional<Pair<BlockPos,String>> carries(PlayerEntity entity) {
        Optional<String> team = entity.getDataManager().get(HAS_FLAG_TEAM);
        Optional<BlockPos> pos = entity.getDataManager().get(HAS_FLAG_POS);
        return team.map(t -> pos.map(p -> new Pair<>(p, t))).flatMap(Function.identity());
    }

    public static boolean hasFlag(PlayerEntity entity) {
        return carries(entity).isPresent();
    }

}
