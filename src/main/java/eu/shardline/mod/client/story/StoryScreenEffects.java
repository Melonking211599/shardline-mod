package eu.shardline.mod.client.story;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.effect.ModEffects;
import eu.shardline.mod.network.ScreenEffectPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Bildschirmeffekte der Story: Blitz und Risse beim Ausbruch, goldenes bzw. dunkles Leuchten
 * bei der Endentscheidung, Visionsschleier, Kamerawackeln und die verzerrte Sicht des
 * Splitterfiebers.
 */
@EventBusSubscriber(modid = Shardline.MODID, value = Dist.CLIENT)
public final class StoryScreenEffects {
    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    private static final ResourceLocation BREACH_OVERLAY = Shardline.id("textures/misc/breach_overlay.png");

    private static int effect = -1;
    private static int duration;
    private static int remaining;
    private static float fever;
    private static float previousFever;

    private StoryScreenEffects() {
    }

    public static void start(int newEffect, int newDuration) {
        effect = newEffect;
        duration = Math.max(1, newDuration);
        remaining = duration;
    }

    /** 1 direkt nach dem Start, 0 am Ende. */
    private static float progress(float partialTick) {
        if (effect < 0 || remaining <= 0) {
            return 0.0F;
        }
        return Mth.clamp((remaining - partialTick) / duration, 0.0F, 1.0F);
    }

    public static boolean isOutbreakRunning() {
        return effect == ScreenEffectPayload.OUTBREAK && remaining > 0;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (remaining > 0) {
            remaining--;
        }
        previousFever = fever;
        LocalPlayer player = Minecraft.getInstance().player;
        boolean feverish = player != null && player.hasEffect(ModEffects.SHARD_FEVER);
        fever = Mth.clamp(fever + (feverish ? 0.02F : -0.04F), 0.0F, 1.0F);
    }

    private static float feverAmount(float partialTick) {
        return ShardlineClientConfig.FEVER_DISTORTION.get() ? Mth.lerp(partialTick, previousFever, fever) : 0.0F;
    }

    // ------------------------------------------------------------------ Overlay (GUI-Ebene)

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float time = minecraft.level.getGameTime() + partialTick;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        float feverValue = feverAmount(partialTick);
        if (feverValue > 0.0F) {
            float pulse = 0.6F + Mth.sin(time * 0.15F) * 0.25F;
            drawVignette(graphics, 0.55F, 0.15F, 0.75F, feverValue * pulse, width, height);
        }

        float value = progress(partialTick);
        if (value <= 0.0F) {
            return;
        }
        switch (effect) {
            case ScreenEffectPayload.OUTBREAK -> {
                float flash = value > 0.85F ? (value - 0.85F) / 0.15F : 0.0F;
                fill(graphics, 1.0F, 0.85F, 1.0F, flash * 0.9F, width, height);
                fill(graphics, 0.45F, 0.05F, 0.6F, value * 0.45F, width, height);
                drawOverlay(graphics, BREACH_OVERLAY, value * 0.85F, width, height);
                drawVignette(graphics, 0.7F, 0.1F, 0.9F, value, width, height);
            }
            case ScreenEffectPayload.SEALED -> {
                fill(graphics, 1.0F, 0.9F, 0.55F, value * value * 0.6F, width, height);
                drawVignette(graphics, 1.0F, 0.8F, 0.3F, value, width, height);
            }
            case ScreenEffectPayload.DOMINATED -> {
                fill(graphics, 0.15F, 0.0F, 0.25F, value * 0.6F, width, height);
                drawVignette(graphics, 0.6F, 0.0F, 0.8F, value, width, height);
            }
            case ScreenEffectPayload.VISION -> {
                float fade = Mth.sin(value * Mth.PI);
                fill(graphics, 0.75F, 0.7F, 1.0F, fade * 0.25F, width, height);
                drawVignette(graphics, 0.5F, 0.6F, 1.0F, fade, width, height);
            }
            case ScreenEffectPayload.PURIFY -> drawVignette(graphics, 0.7F, 1.0F, 0.9F, value, width, height);
            case ScreenEffectPayload.AFTERSHOCK -> drawVignette(graphics, 0.5F, 0.1F, 0.7F, value * 0.6F, width, height);
            default -> {
            }
        }
    }

    private static void fill(GuiGraphics graphics, float r, float g, float b, float alpha, int width, int height) {
        if (alpha <= 0.0F) {
            return;
        }
        int color = ((int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
        graphics.fill(0, 0, width, height, color);
    }

    /** Additiv eingefaerbte Vanilla-Vignette: nur die Raender leuchten. */
    private static void drawVignette(GuiGraphics graphics, float r, float g, float b, float strength, int width, int height) {
        if (strength <= 0.0F) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        graphics.setColor(r * strength, g * strength, b * strength, 1.0F);
        graphics.blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void drawOverlay(GuiGraphics graphics, ResourceLocation texture, float alpha, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F));
        graphics.blit(texture, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    // ------------------------------------------------------------------ Kamera

    @SubscribeEvent
    static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float partialTick = (float) event.getPartialTick();
        Minecraft minecraft = Minecraft.getInstance();
        float time = (minecraft.level != null ? minecraft.level.getGameTime() : 0) + partialTick;

        float feverValue = feverAmount(partialTick);
        if (feverValue > 0.0F) {
            event.setRoll(event.getRoll() + Mth.sin(time * 0.045F) * 4.0F * feverValue);
            event.setYaw(event.getYaw() + Mth.sin(time * 0.031F) * 1.5F * feverValue);
        }

        float value = progress(partialTick);
        if (value <= 0.0F || !ShardlineClientConfig.SCREEN_SHAKE.get()) {
            return;
        }
        float strength = switch (effect) {
            case ScreenEffectPayload.OUTBREAK -> 5.0F;
            case ScreenEffectPayload.DOMINATED -> 3.0F;
            case ScreenEffectPayload.SEALED -> 1.5F;
            case ScreenEffectPayload.AFTERSHOCK -> 2.0F;
            default -> 0.0F;
        };
        if (strength <= 0.0F) {
            return;
        }
        float shake = strength * value * value;
        event.setYaw(event.getYaw() + Mth.sin(time * 2.7F) * shake * 0.6F);
        event.setPitch(event.getPitch() + Mth.sin(time * 3.3F + 1.3F) * shake * 0.5F);
        event.setRoll(event.getRoll() + Mth.sin(time * 1.9F + 2.1F) * shake);
    }

    @SubscribeEvent
    static void onFov(ViewportEvent.ComputeFov event) {
        float partialTick = (float) event.getPartialTick();
        float feverValue = feverAmount(partialTick);
        if (feverValue <= 0.0F || !event.usedConfiguredFov()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float time = (minecraft.level != null ? minecraft.level.getGameTime() : 0) + partialTick;
        event.setFOV(event.getFOV() * (1.0 + Mth.sin(time * 0.08F) * 0.05 * feverValue));
    }
}
