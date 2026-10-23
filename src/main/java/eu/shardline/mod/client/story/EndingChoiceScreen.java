package eu.shardline.mod.client.story;

import eu.shardline.mod.network.EndingChoicePayload;
import eu.shardline.mod.story.StoryEnding;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Endentscheidung am Thron: Versiegeln oder Beherrschen. Im Abstimmungsmodus zaehlt die Wahl
 * als Stimme; der Countdown wird angezeigt.
 */
public class EndingChoiceScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private final boolean vote;
    private final long closesAt;
    private List<FormattedCharSequence> lines = List.of();

    public EndingChoiceScreen(boolean vote, int secondsLeft) {
        super(Component.translatable("screen.shardline.ending.title"));
        this.vote = vote;
        this.closesAt = secondsLeft > 0 ? Util.getMillis() + secondsLeft * 1000L : 0L;
    }

    @Override
    protected void init() {
        this.lines = this.font.split(Component.translatable(this.vote ? "screen.shardline.ending.text_vote" : "screen.shardline.ending.text"),
                Math.min(320, this.width - 40));
        int y = this.height / 2 + 30;
        int center = this.width / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.shardline.ending.seal").withStyle(ChatFormatting.GOLD),
                        button -> this.choose(StoryEnding.SEALED))
                .bounds(center - BUTTON_WIDTH - 5, y, BUTTON_WIDTH, 20)
                .tooltip(Tooltip.create(Component.translatable("story.shardline.ending.seal.description")))
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.shardline.ending.dominate").withStyle(ChatFormatting.DARK_PURPLE),
                        button -> this.choose(StoryEnding.DOMINATED))
                .bounds(center + 5, y, BUTTON_WIDTH, 20)
                .tooltip(Tooltip.create(Component.translatable("story.shardline.ending.dominate.description")))
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.shardline.ending.later"), button -> this.onClose())
                .bounds(center - 50, y + 28, 100, 20)
                .build());
    }

    private void choose(StoryEnding choice) {
        PacketDistributor.sendToServer(new EndingChoicePayload(choice.id()));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int y = this.height / 2 - 70;
        graphics.drawCenteredString(this.font, this.title.copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), center, y, 0xFFFFFF);
        y += 20;
        for (FormattedCharSequence line : this.lines) {
            graphics.drawCenteredString(this.font, line, center, y, 0xD8C8F0);
            y += 11;
        }
        if (this.vote && this.closesAt > 0L) {
            long seconds = Math.max(0L, (this.closesAt - Util.getMillis()) / 1000L);
            graphics.drawCenteredString(this.font, Component.translatable("screen.shardline.ending.countdown", seconds),
                    center, this.height / 2 + 82, 0xFFD86B);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
