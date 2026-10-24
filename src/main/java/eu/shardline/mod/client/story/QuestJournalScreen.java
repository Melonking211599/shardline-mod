package eu.shardline.mod.client.story;

import com.mojang.blaze3d.platform.InputConstants;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.client.KeyBindings;
import eu.shardline.mod.item.custom.ChapterEmblemItem;
import eu.shardline.mod.network.JournalPayload;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.lore.LoreBooks;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Auftragsbuch ("Chronik"): vier Reiter (Kapitel, Auftraege, Figuren, Lore), links eine Liste, rechts die Einzelheiten.
 * Zeigt nur an, was der Server im {@link JournalPayload} schickt. Oeffnen mit Taste J oder dem Chronik-Tagebuch.
 *
 * <p>Layout in Buchkoordinaten: Reiter 32x28 oben (3 px ueber dem Rand), Buch 256x192 (journal/background.png), Liste
 * x 8-104, Detailfeld x 110-248, beide y 22-184. Passt das Buch nicht in das Fenster (hohe GUI-Skalierung), wird es
 * als Ganzes verkleinert; Mauskoordinaten werden zurueckgerechnet. Bedienung: Klick, Mausrad, 1-4 Reiter,
 * Pfeil hoch/runter Auswahl, Bild hoch/runter Text, Esc oder J schliessen.</p>
 */
public class QuestJournalScreen extends Screen {
    private static final ResourceLocation BACKGROUND = Shardline.id("textures/gui/journal/background.png");
    private static final ResourceLocation TABS = Shardline.id("textures/gui/journal/tabs.png");
    private static final ResourceLocation ICONS = Shardline.id("textures/gui/dialogue/icons.png");
    private static final String[] TAB_IDS = {"chapters", "quests", "npcs", "lore"};
    private static final int BOOK_W = 256;
    private static final int BOOK_H = 192;
    private static final int TAB_W = 32;
    private static final int TAB_H = 28;
    /** Oberkante des Buchs unter den Reitern (die Reiter ragen 3 px in den Rand). */
    private static final int BOOK_Y = TAB_H - 3;
    private static final int LIST_X = 8;
    private static final int PANEL_Y = 22;
    private static final int LIST_W = 96;
    private static final int PANEL_H = 162;
    private static final int DETAIL_X = 110;
    private static final int DETAIL_W = 138;
    private static final int ROW_H = 12;
    private static final int LINE_H = 10;
    private static final int TEXT = 0xFFE8DCFF;
    private static final int GOLD = 0xFFF4C95A;
    private static final int DIM = 0xFF8C7FAE;
    private static final int DONE = 0xFF8FE6B0;
    private static int lastTab;

    private JournalPayload payload;
    private int tab = lastTab;
    private final int[] selected = new int[4];
    private int listScroll;
    private int detailScroll;
    private float scale = 1.0F;
    private float left;
    private float top;
    private List<Entry> entries = List.of();

    /** Eine Zeile der Liste mit ihrem Detailtext. */
    private record Entry(Component label, int color, boolean done, List<Component> detail, @Nullable ResourceLocation portrait,
                         @Nullable ItemStack icon) {
    }

    public QuestJournalScreen(JournalPayload payload) {
        super(Component.translatable("screen.shardline.journal.title"));
        this.payload = payload;
    }

    /** Neuer Stand vom Server (Auftrag weiter, Kapitel, Flagge); Auswahl bleibt, soweit moeglich. */
    public void refresh(JournalPayload payload) {
        this.payload = payload;
        this.rebuild();
    }

