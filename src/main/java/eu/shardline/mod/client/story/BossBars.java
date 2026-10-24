package eu.shardline.mod.client.story;

import eu.shardline.mod.Shardline;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

/**
 * Eigene Bossleisten fuer die drei Shardline-Bosse (Kristallkoloss, Splitterkoenigin, Virusherz). Jeder Boss hat einen
 * eigenen Rahmen mit Verzierungen, eine leuchtende, schimmernde Fuellung, Marken fuer die Phasenwechsel und einen
 * Farbton passend zur aktuellen Phase. Alle anderen Bossleisten (Vanilla, andere Mods) bleiben unveraendert.
 * Texturen: textures/gui/bossbar/&lt;boss&gt;.png (Atlas 256x64, erzeugt von tools/assetgen/bossbars.py).
 */
@EventBusSubscriber(modid = Shardline.MODID, value = Dist.CLIENT)
public final class BossBars {
    private static final int ATLAS_W = 256;
    private static final int ATLAS_H = 64;
    private static final int BAR_W = 200;
    private static final int BAR_H = 12;
    private static final int FRAME_W = 232;
    private static final int FRAME_H = 28;
    /** Abstand zur naechsten Leiste (Name + Rahmen). */
    private static final int INCREMENT = 44;

    private record Style(ResourceLocation texture, int nameColor, float[] phases, boolean purplePhase) {
    }

    private static final Style COLOSSUS = new Style(texture("crystal_colossus"), 0x84EFFF, new float[] {0.5F}, false);
    /** Schluessel = Uebersetzungsschluessel des Bossleisten-Namens (der Koloss wechselt in Phase 2 auf "enraged"). */
    private static final Map<String, Style> STYLES = Map.of(
            "entity.shardline.crystal_colossus", COLOSSUS,
            "entity.shardline.crystal_colossus.enraged", COLOSSUS,
            "entity.shardline.shard_queen", new Style(texture("shard_queen"), 0xFF8FE8, new float[] {2.0F / 3.0F, 1.0F / 3.0F}, true),
            "entity.shardline.virus_heart", new Style(texture("virus_heart"), 0xFF6FDC, new float[] {2.0F / 3.0F, 1.0F / 3.0F}, false));

    private BossBars() {
    }

    private static ResourceLocation texture(String boss) {
        return Shardline.id("textures/gui/bossbar/" + boss + ".png");
    }

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();
        if (!(boss.getName().getContents() instanceof TranslatableContents contents)) {
            return;
        }
        Style style = STYLES.get(contents.getKey());
        if (style == null) {
            return;
        }
        event.setCanceled(true);
        event.setIncrement(INCREMENT);
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        int centerX = graphics.guiWidth() / 2;
        int top = event.getY();

        // Name mit Schatten ueber der Leiste
        Component name = boss.getName();
        graphics.drawString(font, name, centerX - font.width(name) / 2, top, style.nameColor(), true);

        int frameX = centerX - FRAME_W / 2;
        int frameY = top + font.lineHeight + 3;
        int barX = frameX + 16;
        int barY = frameY + 8;
        float progress = Mth.clamp(boss.getProgress(), 0.0F, 1.0F);
        float[] tint = tint(boss.getColor(), style.purplePhase());

        // Hintergrund der Leiste
        graphics.blit(style.texture(), barX, barY, 0.0F, 0.0F, BAR_W, BAR_H, ATLAS_W, ATLAS_H);

        // Fuellung: schraege Streifen wandern (Periode 16 Pixel), bei wenig Leben pulsiert die Helligkeit
        int fillW = Math.round(BAR_W * progress);
        if (fillW > 0) {
            float pulse = progress < 0.25F ? 0.78F + 0.22F * Mth.sin(Util.getMillis() / 120.0F) : 1.0F;
            int shift = (int) ((Util.getMillis() / 55L) % 16L);
            graphics.setColor(tint[0] * pulse, tint[1] * pulse, tint[2] * pulse, 1.0F);
            graphics.blit(style.texture(), barX, barY, (float) shift, 12.0F, fillW, BAR_H, ATLAS_W, ATLAS_H);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        // Phasenmarken: erreichte Phasen sind abgedunkelt, kommende leuchten weiss
        for (float phase : style.phases()) {
            int x = barX + Math.round(BAR_W * phase);
            boolean passed = progress <= phase;
            graphics.fill(x, barY, x + 1, barY + BAR_H, passed ? 0x99000000 : 0xCCFFFFFF);
        }

        // Rahmen mit Verzierungen
        graphics.setColor(tint[0], tint[1], tint[2], 1.0F);
        graphics.blit(style.texture(), frameX, frameY, 0.0F, 24.0F, FRAME_W, FRAME_H, ATLAS_W, ATLAS_H);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Farbton je nach Phase: Rot = Raserei, Pink = zweite Phase des Virusherzens; bei der Koenigin ist Violett die zweite Phase. */
    private static float[] tint(BossEvent.BossBarColor color, boolean purplePhase) {
        if (color == BossEvent.BossBarColor.RED) {
            return new float[] {1.0F, 0.55F, 0.55F};
        }
        if (color == BossEvent.BossBarColor.PINK) {
            return new float[] {1.0F, 0.8F, 0.95F};
        }
        if (color == BossEvent.BossBarColor.PURPLE && purplePhase) {
            return new float[] {0.85F, 0.75F, 1.0F};
        }
        return new float[] {1.0F, 1.0F, 1.0F};
    }
}
