package eu.shardline.mod.client.story;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.network.TraderActionPayload;
import eu.shardline.mod.network.TraderDialoguePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Dialogfenster des Splitterhaendlers: Porträt links, Name und Text (Zeile fuer Zeile eingeblendet) rechts,
 * Knoepfe darunter. Der Client zeigt nur an; was gesagt wird und was ein Knopf bewirkt, entscheidet der Server.
 * Textur: textures/gui/trader_dialogue.png (Rahmen 240x100 mit Porträt, erzeugt von tools/assetgen/story2_assets.py).
 */
public class TraderDialogueScreen extends Screen {
    private static final ResourceLocation TEXTURE = Shardline.id("textures/gui/trader_dialogue.png");
    private static final int PANEL_W = 240;
    private static final int PANEL_H = 100;
    private static final int TEXT_X = 68;
    private static final int TEXT_Y = 21;
    private static final int TEXT_W = 164;
    /** Buchstaben pro Sekunde fuer das Einblenden. */
    private static final float CHARS_PER_SECOND = 45.0F;

    private final TraderDialoguePayload dialogue;
    private final Component name;
    private final List<FormattedCharSequence> wrapped = new ArrayList<>();
    private int totalChars;
    private long openedAt;
    private boolean revealAll;
    private int left;
    private int top;

    public TraderDialogueScreen(TraderDialoguePayload dialogue) {
        super(Component.translatable("screen.shardline.trader.title"));
        this.dialogue = dialogue;
        Entity entity = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(dialogue.entityId()) : null;
        this.name = entity != null ? entity.getDisplayName() : Component.translatable("entity.shardline.shard_trader");
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = this.height - PANEL_H - 24;
        this.openedAt = Util.getMillis();
        this.wrapped.clear();
        this.totalChars = 0;
        for (String key : this.dialogue.lines()) {
            for (FormattedCharSequence line : this.font.split(Component.translatable(key), TEXT_W)) {
                this.wrapped.add(line);
                this.totalChars += lengthOf(line);
            }
        }

        int y = this.top + PANEL_H - 26;
        if (this.dialogue.mode() == TraderDialoguePayload.LORE) {
            Button sell = this.button(Component.translatable("screen.shardline.trader.sell_pages", this.dialogue.pages()),
                    TraderActionPayload.SELL_PAGES, 8, y, 88);
            sell.active = this.dialogue.pages() > 0;
            Button bind = this.button(Component.translatable("screen.shardline.trader.bind_book", this.dialogue.bindable()),
                    TraderActionPayload.BIND_BOOK, 100, y, 80);
            bind.active = this.dialogue.bindable() > 0;
            this.button(Component.translatable("screen.shardline.trader.back"), TraderActionPayload.BACK, 184, y, 48);
        } else {
            this.button(Component.translatable("screen.shardline.trader.trade").withStyle(ChatFormatting.GOLD), TraderActionPayload.TRADE, 8, y, 54);
            this.button(Component.translatable("screen.shardline.trader.lore"), TraderActionPayload.LORE, 66, y, 62);
            this.button(Component.translatable("screen.shardline.trader.more"), TraderActionPayload.MORE, 132, y, 54);
            this.addRenderableWidget(Button.builder(Component.translatable("screen.shardline.trader.bye"), b -> this.onClose())
                    .bounds(this.left + 190, y, 42, 20).build());
        }
    }

    private Button button(Component label, int action, int x, int y, int width) {
        return this.addRenderableWidget(Button.builder(label, b -> {
            PacketDistributor.sendToServer(new TraderActionPayload(this.dialogue.entityId(), action));
            if (action == TraderActionPayload.TRADE) {
                this.onClose();
            }
        }).bounds(this.left + x, y, width, 20).build());
    }

    private static int lengthOf(FormattedCharSequence line) {
        int[] count = {0};
        line.accept((index, style, codePoint) -> {
            count[0]++;
            return true;
        });
        return count[0];
    }

    /** Schneidet eine Zeile nach {@code chars} Zeichen ab (fuer das Einblenden). */
    private static FormattedCharSequence prefix(FormattedCharSequence line, int chars) {
        return sink -> {
            int[] left = {chars};
            return line.accept((index, style, codePoint) -> {
                if (left[0]-- <= 0) {
                    return false;
                }
                return sink.accept(index, style, codePoint);
            });
        };
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Kein abgedunkelter Hintergrund: Der Haendler soll sichtbar bleiben.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, this.left, this.top, 0.0F, 0.0F, PANEL_W, PANEL_H, 256, 128);
        graphics.drawString(this.font, this.name.copy().withStyle(Style.EMPTY.withBold(true)), this.left + TEXT_X, this.top + 8, 0xF4C95A, true);

        int shown = this.revealAll ? Integer.MAX_VALUE : (int) ((Util.getMillis() - this.openedAt) / 1000.0F * CHARS_PER_SECOND);
        int y = this.top + TEXT_Y;
        int maxLines = 5;
        for (int i = 0; i < this.wrapped.size() && i < maxLines; i++) {
            FormattedCharSequence line = this.wrapped.get(i);
            int length = lengthOf(line);
            graphics.drawString(this.font, shown >= length ? line : prefix(line, Math.max(0, shown)), this.left + TEXT_X, y, 0xE8DCFF, false);
            shown -= length;
            if (shown <= 0) {
                break;
            }
            y += 10;
        }
        // Blinkender Pfeil, solange noch Text kommt
        if (!this.revealAll && (Util.getMillis() - this.openedAt) / 1000.0F * CHARS_PER_SECOND < this.totalChars
                && (Util.getMillis() / 400L) % 2L == 0L) {
            graphics.drawString(this.font, "▾", this.left + PANEL_W - 14, this.top + PANEL_H - 36, 0xF4C95A, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Klick auf den Text zeigt sofort alles. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (Mth.clamp(mouseX, this.left, this.left + PANEL_W) == mouseX && Mth.clamp(mouseY, this.top, this.top + PANEL_H) == mouseY) {
            this.revealAll = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
