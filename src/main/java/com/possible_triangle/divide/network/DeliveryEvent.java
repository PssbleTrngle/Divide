package com.possible_triangle.divide.network;

import com.possible_triangle.divide.data.DeliverySavedData;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class DeliveryEvent {

    @Cancelable
    public static class PerTeam extends Event {

        public final ServerWorld world;
        public final Team team;

        public PerTeam(ServerWorld world, Team team) {
            this.world = world;
            this.team = team;
        }

    }

}
