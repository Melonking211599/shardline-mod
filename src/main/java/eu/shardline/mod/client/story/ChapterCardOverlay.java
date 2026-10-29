package eu.shardline.mod.client.story;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.item.custom.ChapterEmblemItem;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * Kapitelkarte: Beim Erreichen eines Kapitels blendet der Client ein Banner ein (Emblem, "Kapitel n", Titel,
 * Zusammenfassung), haelt es einige Sekunden und blendet es wieder aus. Ausgeloest nur vom Server
 * ({@link eu.shardline.mod.network.ChapterCardPayload}); abschaltbar in shardline-client.toml (chapterCard).
 * Textur: textures/gui/chapter_card.png (Banner 256x72, erzeugt von tools/assetgen/story2_assets.py).
 */
public final class ChapterCardOverlay {
    private static final ResourceLocation TEXTURE = Shardline.id("textures/gui/chapter_card.png");
    private static final int CARD_W = 256;
    private static final int CARD_H = 72;
    private static final long FADE_IN = 700L;
    private static final long HOLD = 5200L;
    private static final long FADE_OUT = 1100L;

    private static int chapter;
    private static long shownAt = -1L;

    private ChapterCardOverlay() {
    }

    /** @param value Emblem-Wert (Kapitel plus Zweig, {@link StoryChapter#emblemValue}) */
    public static void show(int value) {
        chapter = value;
        shownAt = Util.getMillis();
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        if (shownAt < 0L) {
            return;
        }
        long t = Util.getMillis() - shownAt;
        StoryChapter current = StoryChapter.byEmblemValue(chapter);
        StoryEnding branch = StoryChapter.branchOfEmblemValue(chapter);
        if (current == null || t > FADE_IN + HOLD + FADE_OUT || Minecraft.getInstance().options.hideGui) {
            if (t > FADE_IN + HOLD + FADE_OUT) {
                shownAt = -1L;
            }
            return;
        }
        float alpha = t < FADE_IN ? t / (float) FADE_IN : t > FADE_IN + HOLD ? 1.0F - (t - FADE_IN - HOLD) / (float) FADE_OUT : 1.0F;
        alpha = Mth.clamp(alpha, 0.0F, 1.0F);
        Font font = Minecraft.getInstance().font;
        int x = (graphics.guiWidth() - CARD_W) / 2;
        int y = graphics.guiHeight() / 5;

        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(TEXTURE, x, y, 0.0F, 0.0F, CARD_W, CARD_H, 256, 128);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (alpha > 0.35F) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + 18, y + 20, 0.0F);
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            graphics.renderItem(ChapterEmblemItem.of(current, branch), 0, 0);
            graphics.pose().popPose();
        }

        int a = Math.max(4, Math.round(alpha * 255.0F)) << 24;
        int textX = x + 70;
        graphics.drawString(font, current.label().copy().withStyle(ChatFormatting.BOLD), textX, y + 10, 0xF4C95A | a, true);
        graphics.pose().pushPose();
        graphics.pose().translate(textX, y + 22, 0.0F);
        graphics.pose().scale(1.5F, 1.5F, 1.0F);
        graphics.drawString(font, current.title(branch), 0, 0, 0xF0E4FF | a, true);
        graphics.pose().popPose();
        List<FormattedCharSequence> summary = font.split(current.summary(branch), CARD_W - 70 - 12);
        int lineY = y + 40;
        for (int i = 0; i < summary.size() && i < 3; i++) {
            graphics.drawString(font, summary.get(i), textX, lineY, 0xB9A9D6 | a, false);
            lineY += 10;
        }
        RenderSystem.disableBlend();
    }
}
