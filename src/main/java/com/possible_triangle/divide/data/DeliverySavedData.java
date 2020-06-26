package com.possible_triangle.divide.data;

import com.possible_triangle.divide.Divide;
import com.possible_triangle.divide.network.DeliveryEvent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeliverySavedData extends WorldSavedData {

    public static final int MIN_FREQUENCY = 20 * 40;
    private static final String UUID = Divide.MODID + ":delivery";

    private int frequency = MIN_FREQUENCY;
    private long next = 0;

    private static final int BAR_THRESHOLD = 20 * 10;
    private static final int MESSAGE_THRESHOLD = BAR_THRESHOLD;

    public DeliverySavedData() {
        super(UUID);
    }

    public static CustomServerBossInfo getBar(ServerWorld world) {
        CustomServerBossInfo bar = world.getServer().getCustomBossEvents().get(new ResourceLocation(UUID));
        if(bar == null) return createBar(world);
        return bar;
    }

    private static CustomServerBossInfo createBar(ServerWorld world) {
        CustomServerBossInfo bar = world.getServer().getCustomBossEvents().add(new ResourceLocation(UUID), new TranslationTextComponent("bossbar.divide.delivery"));
        bar.setColor(BossInfo.Color.BLUE);
        bar.setMax(BAR_THRESHOLD);
        bar.setValue(0);
        bar.setVisible(false);
        bar.setOverlay(BossInfo.Overlay.PROGRESS);
        bar.setPlayers(world.getPlayers());
        return bar;
    }

    @SubscribeEvent
    public static void onDelivery(DeliveryEvent.PerTeam event) {
        Collection<ChunkPos> chunks = findDeliveryChunk(event.world, event.team).collect(Collectors.toList());
        Random random = new Random();

        int rank = TeamSavedData.getRank(event.world, event.team);

        event.world.loadedTileEntityList.stream()
                .filter(LockableLootTileEntity.class::isInstance)
                .map(tile -> (LockableLootTileEntity) tile)
                .filter(t -> chunks.contains(new ChunkPos(t.getPos())))
                .limit(rank + 1)
                .sorted((a, b) -> {
                    int ae = a.isEmpty() ? 1 : -1;
                    int be = b.isEmpty() ? 1 : -1;
                    return ae - be;
                }).forEach(tile -> {
                    BlockPos p = tile.getPos();
                    tile.clear();
                    tile.setLootTable(LootTables.CHESTS_END_CITY_TREASURE, random.nextLong());
                    event.world.spawnParticle(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY(), p.getZ(), 10, 1, 1, 1, 0);
                });

    }

    @SubscribeEvent
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getPlayer().world instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getPlayer().world;
            getBar(world).addPlayer(event.getPlayer().getUniqueID());
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.world instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.world;

            long diff = getNext(world) - world.getGameTime();
            if(diff <= 0) deliver(world);
            else if (diff == MESSAGE_THRESHOLD) messageAll(world, new TranslationTextComponent("message.divide.delivery_notify", MESSAGE_THRESHOLD / 20));

            CustomServerBossInfo bar = getBar(world);
            bar.setVisible(diff <= BAR_THRESHOLD);
            if(diff <= BAR_THRESHOLD) bar.setValue((int) diff);
            else bar.setValue(BAR_THRESHOLD);
        }
    }

    public static Stream<ChunkPos> findDeliveryChunk(ServerWorld world, Team team) {
        return ChunkSavedData.getChunks(world)
                .filter(e -> e.getValue().type == ChunkProtection.Type.delivery)
                .filter(e -> e.getValue().team.equals(team.getName()))
                .map(Map.Entry::getKey);
    }

    public static void setFrequency(ServerWorld world, int frequency) {
        DeliverySavedData data = getInstance(world);
        data.frequency = Math.max(MIN_FREQUENCY, frequency);
        data.markDirty();
    }

    public static long getNext(ServerWorld world) {
        DeliverySavedData data = getInstance(world);
        if(data.next == 0) {
            data.next = world.getGameTime() + data.frequency;
            data.markDirty();
        }
        return data.next;
    }

    public static void messageAll(ServerWorld world, ITextComponent text) {
        world.getPlayers().forEach(p -> p.sendStatusMessage(text, true));
    }

    public static void deliver(ServerWorld world) {
        DeliverySavedData data = getInstance(world);
        data.next = world.getGameTime() + data.frequency;
        messageAll(world, new TranslationTextComponent("message.divide.delivery"));
        world.getScoreboard().getTeams().forEach(team -> MinecraftForge.EVENT_BUS.post(new DeliveryEvent.PerTeam(world, team)));
        data.markDirty();
    }

    private static DeliverySavedData getInstance(ServerWorld world) {
        return world.getSavedData().getOrCreate(DeliverySavedData::new, UUID);
    }

    @Override
    public void read(CompoundNBT nbt) {
        if (nbt.contains("frequency")) this.frequency = nbt.getInt("frequency");
        if (nbt.contains("next")) this.next = nbt.getLong("next");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("next", next);
        compound.putInt("frequency", frequency);
        return compound;
    }
}
