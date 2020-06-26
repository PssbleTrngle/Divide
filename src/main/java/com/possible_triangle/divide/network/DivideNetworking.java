package com.possible_triangle.divide.network;

import com.possible_triangle.divide.Divide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class DivideNetworking {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Divide.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(1, Overview.class, Overview::write, Overview::read, Overview::handle);
        INSTANCE.registerMessage(2, CacheRequest.class, CacheRequest::write, CacheRequest::read, CacheRequest::handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static void requestInfo() {
        INSTANCE.sendToServer(new CacheRequest());
    }

}
