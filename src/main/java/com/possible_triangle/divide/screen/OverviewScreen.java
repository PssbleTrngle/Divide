package com.possible_triangle.divide.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.possible_triangle.divide.Divide;
import com.possible_triangle.divide.helper.Roman;
import com.possible_triangle.divide.network.CachedFlag;
import com.possible_triangle.divide.network.CachedTeam;
import com.possible_triangle.divide.network.Overview;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class OverviewScreen extends Screen {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Divide.MODID, "textures/gui/overview.png");

    private static final ITextComponent TITLE = new TranslationTextComponent("screen.divide.title");
    private Overview overview;

    public OverviewScreen() {
        super(TITLE);
        Overview.getCached().thenAccept(o -> this.overview = o);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if(this.minecraft == null || this.minecraft.world == null) return;
        super.render(mouseX, mouseY, partialTicks);
        FontRenderer font = getMinecraft().fontRenderer;
        renderBackground();

        int xSize = 196;
        int ySize = 166;
        int guiLeft = (this.width - xSize) / 2;
        int guiTop = (this.height - ySize) / 2;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BACKGROUND);
        this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (this.overview != null) {

            long next = (this.overview.nextDelivery - minecraft.world.getGameTime()) / 20;
            String message = new TranslationTextComponent("message.divide.delivery_notify", next).getFormattedText();
            drawString(font, message, guiLeft + (xSize - font.getStringWidth(message)) / 2, guiTop + 12, 0xFFFFFF);
            this.minecraft.getTextureManager().bindTexture(BACKGROUND);
            this.blit(guiLeft + 5, guiTop + 5, 0, ySize + 24, xSize - 10, 24);

            for (int i = 0; i < overview.teams.length; i++) {
                CachedTeam team = overview.teams[i];
                int top = guiTop + 5 + 28 * i + 30;
                Color color = new Color(team.color);
                int rankLeft = guiLeft + xSize - 28;

                this.minecraft.getTextureManager().bindTexture(BACKGROUND);
                this.blit(guiLeft + 5, top, 0, ySize, xSize - 10, 24);
                this.blit(rankLeft, top + 2, xSize + 40, 0, 20, 20);

                for (int j = 0; j < team.flags.length; j++) {
                    this.minecraft.getTextureManager().bindTexture(BACKGROUND);

                    this.blit(guiLeft + 40 + 16 * j, top + 2, xSize, 0, 20, 20);
                    RenderSystem.color4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
                    this.blit(guiLeft + 40 + 16 * j, top + 2, xSize + 20, 0, 20, 20);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                    String rank = Roman.toRoman(team.flags[j].rank);
                    drawString(font, rank, guiLeft + 50 + 16 * j - font.getStringWidth(rank) / 2, top + 5, 0xFFFFFF);
                }

                drawString(font, team.display.getFormattedText(), guiLeft + 10, top + 7, color.getRGB());

                String rank = Roman.toRoman(team.rank);
                drawString(font, rank, rankLeft + 10 - font.getStringWidth(rank) / 2, top + 8, 0xFFFFFF);
            }

        } else {
            drawString(font, "Loading", 10, 10, 10);
        }

    }
}