    @Override
    protected void init() {
        this.scale = Math.min(1.0F, Math.min((this.width - 8) / (float) BOOK_W, (this.height - 8) / (float) (BOOK_H + BOOK_Y)));
        this.left = (this.width - BOOK_W * this.scale) / 2.0F;
        this.top = (this.height - (BOOK_H + BOOK_Y) * this.scale) / 2.0F;
        this.rebuild();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ Inhalt

    private void rebuild() {
        this.entries = switch (this.tab) {
            case 0 -> this.chapterEntries();
            case 1 -> this.questEntries();
            case 2 -> this.npcEntries();
            default -> this.loreEntries();
        };
        this.selected[this.tab] = Mth.clamp(this.selected[this.tab], 0, Math.max(0, this.entries.size() - 1));
        this.listScroll = Mth.clamp(this.listScroll, 0, Math.max(0, this.entries.size() - this.visibleRows()));
    }

    private StoryEnding branch() {
        return StoryEnding.byId(this.payload.branch());
    }

    private List<Entry> chapterEntries() {
        List<Entry> list = new ArrayList<>();
        for (int number = this.payload.chapter(); number >= 1; number--) {
            StoryChapter chapter = StoryChapter.byNumber(number);
            if (chapter == null) {
                continue;
            }
            StoryEnding branch = chapter.branched() ? this.branch() : StoryEnding.NONE;
            boolean current = number == this.payload.chapter();
            List<Component> detail = new ArrayList<>();
            detail.add(chapter.label().copy().withStyle(ChatFormatting.GRAY));
            detail.add(chapter.title(branch).copy().withStyle(Style.EMPTY.withColor(GOLD).withBold(true)));
            detail.add(Component.empty());
            detail.add(chapter.summary(branch));
            if (current) {
                detail.add(Component.empty());
                detail.add(Component.translatable("screen.shardline.journal.next").withStyle(Style.EMPTY.withColor(GOLD)));
                detail.add(Component.translatable(this.payload.nextKey()));
                if (!this.payload.epilogue().isEmpty()) {
                    detail.add(Component.empty());
                    detail.add(Component.translatable("screen.shardline.journal.epilogue",
                            Component.translatable("story.shardline.epilogue." + this.payload.epilogue())).withStyle(Style.EMPTY.withColor(GOLD)));
                }
            }
            list.add(new Entry(Component.literal(number + " · ").append(chapter.title(branch)), current ? GOLD : TEXT, !current, detail, null,
                    ChapterEmblemItem.of(chapter, branch)));
        }
        return list;
    }

    private List<Entry> questEntries() {
        List<Entry> list = new ArrayList<>();
        for (JournalPayload.Quest quest : this.payload.quests()) {
            String key = "quest.shardline." + quest.id();
            List<Component> detail = new ArrayList<>();
            detail.add(Component.translatable(key + ".title").withStyle(Style.EMPTY.withColor(GOLD).withBold(true)));
            if (!quest.giver().isEmpty()) {
                detail.add(Component.translatable("screen.shardline.journal.giver", Component.translatable("npc.shardline." + quest.giver()))
                        .withStyle(ChatFormatting.GRAY));
            }
            detail.add(Component.empty());
            detail.add(Component.translatable(key + ".description"));
            detail.add(Component.empty());
            for (int i = 0; i < quest.steps(); i++) {
                Component step = Component.translatable(key + ".step." + (i + 1));
                if (i < quest.step()) {
                    detail.add(Component.literal("✔ ").append(step).withStyle(Style.EMPTY.withColor(DONE)));
                } else if (i == quest.step() && quest.active()) {
                    Component line = Component.literal("▸ ").append(step);
                    if (quest.needed() > 1) {
                        line = line.copy().append(Component.literal("  " + quest.count() + "/" + quest.needed()).withStyle(Style.EMPTY.withColor(GOLD)));
                    }
                    detail.add(line);
                } else {
                    detail.add(Component.translatable("screen.shardline.journal.hidden_step").withStyle(Style.EMPTY.withColor(DIM)));
                    break;
                }
            }
            if (!quest.active()) {
                detail.add(Component.empty());
                detail.add(Component.translatable("screen.shardline.journal.completed", quest.completions()).withStyle(Style.EMPTY.withColor(DONE)));
            }
            list.add(new Entry(Component.translatable(key + ".title"), quest.active() ? GOLD : DIM, !quest.active(), detail, null, null));
        }
        return list;
    }

    private List<Entry> npcEntries() {
        List<Entry> list = new ArrayList<>();
        for (JournalPayload.Npc npc : this.payload.npcs()) {
            Component name = Component.translatable("npc.shardline." + npc.id());
            List<Component> detail = List.of(name.copy().withStyle(Style.EMPTY.withColor(GOLD).withBold(true)), Component.empty(),
                    Component.translatable(npc.bioKey()));
            list.add(new Entry(name, TEXT, false, detail, Shardline.id("textures/gui/portraits/" + npc.id() + "_neutral.png"), null));
        }
        return list;
    }

    private List<Entry> loreEntries() {
        List<Entry> list = new ArrayList<>();
        for (JournalPayload.Lore lore : this.payload.lore()) {
            LoreBooks.LoreBook book = LoreBooks.byId(lore.book());
            if (book == null) {
                continue;
            }
            List<Component> detail = new ArrayList<>();
            detail.add(book.title().copy().withStyle(Style.EMPTY.withColor(GOLD).withBold(true)));
            detail.add(book.author().copy().withStyle(ChatFormatting.GRAY));
            int read = 0;
            for (int page = 1; page <= book.pages(); page++) {
                if ((lore.pages() & (1 << (page - 1))) != 0) {
                    read++;
                    detail.add(Component.empty());
                    detail.add(Component.translatable("screen.shardline.journal.page", page).withStyle(Style.EMPTY.withColor(GOLD)));
                    detail.add(book.page(page));
                }
            }
            if (read < book.pages()) {
                detail.add(Component.empty());
                detail.add(Component.translatable("screen.shardline.journal.pages_missing", book.pages() - read).withStyle(Style.EMPTY.withColor(DIM)));
            }
            list.add(new Entry(book.title(), read == book.pages() ? TEXT : DIM, read == book.pages(), detail, null, null));
        }
        return list;
    }

    // ------------------------------------------------------------------ Zeichnen

    private int visibleRows() {
        return (PANEL_H - 8) / ROW_H;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        float bx = (mouseX - this.left) / this.scale;
        float by = (mouseY - this.top) / this.scale;
        graphics.pose().pushPose();
        graphics.pose().translate(this.left, this.top, 0.0F);
        graphics.pose().scale(this.scale, this.scale, 1.0F);
        for (int i = 0; i < 4; i++) {
            if (i != this.tab) {
                this.drawTab(graphics, i, false);
            }
        }
        graphics.blit(BACKGROUND, 0, BOOK_Y, 0, 0, BOOK_W, BOOK_H, 256, 192);
        this.drawTab(graphics, this.tab, true);

        Component tabName = Component.translatable("screen.shardline.journal.tab." + TAB_IDS[this.tab]);
        graphics.drawString(this.font, tabName, 12, BOOK_Y + 6, GOLD, false);
        StoryChapter chapter = StoryChapter.byNumber(this.payload.chapter());
        if (chapter != null) {
            Component right = chapter.label();
            graphics.drawString(this.font, right, BOOK_W - 12 - this.font.width(right), BOOK_Y + 6, DIM, false);
        }

        this.drawList(graphics, bx, by);
        this.drawDetail(graphics);
        graphics.pose().popPose();

        int hoverRow = this.rowAt(bx, by);
        if (hoverRow >= 0 && this.font.width(this.entries.get(hoverRow).label()) > LIST_W - 10 - (this.entries.get(hoverRow).done() ? 9 : 0)) {
            graphics.renderTooltip(this.font, this.entries.get(hoverRow).label(), mouseX, mouseY);   // gekuerzter Titel
        }
        int hoverTab = this.tabAt(bx, by);
        if (hoverTab >= 0) {
            graphics.renderTooltip(this.font, Component.translatable("screen.shardline.journal.tab." + TAB_IDS[hoverTab])
                    .append(Component.literal("  [" + (hoverTab + 1) + "]").withStyle(ChatFormatting.DARK_GRAY)), mouseX, mouseY);
        }
    }

    private void drawTab(GuiGraphics graphics, int index, boolean active) {
        int x = 10 + index * (TAB_W + 2);
        graphics.blit(TABS, x, active ? 0 : 2, index * TAB_W, active ? 0 : TAB_H, TAB_W, TAB_H, 128, 56);
    }

    private int tabAt(float bx, float by) {
        if (by < 0 || by >= BOOK_Y + 3) {
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            int x = 10 + i * (TAB_W + 2);
            if (bx >= x && bx < x + TAB_W) {
                return i;
            }
        }
        return -1;
    }

    private int rowAt(float bx, float by) {
        int top = BOOK_Y + PANEL_Y + 4;
        if (bx < LIST_X + 2 || bx >= LIST_X + LIST_W - 2 || by < top || by >= top + this.visibleRows() * ROW_H) {
            return -1;
        }
        int index = this.listScroll + (int) ((by - top) / ROW_H);
        return index < this.entries.size() ? index : -1;
    }

    private void drawList(GuiGraphics graphics, float bx, float by) {
        int top = BOOK_Y + PANEL_Y + 4;
        if (this.entries.isEmpty()) {
            List<FormattedCharSequence> lines = this.font.split(Component.translatable("screen.shardline.journal.empty." + TAB_IDS[this.tab]),
                    LIST_W - 10);
            for (int i = 0; i < lines.size(); i++) {
                graphics.drawString(this.font, lines.get(i), LIST_X + 5, top + 2 + i * LINE_H, DIM, false);
            }
            return;
        }
        int hover = this.rowAt(bx, by);
        int rows = Math.min(this.visibleRows(), this.entries.size() - this.listScroll);
        for (int i = 0; i < rows; i++) {
            int index = this.listScroll + i;
            Entry entry = this.entries.get(index);
            int y = top + i * ROW_H;
            if (index == this.selected[this.tab]) {
                graphics.fill(LIST_X + 3, y, LIST_X + LIST_W - 3, y + ROW_H - 1, 0x663F2A78);
            } else if (index == hover) {
                graphics.fill(LIST_X + 3, y, LIST_X + LIST_W - 3, y + ROW_H - 1, 0x33A681F4);
            }
            int textW = LIST_W - 10 - (entry.done() ? 9 : 0);
            String plain = entry.label().getString();
            FormattedCharSequence label = this.font.width(entry.label()) > textW
                    ? FormattedCharSequence.forward(this.font.plainSubstrByWidth(plain, textW - 6) + "…", Style.EMPTY)
                    : entry.label().getVisualOrderText();
            graphics.drawString(this.font, label, LIST_X + 5, y + 2, entry.color(), false);
            if (entry.done()) {
                graphics.blit(ICONS, LIST_X + LIST_W - 13, y + 2, 24, 0, 8, 8, 32, 16);
            }
        }
        if (this.entries.size() > this.visibleRows()) {
            int trackH = this.visibleRows() * ROW_H;
            int barH = Math.max(8, trackH * this.visibleRows() / this.entries.size());
            int maxScroll = this.entries.size() - this.visibleRows();
            int barY = top + (trackH - barH) * this.listScroll / Math.max(1, maxScroll);
            graphics.fill(LIST_X + LIST_W - 3, barY, LIST_X + LIST_W - 2, barY + barH, 0xAAA681F4);
        }
    }

    private List<FormattedCharSequence> detailLines(List<Component> detail, int width) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component paragraph : detail) {
            if (paragraph.getString().isEmpty()) {
                lines.add(FormattedCharSequence.EMPTY);
            } else {
                lines.addAll(this.font.split(paragraph, width));
            }
        }
        return lines;
    }

    /** Kopfbereich: Portraet (48 px) oder Emblem mit der ersten Detailzeile daneben (18 px). */
    private int detailHeader(Entry entry) {
        return entry.portrait() != null ? 52 : entry.icon() != null ? 20 : 0;
    }

    private void drawDetail(GuiGraphics graphics) {
        if (this.entries.isEmpty()) {
            return;
        }
        Entry entry = this.entries.get(this.selected[this.tab]);
        int x = DETAIL_X + 6;
        int y = BOOK_Y + PANEL_Y + 6;
        int width = DETAIL_W - 12;
        if (entry.portrait() != null) {
            graphics.fill(x - 1, y - 1, x + 49, y + 49, 0xFF2A1556);
            graphics.blit(entry.portrait(), x, y, 48, 48, 0, 0, 64, 64, 64, 64);
        }
        List<Component> detail = entry.detail();
        if (entry.icon() != null) {
            graphics.renderItem(entry.icon(), x, y);
            graphics.drawString(this.font, detail.get(0), x + 20, y + 4, TEXT, false);
            detail = detail.subList(1, detail.size());
        }
        int header = this.detailHeader(entry);
        List<FormattedCharSequence> lines = this.detailLines(detail, width);
        int visible = (PANEL_H - 12 - header) / LINE_H;
        this.detailScroll = Mth.clamp(this.detailScroll, 0, Math.max(0, lines.size() - visible));
        for (int i = 0; i < visible && this.detailScroll + i < lines.size(); i++) {
            graphics.drawString(this.font, lines.get(this.detailScroll + i), x, y + header + i * LINE_H, TEXT, false);
        }
        if (lines.size() > visible) {
            int trackY = y + header;
            int trackH = visible * LINE_H;
            int barH = Math.max(8, trackH * visible / lines.size());
            int barY = trackY + (trackH - barH) * this.detailScroll / Math.max(1, lines.size() - visible);
            graphics.fill(DETAIL_X + DETAIL_W - 4, barY, DETAIL_X + DETAIL_W - 3, barY + barH, 0xAAA681F4);
        }
    }

    // ------------------------------------------------------------------ Eingabe

    private void selectTab(int index) {
        if (index != this.tab) {
            this.tab = index;
            lastTab = index;
            this.listScroll = 0;
            this.detailScroll = 0;
            this.rebuild();
        }
    }

    private void select(int index) {
        if (!this.entries.isEmpty()) {
            this.selected[this.tab] = Mth.clamp(index, 0, this.entries.size() - 1);
            this.detailScroll = 0;
            if (this.selected[this.tab] < this.listScroll) {
                this.listScroll = this.selected[this.tab];
            } else if (this.selected[this.tab] >= this.listScroll + this.visibleRows()) {
                this.listScroll = this.selected[this.tab] - this.visibleRows() + 1;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float bx = (float) ((mouseX - this.left) / this.scale);
        float by = (float) ((mouseY - this.top) / this.scale);
        int clickedTab = this.tabAt(bx, by);
        if (clickedTab >= 0) {
            this.selectTab(clickedTab);
            return true;
        }
        int row = this.rowAt(bx, by);
        if (row >= 0) {
            this.select(row);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float bx = (float) ((mouseX - this.left) / this.scale);
        int step = scrollY > 0 ? -1 : 1;
        if (bx < DETAIL_X) {
            this.listScroll = Mth.clamp(this.listScroll + step, 0, Math.max(0, this.entries.size() - this.visibleRows()));
        } else {
            this.detailScroll = Math.max(0, this.detailScroll + step * 2);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindings.JOURNAL.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4 -> {
                this.selectTab(keyCode - GLFW.GLFW_KEY_1);
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                this.select(this.selected[this.tab] - 1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                this.select(this.selected[this.tab] + 1);
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                this.detailScroll = Math.max(0, this.detailScroll - 6);
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                this.detailScroll += 6;
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }
}
