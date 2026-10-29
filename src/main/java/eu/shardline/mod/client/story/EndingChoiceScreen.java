package eu.shardline.mod.client.story;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.network.EndingChoicePayload;
import eu.shardline.mod.story.StoryEnding;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Endentscheidung am Entscheidungsaltar (oder Thron): zwei grosse Karten, links "Versiegeln" (Elfenbein/Gold),
 * rechts "Beherrschen" (Schwarzviolett/Magenta). Die Karte unter der Maus leuchtet auf, ein Klick waehlt. Im
 * Abstimmungsmodus zaehlt die Wahl als Stimme; der Countdown wird angezeigt.
 * Textur: textures/gui/ending_choice.png (zwei Karten 150x190, erzeugt von tools/assetgen/story2_assets.py).
 */
public class EndingChoiceScreen extends Screen {
    private static final ResourceLocation TEXTURE = Shardline.id("textures/gui/ending_choice.png");
    private static final int CARD_W = 150;
    private static final int CARD_H = 190;
    private static final int GAP = 18;
    private static final int TEXT_W = 122;

    private final boolean vote;
    private final long closesAt;
    private List<FormattedCharSequence> intro = List.of();
    private List<FormattedCharSequence> sealText = List.of();
    private List<FormattedCharSequence> dominateText = List.of();
    private int cardsLeft;
    private int cardsTop;

    public EndingChoiceScreen(boolean vote, int secondsLeft) {
        super(Component.translatable("screen.shardline.ending.title"));
        this.vote = vote;
        this.closesAt = secondsLeft > 0 ? Util.getMillis() + secondsLeft * 1000L : 0L;
    }

    @Override
    protected void init() {
        this.intro = this.font.split(Component.translatable(this.vote ? "screen.shardline.ending.text_vote" : "screen.shardline.ending.text"),
                Math.min(2 * CARD_W + GAP, this.width - 40));
        this.sealText = this.font.split(Component.translatable("story.shardline.ending.seal.description"), TEXT_W);
        this.dominateText = this.font.split(Component.translatable("story.shardline.ending.dominate.description"), TEXT_W);
        this.cardsLeft = (this.width - 2 * CARD_W - GAP) / 2;
        int block = 22 + this.intro.size() * 10 + 8 + CARD_H + 30;
        this.cardsTop = Math.max(4, (this.height - block) / 2) + 22 + this.intro.size() * 10 + 8;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.shardline.ending.later"), button -> this.onClose())
                .bounds(this.width / 2 - 50, this.cardsTop + CARD_H + 8, 100, 20).build());
    }

    /** 0 = keine Karte, 1 = Versiegeln, 2 = Beherrschen. */
    private int cardAt(double mouseX, double mouseY) {
        if (mouseY < this.cardsTop || mouseY >= this.cardsTop + CARD_H) {
            return 0;
        }
        if (mouseX >= this.cardsLeft && mouseX < this.cardsLeft + CARD_W) {
            return 1;
        }
        int right = this.cardsLeft + CARD_W + GAP;
        return mouseX >= right && mouseX < right + CARD_W ? 2 : 0;
    }

    private void choose(StoryEnding choice) {
        PacketDistributor.sendToServer(new EndingChoicePayload(choice.id()));
        this.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int card = this.cardAt(mouseX, mouseY);
        if (button == 0 && card != 0) {
            this.choose(card == 1 ? StoryEnding.SEALED : StoryEnding.DOMINATED);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int y = this.cardsTop - 8 - this.intro.size() * 10 - 22;
        graphics.drawCenteredString(this.font, this.title.copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), center, y, 0xFFFFFF);
        y += 22;
        for (FormattedCharSequence line : this.intro) {
            graphics.drawCenteredString(this.font, line, center, y, 0xD8C8F0);
            y += 10;
        }

        int hovered = this.cardAt(mouseX, mouseY);
        float pulse = 0.85F + 0.15F * Mth.sin(Util.getMillis() / 300.0F);
        this.card(graphics, this.cardsLeft, 0, hovered, pulse, "screen.shardline.ending.seal", 0x8A5A14, 0x3A2A10, this.sealText);
        this.card(graphics, this.cardsLeft + CARD_W + GAP, CARD_W, hovered, pulse, "screen.shardline.ending.dominate", 0xFF6FDC, 0xE8DCFF,
                this.dominateText);

        if (this.vote && this.closesAt > 0L) {
            long seconds = Math.max(0L, (this.closesAt - Util.getMillis()) / 1000L);
            graphics.drawCenteredString(this.font, Component.translatable("screen.shardline.ending.countdown", seconds),
                    center, this.cardsTop + CARD_H + 32, 0xFFD86B);
        }
    }

    private void card(GuiGraphics graphics, int x, int u, int hovered, float pulse, String titleKey, int titleColor, int textColor,
                      List<FormattedCharSequence> text) {
        int index = u == 0 ? 1 : 2;
        float light = hovered == index ? pulse + 0.15F : hovered == 0 ? 0.92F : 0.62F;
        RenderSystem.enableBlend();
        graphics.setColor(Math.min(1.0F, light), Math.min(1.0F, light), Math.min(1.0F, light), 1.0F);
        int lift = hovered == index ? 3 : 0;
        graphics.blit(TEXTURE, x, this.cardsTop - lift, u, 0.0F, CARD_W, CARD_H, 512, 256);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        Component title = Component.translatable(titleKey).withStyle(ChatFormatting.BOLD);
        graphics.drawString(this.font, title, x + (CARD_W - this.font.width(title)) / 2, this.cardsTop - lift + 76, titleColor, u != 0);
        int ty = this.cardsTop - lift + 92;
        for (int i = 0; i < text.size() && i < 8; i++) {
            graphics.drawString(this.font, text.get(i), x + 14, ty, textColor, false);
            ty += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
