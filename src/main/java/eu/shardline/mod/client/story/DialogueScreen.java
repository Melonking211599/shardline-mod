package eu.shardline.mod.client.story;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.network.OpenDialoguePayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Dialogfenster der NPCs (nur Anzeige; jede Entscheidung trifft der Server).
 *
 * <pre>
 *  [Name]
 *  +--------+ +-------------------------------------------+   [1] Antwort
 *  |Porträt | | Text mit Schreibmaschinen-Effekt        v |   [2] Antwort
 *  +--------+ +-------------------------------------------+
 *        Enter/Leertaste weiter · 1-4 Antwort · L Verlauf · Esc schliessen
 * </pre>
 *
 * <p>Jede Textseite erscheint Zeichen fuer Zeichen (abschaltbar); ein Klick oder Enter zeigt sie ganz, der naechste
 * blaettert weiter. Antworten erscheinen erst auf der letzten Seite. Gesperrte Antworten sind grau und zeigen ihren
 * Hinweis beim Darueberfahren. Der Verlauf (L) listet alle Seiten und eigenen Antworten dieser Sitzung.</p>
 */
public class DialogueScreen extends Screen {
    private static final ResourceLocation FRAME = Shardline.id("textures/gui/dialogue/frame.png");
    private static final ResourceLocation PORTRAIT_FRAME = Shardline.id("textures/gui/dialogue/portrait_frame.png");
    private static final ResourceLocation NAMEPLATE = Shardline.id("textures/gui/dialogue/nameplate.png");
    private static final ResourceLocation BUTTON = Shardline.id("textures/gui/dialogue/button.png");
    private static final ResourceLocation ICONS = Shardline.id("textures/gui/dialogue/icons.png");
    private static final ResourceLocation UNKNOWN_PORTRAIT = Shardline.id("textures/gui/portraits/unknown.png");
    private static final Map<ResourceLocation, Boolean> EXISTS = new HashMap<>();

    private static final int TEXT_COLOR = 0xE8DCFF;
    private static final int NAME_COLOR = 0xF4C95A;
    private static final int HINT_COLOR = 0x9A8CB8;
    private static final int PORTRAIT_BOX = 80;
    private static final int BOX_H = 80;
    /** Niedrige Box (ohne Porträt) fuer kleine Fenster bzw. hohe GUI-Skalierung, damit vier Antworten darueber passen. */
    private static final int BOX_H_SMALL = 64;
    private static final int PAD = 9;
    private static final int LINE_H = 10;
    private static final int BUTTON_H = 20;
    private static final long SLIDE_MS = 260L;

    private OpenDialoguePayload payload;
    private String lastSpeaker = "";
    private long speakerChangedAt;
    private int page;
    private long pageStart;
    private long pageCompleteAt = -1L;
    private int lastSoundChar;
    private List<String> lines = List.of();
    private int totalChars;
    private int textScroll;
    private boolean awaitingServer;
    private boolean closedByServer;
    private boolean historyOpen;
    private int historyScroll;
    private int pressedChoice = -1;

    // Layout (in init berechnet, haengt von der GUI-Skalierung ab)
    private boolean compact;
    private int boxX;
    private int boxY;
    private int boxW;
    private int boxH = BOX_H;
    private int portraitX;
    private int textW;
    private int visibleLines;

    public DialogueScreen(OpenDialoguePayload payload) {
        super(Component.translatable("screen.shardline.dialogue.title"));
        this.payload = payload;
    }

    public int session() {
        return this.payload.session();
    }

    /** Neuer Knoten derselben Sitzung (Antwort angenommen oder Seite weiter). */
    public void show(OpenDialoguePayload next) {
        if (!next.speaker().equals(this.payload.speaker())) {
            this.lastSpeaker = this.payload.speaker();
            this.speakerChangedAt = Util.getMillis();
        }
        this.payload = next;
        this.awaitingServer = false;
        this.pressedChoice = -1;
        this.startPage(0);
    }

    /** Der Server hat den Dialog beendet (Ende, Schaden, Abstand ...). */
    public void closeFromServer() {
        this.closedByServer = true;
        this.onClose();
    }

    @Override
    protected void init() {
        int margin = 8;
        int available = this.width - margin * 2;
        this.compact = available < 300 || this.height < 220;
        this.boxH = this.height < 220 ? BOX_H_SMALL : BOX_H;
        int portraitSpace = this.compact ? 0 : PORTRAIT_BOX + 4;
        this.boxW = Math.min(available - portraitSpace, 360);
        int total = portraitSpace + this.boxW;
        this.portraitX = (this.width - total) / 2;
        this.boxX = this.portraitX + portraitSpace;
        this.boxY = this.height - this.boxH - 18;
        this.textW = this.boxW - PAD * 2 - 8;
        this.visibleLines = (this.boxH - PAD * 2) / LINE_H;
        this.startPage(this.page);
    }

