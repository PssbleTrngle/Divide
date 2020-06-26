package com.possible_triangle.divide.network;

import com.google.common.collect.Queues;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Overview {

    @Nullable
    private static Overview CACHED = null;
    private static final Queue<CompletableFuture<Overview>> QUEUE = Queues.newArrayDeque();

    public static CompletableFuture<Overview> getCached() {
        CompletableFuture<Overview> future = new CompletableFuture<>();
        //if(CACHED != null) future.complete(CACHED);
        QUEUE.add(future);
        DivideNetworking.requestInfo();
        return future;
    }

    public final long nextDelivery;
    public final CachedTeam[] teams;

    public Overview(CachedTeam[] teams, long nextDelivery) {
        this.teams = teams;
        this.nextDelivery = nextDelivery;
    }

    public static Overview read(PacketBuffer buffer) {
        int length = buffer.readInt();
        CachedTeam[] teams = IntStream.range(0, length).mapToObj($ -> CachedTeam.read(buffer)).toArray(CachedTeam[]::new);
        return new Overview(teams, buffer.readLong());
    }

    public void write(PacketBuffer buffer) {
        buffer.writeInt(teams.length);
        for(CachedTeam team : teams) team.write(buffer);
        buffer.writeLong(this.nextDelivery);
    }

    public static void handle(Overview msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            CACHED = msg;
            CompletableFuture<Overview> future;
            while((future = QUEUE.poll()) != null) future.complete(msg);
        });
        context.get().setPacketHandled(true);
    }
}
