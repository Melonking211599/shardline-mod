package eu.shardline.mod.client.story;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.virus.InfectionMap;
import eu.shardline.mod.world.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;

/**
 * Atmosphaere infizierter Gebiete (nur Client): der Client schaetzt anhand einer Stichprobe
 * benachbarter Bloecke, wie stark die Umgebung befallen ist, und blendet danach violetten
 * Nebel und eigene Musik ein. Solange der Virus frei ist, faerbt sich zudem der Himmel
 * aller Dimensionen mit Tageslicht violett.
 */
@EventBusSubscriber(modid = Shardline.MODID, value = Dist.CLIENT)
public final class InfectionAtmosphere {
    private static final int SAMPLE_INTERVAL = 10;
    private static final int SAMPLE_RADIUS = 6;
    private static final int SAMPLE_STEP = 3;
    /** Ab so vielen infizierten Treffern in der Stichprobe gilt die Umgebung als voll befallen. */
    private static final float FULL_INFECTION_HITS = 14.0F;

    private static float intensity;
    private static float target;
    private static Music infectedMusic;

    private InfectionAtmosphere() {
    }

    /** 0 = sauber, 1 = stark befallen. */
    public static float intensity() {
        return intensity;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            intensity = 0.0F;
            target = 0.0F;
            return;
        }
        if (player.tickCount % SAMPLE_INTERVAL == 0) {
            target = sample(level, player.blockPosition());
        }
        intensity += (target - intensity) * 0.05F;
    }

    private static float sample(ClientLevel level, BlockPos center) {
        int hits = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -SAMPLE_RADIUS; dx <= SAMPLE_RADIUS; dx += SAMPLE_STEP) {
            for (int dz = -SAMPLE_RADIUS; dz <= SAMPLE_RADIUS; dz += SAMPLE_STEP) {
                for (int dy = -3; dy <= 3; dy += SAMPLE_STEP) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (InfectionMap.isInfected(level.getBlockState(pos))) {
                        hits++;
                    }
                }
            }
        }
        return Mth.clamp(hits / FULL_INFECTION_HITS, 0.0F, 1.0F);
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        StoryClientState.reset();
        intensity = 0.0F;
        target = 0.0F;
    }

    // ------------------------------------------------------------------ Nebel

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!ShardlineClientConfig.INFECTION_FOG.get() || intensity < 0.02F || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }
        float far = Math.min(event.getFarPlaneDistance(), Mth.lerp(intensity, event.getFarPlaneDistance(), 28.0F));
        event.setNearPlaneDistance(far * 0.05F);
        event.setFarPlaneDistance(far);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float blend = 0.0F;
        if (ShardlineClientConfig.INFECTION_FOG.get()) {
            blend = intensity * 0.75F;
        }
        if (skyTinted()) {
            blend = Math.max(blend, 0.3F);
        }
        if (blend <= 0.0F) {
            return;
        }
        event.setRed(Mth.lerp(blend, event.getRed(), 0.36F));
        event.setGreen(Mth.lerp(blend, event.getGreen(), 0.1F));
        event.setBlue(Mth.lerp(blend, event.getBlue(), 0.48F));
    }

    // ------------------------------------------------------------------ Musik

    @SubscribeEvent
    static void onSelectMusic(SelectMusicEvent event) {
        if (!ShardlineClientConfig.INFECTION_MUSIC.get() || intensity < 0.35F) {
            return;
        }
        if (infectedMusic == null) {
            infectedMusic = new Music(ModSounds.MUSIC_INFECTED, 600, 2400, true);
        }
        event.setMusic(infectedMusic);
    }

    // ------------------------------------------------------------------ Himmel

    private static boolean skyTinted() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        return ShardlineClientConfig.INFECTED_SKY.get() && level != null && StoryClientState.virusActive()
                && level.dimension() != ModDimensions.SPLITTERWEITE_LEVEL && level.dimensionType().hasSkyLight();
    }

    @SubscribeEvent
    static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY || !skyTinted()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float time = minecraft.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float alpha = StoryClientState.ending() == StoryEnding.DOMINATED ? 0.5F : 0.38F;
        alpha += Mth.sin(time * 0.01F) * 0.05F;
        if (StoryScreenEffects.isOutbreakRunning()) {
            alpha = Math.min(0.85F, alpha + 0.3F);
        }
        int color = ((int) (alpha * 255) << 24) | 0x5A1A8C;
        int horizon = ((int) (alpha * 200) << 24) | 0x8A2BB8;

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getModelViewMatrix());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
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
            int faceColor = face == 0 || face == 3 ? color : horizon;
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex(matrix, -60.0F, -60.0F, -60.0F).setColor(faceColor);
            buffer.addVertex(matrix, -60.0F, -60.0F, 60.0F).setColor(faceColor);
            buffer.addVertex(matrix, 60.0F, -60.0F, 60.0F).setColor(faceColor);
            buffer.addVertex(matrix, 60.0F, -60.0F, -60.0F).setColor(faceColor);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
