package eu.shardline.mod.client.story;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.story.lore.LoreBooks;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/**
 * Lesefenster einer losen Lore-Seite: ein herausgerissenes Pergamentblatt mit Buchtitel, Seitenzahl und Text in
 * Tinte. Textur: textures/gui/lore_page.png (Blatt 176x208, erzeugt von tools/assetgen/story2_assets.py).
 */
public class LorePageScreen extends Screen {
    private static final ResourceLocation TEXTURE = Shardline.id("textures/gui/lore_page.png");
    private static final int PAGE_W = 176;
    private static final int PAGE_H = 208;
    private static final int TEXT_X = 20;
    private static final int TEXT_W = 134;
    private static final int INK = 0x2B1840;
    private static final int TITLE = 0x6B3E12;

    private final LoreBooks.LoreBook book;
    private final int page;
    private List<FormattedCharSequence> lines = List.of();
    private int left;
    private int top;

    public LorePageScreen(LoreBooks.LoreBook book, int page) {
        super(Component.translatable("item.shardline.lore_page.named", book.title()));
        this.book = book;
        this.page = page;
    }

    @Override
    protected void init() {
        this.left = (this.width - PAGE_W) / 2;
        this.top = Math.max(4, (this.height - PAGE_H - 24) / 2);
        this.lines = this.font.split(this.book.page(this.page), TEXT_W);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 50, this.top + PAGE_H + 4, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.blit(TEXTURE, this.left, this.top, 0.0F, 0.0F, PAGE_W, PAGE_H, 256, 256);
        Component title = this.book.title().copy().withStyle(Style.EMPTY.withBold(true));
        graphics.drawString(this.font, title, this.left + (PAGE_W - this.font.width(title)) / 2 - 4, this.top + 16, TITLE, false);
        Component pageLabel = Component.translatable("item.shardline.lore_page.page", this.page, this.book.pages());
        graphics.drawString(this.font, pageLabel, this.left + (PAGE_W - this.font.width(pageLabel)) / 2 - 4, this.top + 28, 0x8A6F4E, false);
        int y = this.top + 44;
        for (int i = 0; i < this.lines.size() && i < 14; i++) {
            graphics.drawString(this.font, this.lines.get(i), this.left + TEXT_X, y, INK, false);
            y += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
