package eu.shardline.mod.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import eu.shardline.mod.Shardline;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Himmel der Splitterweite: ein violetter Sternennebel-Wuerfel (wie der End-Himmel, aber mit eigener
 * Textur und langsamer Drehung) und ein zerbrochener Kristallmond.
 * Texturen: textures/environment/splitterweite_sky.png und splitterweite_moon.png.
 */
public class SplitterweiteEffects extends DimensionSpecialEffects {
    private static final ResourceLocation SKY_TEXTURE = Shardline.id("textures/environment/splitterweite_sky.png");
    private static final ResourceLocation MOON_TEXTURE = Shardline.id("textures/environment/splitterweite_moon.png");
    private static final Vec3 FOG_COLOR = new Vec3(0.16, 0.09, 0.26);

    public SplitterweiteEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return FOG_COLOR;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix,
                             boolean isFoggy, Runnable setupFog) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        float rotation = (ticks + partialTick) * 0.01F;

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, SKY_TEXTURE);
        Tesselator tesselator = Tesselator.getInstance();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            switch (face) {
                case 1 -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                case 2 -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                case 3 -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                case 4 -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                case 5 -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
                default -> {
                }
            }
            Matrix4f matrix = poseStack.last().pose();
            int color = face == 0 ? 0xFF140A22 : 0xFF2A1748;
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex(matrix, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(color);
            buffer.addVertex(matrix, -100.0F, -100.0F, 100.0F).setUv(0.0F, 4.0F).setColor(color);
            buffer.addVertex(matrix, 100.0F, -100.0F, 100.0F).setUv(4.0F, 4.0F).setColor(color);
            buffer.addVertex(matrix, 100.0F, -100.0F, -100.0F).setUv(4.0F, 0.0F).setColor(color);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }
        poseStack.popPose();

        // Zerbrochener Kristallmond
        RenderSystem.blendFuncSeparate(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderTexture(0, MOON_TEXTURE);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-30.0F + rotation * 0.5F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F));
        Matrix4f moon = poseStack.last().pose();
        float size = 28.0F;
        BufferBuilder moonBuffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        moonBuffer.addVertex(moon, -size, 100.0F, -size).setUv(0.0F, 0.0F).setColor(0xFFE8D0FF);
        moonBuffer.addVertex(moon, size, 100.0F, -size).setUv(1.0F, 0.0F).setColor(0xFFE8D0FF);
        moonBuffer.addVertex(moon, size, 100.0F, size).setUv(1.0F, 1.0F).setColor(0xFFE8D0FF);
        moonBuffer.addVertex(moon, -size, 100.0F, size).setUv(0.0F, 1.0F).setColor(0xFFE8D0FF);
        BufferUploader.drawWithShader(moonBuffer.buildOrThrow());
        poseStack.popPose();

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        return true;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ,
                                Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        return true;
    }
}
