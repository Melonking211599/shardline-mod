package eu.shardline.mod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.config.ShardlineClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Eigener Portaleffekt: Waehrend der Spieler im Splitterportal steht, legt sich ein pulsierender
 * Kristallschleier ueber den Bildschirm (textures/misc/shard_portal_overlay.png).
 */
public final class PortalOverlay {
    private static final ResourceLocation TEXTURE = Shardline.id("textures/misc/shard_portal_overlay.png");
    private static float intensity;
    private static float previousIntensity;

    private PortalOverlay() {
    }

    /** Wird jeden Client-Tick aufgerufen. */
    public static void tick(Minecraft minecraft) {
        previousIntensity = intensity;
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            intensity = 0.0F;
            return;
        }
        boolean inside = BlockPos.betweenClosedStream(player.getBoundingBox().deflate(0.01))
                .anyMatch(pos -> minecraft.level.getBlockState(pos).is(ModBlocks.SHARD_PORTAL.get()));
        intensity = Mth.clamp(intensity + (inside ? 0.0125F : -0.05F), 0.0F, 1.0F);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ShardlineClientConfig.PORTAL_OVERLAY.get()) {
            return;
        }
        float value = Mth.lerp(deltaTracker.getGameTimeDeltaPartialTick(false), previousIntensity, intensity);
        if (value <= 0.0F) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float time = (minecraft.level != null ? minecraft.level.getGameTime() : 0) + deltaTracker.getGameTimeDeltaPartialTick(false);
        float pulse = 0.85F + Mth.sin(time * 0.2F) * 0.15F;
        float alpha = Mth.clamp(value * 0.8F * pulse, 0.0F, 1.0F);

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.setColor(0.85F, 0.7F, 1.0F, alpha);
        graphics.blit(TEXTURE, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