    private void startPage(int index) {
        this.page = Mth.clamp(index, 0, Math.max(0, this.payload.text().size() - 1));
        this.pageStart = Util.getMillis();
        this.pageCompleteAt = -1L;
        this.lastSoundChar = 0;
        this.textScroll = 0;
        List<String> wrapped = new ArrayList<>();
        if (!this.payload.text().isEmpty()) {
            for (FormattedText line : this.font.getSplitter().splitLines(this.payload.text().get(this.page), Math.max(40, this.textW), Style.EMPTY)) {
                wrapped.add(line.getString());
            }
        }
        this.lines = wrapped;
        this.totalChars = wrapped.stream().mapToInt(String::length).sum();
        if (!ShardlineClientConfig.DIALOGUE_TYPEWRITER.get()) {
            this.pageCompleteAt = Util.getMillis();
        }
    }

    private int revealedChars() {
        if (this.pageCompleteAt >= 0L) {
            return this.totalChars;
        }
        long elapsed = Util.getMillis() - this.pageStart;
        int chars = (int) (elapsed * ShardlineClientConfig.DIALOGUE_TEXT_SPEED.get() / 1000L);
        if (chars >= this.totalChars) {
            this.pageCompleteAt = Util.getMillis();
            return this.totalChars;
        }
        return chars;
    }

    private boolean onLastPage() {
        return this.page >= this.payload.text().size() - 1;
    }

    private boolean choicesVisible() {
        return this.onLastPage() && this.pageCompleteAt >= 0L && !this.payload.choices().isEmpty();
    }

    // ------------------------------------------------------------------ Eingabe

    /** Enter, Leertaste oder Klick in die Box: Seite fertig zeigen, weiterblaettern oder (ohne Antworten) weiter. */
    private void advance() {
        if (this.awaitingServer) {
            return;
        }
        if (this.pageCompleteAt < 0L) {
            this.pageCompleteAt = Util.getMillis();
            return;
        }
        int maxScroll = Math.max(0, this.lines.size() - this.visibleLines);
        if (this.textScroll < maxScroll) {
            this.textScroll = Math.min(maxScroll, this.textScroll + this.visibleLines);
            return;
        }
        if (!this.onLastPage()) {
            this.startPage(this.page + 1);
            return;
        }
        if (this.payload.choices().isEmpty()) {
            this.playClick();
            this.awaitingServer = true;
            ClientDialogue.choose(this.session(), 0, null);
        }
    }

    private void choose(int index) {
        if (this.awaitingServer || !this.choicesVisible() || index < 0 || index >= this.payload.choices().size()) {
            return;
        }
        OpenDialoguePayload.Choice choice = this.payload.choices().get(index);
        if (!choice.enabled()) {
            return;
        }
        this.playClick();
        this.awaitingServer = true;
        ClientDialogue.choose(this.session(), index, choice.text());
    }

