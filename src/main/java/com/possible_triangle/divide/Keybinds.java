package com.possible_triangle.divide;

import com.possible_triangle.divide.screen.OverviewScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import javax.swing.text.JTextComponent;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class Keybinds {

    public static final KeyBinding GUI = new KeyBinding(
            "key.divide.overview.description",
            KeyConflictContext.IN_GAME,
            InputMappings.getInputByCode(GLFW.GLFW_KEY_O, 0),
            "key.divide.category"
    );

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if(GUI.isPressed() && mc.world != null) {
            mc.displayGuiScreen(new OverviewScreen());
        }
    }

    public static void register() {
        Stream.of(GUI).forEach(ClientRegistry::registerKeyBinding);
    }

}
