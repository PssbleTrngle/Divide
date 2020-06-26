package com.possible_triangle.divide.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkProtection {

    @SubscribeEvent
    public static void explosion(ExplosionEvent event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            boolean isProtected = event.getExplosion().getAffectedBlockPositions().stream()
                    .map(ChunkPos::new).distinct()
                    .map(pos -> ChunkSavedData.getProtection(world, pos))
                    .map(p -> p.filter(protection -> protection.isInTeam(event.getExplosion().getExplosivePlacedBy())))
                    .anyMatch(Optional::isPresent);

            if (isProtected) event.getExplosion().clearAffectedBlockPositions();
        }
    }

    @SubscribeEvent
    public static void chunkChanged(EntityEvent.EnteringChunk event) {
        if (event.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            ServerWorld world = player.getServerWorld();
            ChunkPos pos = new ChunkPos(event.getNewChunkX(), event.getNewChunkZ());

            Optional<ChunkProtection> protection = ChunkSavedData.getProtection(world, pos);

            Optional<String> last = player.getDataManager().get(PlayerData.IN_CHUNK_BY);
            if (protection.isPresent()) {
                ChunkProtection p = protection.get();
                if (!last.filter(t -> t.equals(p.team)).isPresent()) {
                    boolean hisTeam = p.isInTeam(player);
                    String msg = "message.divide.protection.entered_" + (hisTeam ? "own" : "other");
                    player.sendStatusMessage(new TranslationTextComponent(msg, p.team), true);
                    player.getDataManager().set(PlayerData.IN_CHUNK_BY, Optional.of(p.team));
                }
            } else if (last.isPresent()) {
                boolean hisTeam = player.getTeam() != null && player.getTeam().getName().equals(last.get());
                String msg = "message.divide.protection.left_" + (hisTeam ? "own" : "other");
                player.sendStatusMessage(new TranslationTextComponent(msg, last.get()), true);
                player.getDataManager().set(PlayerData.IN_CHUNK_BY, Optional.empty());
            }

            boolean adventure = protection.filter(p -> p.onlyAdventureMode(player)).isPresent();

            switch (player.interactionManager.getGameType()) {
                case ADVENTURE:
                    if (!adventure) player.setGameType(GameType.SURVIVAL);
                    break;
                case SURVIVAL:
                    if (adventure) player.setGameType(GameType.ADVENTURE);
                    break;
            }
        }
    }

    public final Type type;
    public final String team;

    public ChunkProtection(Type type, String team) {
        this.type = type;
        this.team = team;
    }

    public enum Type {
        flag, delivery, claimed
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("team", team);
        nbt.putString("type", type.name());
        return nbt;
    }

    public static ChunkProtection deserializeNBT(CompoundNBT nbt) {
        Type type = Type.valueOf(nbt.getString("type"));
        String team = nbt.getString("team");
        return new ChunkProtection(type, team);
    }

    public boolean onlyAdventureMode(ServerPlayerEntity player) {
        return !isInTeam(player);
    }

    public boolean isInTeam(Entity entity) {
        return entity != null && entity.getTeam() != null && entity.getTeam().getName().equals(this.team);
    }

}