    private void playClick() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.DIALOGUE_CHOICE.get(), 1.0F, 0.6F));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            this.advance();
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_4) {
            this.choose(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_L) {
            this.historyOpen = !this.historyOpen;
            this.historyScroll = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            this.scroll(keyCode == GLFW.GLFW_KEY_UP ? 1 : -1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int choice = this.choiceAt(mouseX, mouseY);
            if (choice >= 0) {
                this.pressedChoice = choice;
                return true;
            }
            if (this.inside(mouseX, mouseY, this.historyButtonX(), this.historyButtonY(), 10, 10)) {
                this.historyOpen = !this.historyOpen;
                this.historyScroll = 0;
                return true;
            }
            if (this.inside(mouseX, mouseY, this.boxX, this.boxY, this.boxW, this.boxH)) {
                this.advance();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.pressedChoice >= 0) {
            int choice = this.choiceAt(mouseX, mouseY);
            if (choice == this.pressedChoice) {
                this.choose(choice);
            }
            this.pressedChoice = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll(scrollY > 0 ? 1 : scrollY < 0 ? -1 : 0);
        return true;
    }

    private void scroll(int direction) {
        if (this.historyOpen) {
            this.historyScroll = Math.max(0, this.historyScroll + direction);
        } else {
            int maxScroll = Math.max(0, this.lines.size() - this.visibleLines);
            this.textScroll = Mth.clamp(this.textScroll - direction, 0, maxScroll);
        }
    }

    @Override
    public void onClose() {
        if (!this.closedByServer) {
            ClientDialogue.sendClose(this.session());
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        // Automatisch weiterblaettern (Config), nie ueber Antworten hinweg
        if (ShardlineClientConfig.DIALOGUE_AUTO_ADVANCE.get() && this.pageCompleteAt >= 0L && !this.historyOpen
                && Util.getMillis() - this.pageCompleteAt > ShardlineClientConfig.DIALOGUE_AUTO_ADVANCE_SECONDS.get() * 1000L
                && !(this.onLastPage() && !this.payload.choices().isEmpty())) {
            this.advance();
        }
    }

    // ------------------------------------------------------------------ Antwortknoepfe

    private int choiceWidth() {
        int widest = 0;
        for (OpenDialoguePayload.Choice choice : this.payload.choices()) {
            widest = Math.max(widest, this.font.width(choice.text()));
        }
        return Mth.clamp(widest + 34, 120, this.boxW);
    }

    private int choiceX() {
        return this.boxX + this.boxW - this.choiceWidth();
    }

    private int choiceY(int index) {
        int count = this.payload.choices().size();
        return this.boxY - 4 - (count - index) * (BUTTON_H + 2);
    }

    private int choiceAt(double mouseX, double mouseY) {
        if (!this.choicesVisible() || this.historyOpen) {
            return -1;
        }
        for (int i = 0; i < this.payload.choices().size(); i++) {
            if (this.inside(mouseX, mouseY, this.choiceX(), this.choiceY(i), this.choiceWidth(), BUTTON_H)) {
                return i;
            }
        }
        return -1;
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private int historyButtonX() {
        return this.boxX + this.boxW - 14;
    }

    private int historyButtonY() {
        return this.boxY + 4;
    }

    // ------------------------------------------------------------------ Zeichnen

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Welt bleibt sichtbar: leichter Schleier, unten etwas dunkler hinter der Box
        graphics.fill(0, 0, this.width, this.height, 0x30000000);
        graphics.fillGradient(0, this.height / 2, this.width, this.height, 0x00000000, 0x90000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        this.renderPortraitAndName(graphics);
        nineSlice(graphics, FRAME, this.boxX, this.boxY, this.boxW, this.boxH, 8, 32, 32);
        this.renderText(graphics);
        this.renderIcons(graphics, mouseX, mouseY);
        if (this.historyOpen) {
            this.renderHistory(graphics);
        } else if (this.choicesVisible()) {
            this.renderChoices(graphics, mouseX, mouseY);
        }
        Component hints = Component.translatable("screen.shardline.dialogue.hints");
        if (this.font.width(hints) > this.width - 8) {
            hints = Component.translatable("screen.shardline.dialogue.hints_short");
        }
        graphics.drawCenteredString(this.font, hints, this.width / 2,
                this.boxY + this.boxH + 5, HINT_COLOR);
        RenderSystem.disableBlend();
    }

    private void renderPortraitAndName(GuiGraphics graphics) {
        float slide = Mth.clamp((Util.getMillis() - this.speakerChangedAt) / (float) SLIDE_MS, 0.0F, 1.0F);
        float eased = 1.0F - (1.0F - slide) * (1.0F - slide);
        int offset = Math.round((1.0F - eased) * -18.0F);
        Component name = Component.translatable("npc.shardline." + this.payload.speaker());
        if (!this.compact) {
            int x = this.portraitX + offset;
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.35F + 0.65F * eased);
            graphics.blit(this.portrait(), x + 8, this.boxY + 8, 0.0F, 0.0F, 64, 64, 64, 64);
            graphics.blit(PORTRAIT_FRAME, x, this.boxY, 0.0F, 0.0F, PORTRAIT_BOX, PORTRAIT_BOX, 80, 80);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        int plateW = this.font.width(name) + 22;
        int plateX = (this.compact ? this.boxX : this.portraitX) + Math.max(0, offset);
        int plateY = this.boxY - 15;
        hSlice(graphics, NAMEPLATE, plateX, plateY, plateW, 16, 8, 48, 16, 0, 0, 48);
        graphics.drawString(this.font, name, plateX + 11, plateY + 4, NAME_COLOR, true);
    }

    private ResourceLocation portrait() {
        ResourceLocation pose = Shardline.id("textures/gui/portraits/" + this.payload.speaker() + "_" + this.payload.portrait() + ".png");
        if (this.exists(pose)) {
            return pose;
        }
        ResourceLocation neutral = Shardline.id("textures/gui/portraits/" + this.payload.speaker() + "_neutral.png");
        return this.exists(neutral) ? neutral : UNKNOWN_PORTRAIT;
    }

    private boolean exists(ResourceLocation location) {
        return EXISTS.computeIfAbsent(location, rl -> Minecraft.getInstance().getResourceManager().getResource(rl).isPresent());
    }

    private void renderText(GuiGraphics graphics) {
        int revealed = this.revealedChars();
        if (revealed > this.lastSoundChar + 2 && revealed < this.totalChars && ShardlineClientConfig.DIALOGUE_TYPE_SOUND.get()) {
            this.lastSoundChar = revealed;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.DIALOGUE_TYPE.get(),
                    1.3F + (revealed % 5) * 0.06F, 0.18F));
        }
        int x = this.boxX + PAD;
        int y = this.boxY + PAD;
        int shown = 0;
        int remaining = revealed;
        for (int i = 0; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            String visible = remaining >= line.length() ? line : line.substring(0, Math.max(0, remaining));
            remaining -= line.length();
            if (i < this.textScroll) {
                continue;
            }
            if (shown >= this.visibleLines) {
                break;
            }
            if (!visible.isEmpty()) {
                graphics.drawString(this.font, visible, x, y + shown * LINE_H, TEXT_COLOR, false);
            }
            shown++;
            if (remaining <= 0) {
                break;
            }
        }
        // Seitenanzeige
        if (this.payload.text().size() > 1) {
            String pages = (this.page + 1) + "/" + this.payload.text().size();
            graphics.drawString(this.font, pages, this.boxX + this.boxW - PAD - this.font.width(pages) - 12, this.boxY + this.boxH - PAD - 7,
                    HINT_COLOR, false);
        }
    }

    private void renderIcons(GuiGraphics graphics, int mouseX, int mouseY) {
        // Verlauf-Knopf oben rechts in der Box
        boolean hover = this.inside(mouseX, mouseY, this.historyButtonX(), this.historyButtonY(), 10, 10);
        graphics.setColor(1.0F, 1.0F, 1.0F, hover || this.historyOpen ? 1.0F : 0.6F);
        graphics.blit(ICONS, this.historyButtonX(), this.historyButtonY(), 0.0F, 8.0F, 8, 8, 32, 16);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        // Weiter-Pfeil, wenn die Seite fertig ist und keine Antworten warten
        if (this.pageCompleteAt >= 0L && !this.choicesVisible() && !this.awaitingServer) {
            boolean bounce = (Util.getMillis() / 400L) % 2 == 0;
            graphics.blit(ICONS, this.boxX + this.boxW - PAD - 8, this.boxY + this.boxH - PAD - 6 + (bounce ? 0 : 1),
                    bounce ? 0.0F : 8.0F, 0.0F, 8, 6, 32, 16);
            if (this.onLastPage() && this.payload.ends()) {
                Component close = Component.translatable("screen.shardline.dialogue.close");
                graphics.drawString(this.font, close, this.boxX + this.boxW - PAD - 12 - this.font.width(close), this.boxY + this.boxH - PAD - 7,
                        HINT_COLOR, false);
            }
        }
    }

    private void renderChoices(GuiGraphics graphics, int mouseX, int mouseY) {
        int hovered = this.choiceAt(mouseX, mouseY);
        int width = this.choiceWidth();
        int x = this.choiceX();
        for (int i = 0; i < this.payload.choices().size(); i++) {
            OpenDialoguePayload.Choice choice = this.payload.choices().get(i);
            int y = this.choiceY(i);
            int state = !choice.enabled() ? 3 : this.pressedChoice == i && hovered == i ? 2 : hovered == i ? 1 : 0;
            hSlice(graphics, BUTTON, x, y, width, BUTTON_H, 4, 64, 80, 0, state * 20, 64);
            int shift = state == 2 ? 1 : 0;
            int color = choice.enabled() ? (state == 1 ? 0xFFFFFF : TEXT_COLOR) : 0x77708A;
            graphics.drawString(this.font, String.valueOf(i + 1), x + 7 + shift, y + 6 + shift, choice.enabled() ? NAME_COLOR : 0x77708A, false);
            if (!choice.enabled()) {
                graphics.blit(ICONS, x + width - 12, y + 6, 16.0F, 0.0F, 8, 8, 32, 16);
            }
            Component label = choice.text();
            int max = width - 34;
            FormattedCharSequence text = this.font.width(label) > max
                    ? FormattedCharSequence.forward(this.font.plainSubstrByWidth(label.getString(), max - 6) + "…", Style.EMPTY)
                    : label.getVisualOrderText();
            graphics.drawString(this.font, text, x + 18 + shift, y + 6 + shift, color, false);
        }
        if (hovered >= 0 && !this.payload.choices().get(hovered).enabled()) {
            graphics.renderTooltip(this.font, this.payload.choices().get(hovered).hint().copy().withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        }
    }

    private void renderHistory(GuiGraphics graphics) {
        int x = this.compact ? this.boxX : this.portraitX;
        int w = this.boxX + this.boxW - x;
        int top = 12;
        int h = this.boxY - 20 - top;
        if (h < 40) {
            return;
        }
        nineSlice(graphics, FRAME, x, top, w, h, 8, 32, 32);
        graphics.drawString(this.font, Component.translatable("screen.shardline.dialogue.history").withStyle(ChatFormatting.BOLD),
                x + PAD, top + PAD, NAME_COLOR, false);
        List<FormattedCharSequence> rendered = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (ClientDialogue.HistoryEntry entry : ClientDialogue.history()) {
            Component line = entry.ownChoice()
                    ? Component.literal("▸ ").append(entry.text())
                    : Component.empty().append(entry.speaker().copy().withStyle(ChatFormatting.GOLD)).append(": ").append(entry.text());
            for (FormattedCharSequence part : this.font.split(line, w - PAD * 2)) {
                rendered.add(part);
                colors.add(entry.ownChoice() ? 0xB9E8F0 : TEXT_COLOR);
            }
        }
        int rows = (h - PAD * 2 - 14) / LINE_H;
        int maxScroll = Math.max(0, rendered.size() - rows);
        this.historyScroll = Math.min(this.historyScroll, maxScroll);
        int start = Math.max(0, rendered.size() - rows - this.historyScroll);
        for (int i = 0; i < rows && start + i < rendered.size(); i++) {
            graphics.drawString(this.font, rendered.get(start + i), x + PAD, top + PAD + 14 + i * LINE_H, colors.get(start + i), false);
        }
    }

    // ------------------------------------------------------------------ Gekachelte Rahmen

    /** 9-Slice: Ecken fest, Kanten und Mitte gekachelt (keine Verzerrung der Pixel). */
    static void nineSlice(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h, int border, int texW, int texH) {
        int innerTexW = texW - border * 2;
        int innerTexH = texH - border * 2;
        int innerW = w - border * 2;
        int innerH = h - border * 2;
        // Ecken
        graphics.blit(texture, x, y, 0, 0, border, border, texW, texH);
        graphics.blit(texture, x + w - border, y, texW - border, 0, border, border, texW, texH);
        graphics.blit(texture, x, y + h - border, 0, texH - border, border, border, texW, texH);
        graphics.blit(texture, x + w - border, y + h - border, texW - border, texH - border, border, border, texW, texH);
        // Kanten und Mitte
        for (int dx = 0; dx < innerW; dx += innerTexW) {
            int cw = Math.min(innerTexW, innerW - dx);
            graphics.blit(texture, x + border + dx, y, border, 0, cw, border, texW, texH);
            graphics.blit(texture, x + border + dx, y + h - border, border, texH - border, cw, border, texW, texH);
            for (int dy = 0; dy < innerH; dy += innerTexH) {
                int ch = Math.min(innerTexH, innerH - dy);
                graphics.blit(texture, x + border + dx, y + border + dy, border, border, cw, ch, texW, texH);
            }
        }
        for (int dy = 0; dy < innerH; dy += innerTexH) {
            int ch = Math.min(innerTexH, innerH - dy);
            graphics.blit(texture, x, y + border + dy, 0, border, border, ch, texW, texH);
            graphics.blit(texture, x + w - border, y + border + dy, texW - border, border, border, ch, texW, texH);
        }
    }

    /** 3-Slice waagerecht: linke und rechte Kappe fest, Mitte gekachelt; Bereich (u, v, regionW, h) im Atlas. */
    static void hSlice(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h, int cap, int texW, int texH, int u, int v,
                       int regionW) {
        graphics.blit(texture, x, y, u, v, cap, h, texW, texH);
        graphics.blit(texture, x + w - cap, y, u + regionW - cap, v, cap, h, texW, texH);
        int inner = regionW - cap * 2;
        for (int dx = 0; dx < w - cap * 2; dx += inner) {
            int cw = Math.min(inner, w - cap * 2 - dx);
            graphics.blit(texture, x + cap + dx, y, u + cap, v, cw, h, texW, texH);
        }
    }
}
