package com.possible_triangle.divide.network;

import com.possible_triangle.divide.data.DeliverySavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class CacheRequest {

    public static CacheRequest read(PacketBuffer buffer) {
        return new CacheRequest();
    }

    public void write(PacketBuffer buffer) {

    }

    public static Overview create(ServerWorld world, @Nullable ServerPlayerEntity player) {

        CachedTeam[] teams = world.getScoreboard().getTeams().stream()
                .map(team -> new CachedTeam(team, world))
                .toArray(CachedTeam[]::new);

        long nextDelivery = DeliverySavedData.getNext(world);

        return new Overview(teams, nextDelivery);
    }

    public static void handle(CacheRequest msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player != null)
                DivideNetworking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), create(player.getServerWorld(), player));
        });
        context.get().setPacketHandled(true);
    }

}
