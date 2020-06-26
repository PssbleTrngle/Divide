package com.possible_triangle.divide;

import com.possible_triangle.divide.block.tile.FlagTile;
import com.possible_triangle.divide.block.tile.render.FlagRenderer;
import com.possible_triangle.divide.command.DivideCommand;
import com.possible_triangle.divide.network.DivideNetworking;
import net.java.games.input.Keyboard;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Divide.MODID)
public class Divide {

    public static final String MODID = "divide";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public Divide() {
        Content.init();
        DivideNetworking.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        Content.FLAG_TILE_TYPE.ifPresent(type -> ClientRegistry.bindTileEntityRenderer(type, FlagRenderer::new));

        Keybinds.register();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        DivideCommand.register(event.getCommandDispatcher());
    }
}
