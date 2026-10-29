package eu.shardline.mod.client.story;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import java.util.HashMap;
import java.util.List;
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
 * Eigene Bossleisten fuer die Shardline-Bosse (Kristallkoloss, Splitterkoenigin, Virusherz, Nachhall, Schwaerenherz).
 * Jeder Boss hat ein eigenes Design aus drei Texturen (Hintergrund, Fuellung, Rahmen) mit je einer Zeile pro Phase und
 * ein Zusatzmerkmal: Schild am 50-%-Marker (Koloss), Risse mit sinkendem Leben (Koenigin), Adern im Herzschlag
 * (Virusherz, Schwaerenherz) oder durchscheinende Darstellung (Nachhall). Alle anderen Bossleisten (Vanilla, andere Mods)
 * bleiben unveraendert; in der Client-Konfiguration laesst sich die ganze Darstellung abschalten ({@code customBossBars}).
 * Texturen: textures/gui/boss_bar/&lt;boss&gt;_{background,fill,overlay}.png (Leiste 182 x 10 wie Vanilla-Breite,
 * erzeugt von tools/assetgen/bossbars.py). Neue Designs meldet {@link #register(String, Design)} an.
 */
@EventBusSubscriber(modid = Shardline.MODID, value = Dist.CLIENT)
public final class BossBars {
    private static final int BAR_W = 182;
    private static final int BAR_H = 10;
    /** Die Fuellung ist eine Periode (16 px) breiter, damit das Fenster fuers Schimmern verschoben werden kann. */
    private static final int FILL_W = BAR_W + 16;
    private static final int FRAME_W = 214;
    private static final int FRAME_H = 32;
    /** Lage des Leistenfensters im Rahmen. */
    private static final int WINDOW_X = 16;
    private static final int WINDOW_Y = 12;
    /** Abstand zur naechsten Leiste (Name + Rahmen). */
    private static final int INCREMENT = 48;

    /** Zusatzmerkmal eines Designs. */
    public enum Feature {
        NONE,
        /** Schildsymbol am 50-%-Marker, nach dem Phasenwechsel zerbrochen. */
        SHIELD,
        /** Risse in der Leiste, die mit sinkendem Leben zunehmen. */
        CRACKS,
        /** Adern, die im Takt des Herzschlags aufleuchten. */
        VEINS,
        /** Durchscheinende Leiste mit leichtem Flackern. */
        GHOST
    }

    /**
     * Design einer Bossleiste.
     *
     * @param id         Dateipraefix unter textures/gui/boss_bar/
     * @param nameColor  Farbe des Namens ueber der Leiste
     * @param phases     Vanilla-Farben, die der Server je Phase setzt; die Reihenfolge entspricht den Zeilen von Fuellung und Rahmen
     * @param marks      Lebensanteile, an denen eine Phasenmarke sitzt
     * @param feature    Zusatzmerkmal
     */
    public record Design(String id, int nameColor, List<BossEvent.BossBarColor> phases, float[] marks, Feature feature) {
        private ResourceLocation tex(String part) {
            return Shardline.id("textures/gui/boss_bar/" + this.id + "_" + part + ".png");
        }
    }

    /** Schluessel = Uebersetzungsschluessel des Bossleisten-Namens (der Koloss wechselt in Phase 2 auf "enraged"). */
    private static final Map<String, Design> DESIGNS = new HashMap<>();

    static {
        Design colossus = new Design("crystal_colossus", 0x84EFFF, List.of(BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.RED),
                new float[] {0.5F}, Feature.SHIELD);
        register("entity.shardline.crystal_colossus", colossus);
        register("entity.shardline.crystal_colossus.enraged", colossus);
        register("entity.shardline.shard_queen", new Design("shard_queen", 0xFF8FE8,
                List.of(BossEvent.BossBarColor.PINK, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.RED),
                new float[] {2.0F / 3.0F, 1.0F / 3.0F}, Feature.CRACKS));
        register("entity.shardline.virus_heart", new Design("virus_heart", 0xFF6FDC,
                List.of(BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.PINK, BossEvent.BossBarColor.RED),
                new float[] {2.0F / 3.0F, 1.0F / 3.0F}, Feature.VEINS));
        register("entity.shardline.echo_queen", new Design("echo_queen", 0xB8E6FF, List.of(BossEvent.BossBarColor.BLUE),
                new float[] {2.0F / 3.0F, 1.0F / 3.0F}, Feature.GHOST));
        register("entity.shardline.festering_heart", new Design("festering_heart", 0xD2DC4A, List.of(BossEvent.BossBarColor.YELLOW),
                new float[] {2.0F / 3.0F, 1.0F / 3.0F}, Feature.VEINS));
    }

    private BossBars() {
    }

    /** Meldet ein Design fuer den Uebersetzungsschluessel eines Bossnamens an (ersetzt ein vorhandenes). */
    public static void register(String nameKey, Design design) {
        DESIGNS.put(nameKey, design);
    }

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!ShardlineClientConfig.CUSTOM_BOSS_BARS.get()) {
            return;
        }
        LerpingBossEvent boss = event.getBossEvent();
        if (!(boss.getName().getContents() instanceof TranslatableContents contents)) {
            return;
        }
        Design design = DESIGNS.get(contents.getKey());
        if (design == null) {
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
        graphics.drawString(font, name, centerX - font.width(name) / 2, top, design.nameColor(), true);

        int frameX = centerX - FRAME_W / 2;
        int frameY = top + font.lineHeight + 3;
        int barX = frameX + WINDOW_X;
        int barY = frameY + WINDOW_Y;
        float progress = Mth.clamp(boss.getProgress(), 0.0F, 1.0F);
        int phaseCount = design.phases().size();
        int phase = Math.max(0, design.phases().indexOf(boss.getColor()));
        long now = Util.getMillis();
        boolean ghost = design.feature() == Feature.GHOST;
        float beat = design.feature() == Feature.VEINS ? heartbeat(now) : 0.0F;
        float flicker = ghost ? 0.88F + 0.12F * Mth.sin(now / 420.0F) : 1.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Hintergrund der Leiste
        graphics.setColor(1.0F, 1.0F, 1.0F, flicker);
        graphics.blit(design.tex("background"), barX, barY, 0.0F, 0.0F, BAR_W, BAR_H, BAR_W, BAR_H);

        // Fuellung: Muster wandert (Periode 16 Pixel); bei wenig Leben pulsiert die Helligkeit, beim Herzen im Takt des Herzschlags
        int fillW = Math.round(BAR_W * progress);
        if (fillW > 0) {
            float light = 1.0F;
            if (design.feature() == Feature.VEINS) {
                light = 0.8F + 0.2F * beat;
            } else if (progress < 0.25F) {
                light = 0.78F + 0.22F * Mth.sin(now / 120.0F);
            }
            int shift = (int) ((now / 55L) % 16L);
            graphics.setColor(light, light, light, flicker);
            graphics.blit(design.tex("fill"), barX, barY, (float) shift, (float) (phase * BAR_H), fillW, BAR_H, FILL_W, BAR_H * phaseCount);
        }

        // Zusatzmerkmale ueber der Leiste
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        switch (design.feature()) {
            case CRACKS -> {
                int stage = progress > 2.0F / 3.0F ? 0 : progress > 1.0F / 3.0F ? 1 : progress > 0.10F ? 2 : 3;
                if (stage > 0) {
                    graphics.blit(design.tex("cracks"), barX, barY, 0.0F, (float) ((stage - 1) * BAR_H), BAR_W, BAR_H, BAR_W, BAR_H * 3);
                }
            }
            case VEINS -> {
                float glow = 0.3F + 0.7F * beat;
                graphics.setColor(1.0F, 1.0F, 1.0F, glow);
                graphics.blit(design.tex("veins"), barX, barY, 0.0F, 0.0F, BAR_W, BAR_H, BAR_W, BAR_H);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            case SHIELD -> {
                int x = barX + Math.round(BAR_W * design.marks()[0]) - 5;
                graphics.blit(design.tex("shield"), x, barY, progress > design.marks()[0] ? 0.0F : 10.0F, 0.0F, 10, BAR_H, 20, BAR_H);
            }
            default -> {
            }
        }

        // Phasenmarken: erreichte Phasen sind abgedunkelt, kommende leuchten weiss (der Schild ersetzt die Marke)
        if (design.feature() != Feature.SHIELD) {
            for (float phaseMark : design.marks()) {
                int x = barX + Math.round(BAR_W * phaseMark);
                boolean passed = progress <= phaseMark;
                graphics.fill(x, barY, x + 1, barY + BAR_H, passed ? 0x99000000 : 0xCCFFFFFF);
            }
        }

        // Rahmen mit Verzierungen
        graphics.setColor(1.0F, 1.0F, 1.0F, flicker);
        graphics.blit(design.tex("overlay"), frameX, frameY, 0.0F, (float) (phase * FRAME_H), FRAME_W, FRAME_H, FRAME_W, FRAME_H * phaseCount);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Herzschlag 0..1: Doppelschlag ("lub-dub") nach jeder Fieberwelle des Herzens, sonst ein ruhiger, langsamer Grundpuls.
     */
    private static float heartbeat(long now) {
        long since = now - VirusHeartEntity.lastPulseMillis();
        if (since >= 0L && since < 1400L) {
            float first = (float) Math.exp(-since / 150.0);
            float second = since > 170L ? 0.7F * (float) Math.exp(-(since - 170L) / 150.0) : 0.0F;
            return Math.max(first, second);
        }
        return 0.12F + 0.12F * Mth.sin(now / 520.0F);
    }
}
