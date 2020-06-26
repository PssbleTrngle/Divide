package com.possible_triangle.divide.block.tile.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.possible_triangle.divide.block.Flag;
import com.possible_triangle.divide.block.tile.FlagTile;
import com.possible_triangle.divide.helper.Roman;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlagRenderer extends TileEntityRenderer<FlagTile> {
    private final ModelRenderer renderer_a = BannerTileEntityRenderer.func_228836_a_();
    private final ModelRenderer renderer_b = new ModelRenderer(64, 64, 44, 0);
    private final ModelRenderer renderer_c;

    public static final Color INVALID = new Color(109, 109, 109);

    public FlagRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        this.renderer_b.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
        this.renderer_c = new ModelRenderer(64, 64, 0, 42);
        this.renderer_c.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
    }

    public static float[] getColor(FlagTile tile) {

        //float hue = (int) ( System.currentTimeMillis() /5L) % 360 / 360F;
        //Color color = Color.getHSBColor(hue, 1F, 1F);

        Color color = tile.getColor().map(TextFormatting::getColor).map(Color::new).orElse(INVALID);
        float r = color.getRed() / 255F;
        float g = color.getGreen() / 255F;
        float b = color.getBlue() / 255F;

        if (tile.getBlockState().get(Flag.MISSING)) return new float[]{r / 2, g / 2, b / 2};
        return new float[]{r, g, b};
    }

    @Override
    public void render(FlagTile tile, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {

        matrix.push();
        this.renderer_b.showModel = true;

        if (tile.getWorld() == null) {
            matrix.translate(0.5D, 0.5D, 0.5D);
            matrix.push();
        } else {
            BlockState blockstate = tile.getBlockState();

            matrix.translate(0.5D, 0.5D, 0.5D);
            float deg = (float) (-blockstate.get(Flag.ROTATION) * 360) / 16.0F;
            matrix.push();
            matrix.rotate(Vector3f.YP.rotationDegrees(deg));
        }


        renderBanner(tile, matrix, buffer, light, overlay, partialTicks);
        matrix.pop();

        //renderText(tile, matrix, buffer, light, overlay, partialTicks);
        matrix.pop();
    }

    private void renderBanner(FlagTile tile, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay, float partialTicks) {
        float[] color = getColor(tile);

        long time;
        if (tile.getWorld() == null) {
            time = 0L;
        } else {
            time = tile.getWorld().getGameTime();
        }

        matrix.push();
        matrix.scale(0.6666667F, -0.6666667F, -0.6666667F);
        IVertexBuilder vertexBuilder = ModelBakery.LOCATION_BANNER_BASE.getBuffer(buffer, RenderType::getEntitySolid);
        this.renderer_b.render(matrix, vertexBuilder, light, overlay);
        this.renderer_c.render(matrix, vertexBuilder, light, overlay);
        BlockPos blockpos = tile.getPos();
        float f2 = ((float) Math.floorMod((long) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + time, 100L) + partialTicks) / 100.0F;
        this.renderer_a.rotateAngleX = (-0.0125F + 0.01F * MathHelper.cos(((float) Math.PI * 2F) * f2)) * (float) Math.PI;
        this.renderer_a.rotationPointY = -32.0F;

        Material material = new Material(Atlases.BANNER_ATLAS, BannerPattern.BASE.func_226957_a_(true));
        renderer_a.render(matrix, material.getBuffer(buffer, RenderType::getEntitySolid), light, overlay, color[0], color[1], color[2], 1.0F);

        String rank = Roman.toRoman(tile.getRank());
        FontRenderer font = Minecraft.getInstance().fontRenderer;

        matrix.pop();
        matrix.push();

        matrix.scale(-0.04F, -0.04F, 0.04F);
        matrix.translate(0, -15, 3 - this.renderer_a.rotateAngleX * 10);
        matrix.rotate(new Quaternion(new Vector3f(0, 1, 0), 180, true));
        matrix.rotate(new Quaternion(new Vector3f(1, 0, 0), this.renderer_a.rotateAngleX, false));

        font.renderString(rank, -font.getStringWidth(rank) / 2F, 0,0xFFFFFF, false, matrix.getLast().getMatrix(), buffer, false, 0, light);

        matrix.pop();
    }

    public static final ITextComponent NO_TEAM = new TranslationTextComponent("message.divide.no_team");

    public static Stream<ITextComponent> getTextFor(FlagTile tile, PlayerEntity player) {
        return Stream.of(
                tile.getTeam(tile.getWorld()).map(ScorePlayerTeam::getDisplayName).orElse(NO_TEAM),
                new StringTextComponent("Rank " + tile.getRank())
        );
    }

    private void renderText(FlagTile tile, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3d vec = new Vec3d(tile.getPos()).add(0.5, 0.5, 0.5).subtract(mc.player.getPositionVec());
        if (vec.lengthSquared() > 16) return;

        matrix.push();
        float angle = (float) MathHelper.atan2(vec.x, vec.z);
        matrix.rotate(new Quaternion(new Vector3f(0, 1F, 0), angle, false));

        matrix.scale(-0.025F, -0.025F, 0.025F);
        matrix.translate(0, 0, -40);

        Set<String> text = getTextFor(tile, mc.player).map(ITextComponent::getFormattedText).collect(Collectors.toSet());

        int i = 0;
        for (String line : text) {
            mc.fontRenderer.renderString(line, -mc.fontRenderer.getStringWidth(line) / 2F, i * 10 - (25 + 5 * text.size()), 0, false, matrix.getLast().getMatrix(), buffer, false, 0, light);
            i++;
        }

        matrix.pop();
    }

}
