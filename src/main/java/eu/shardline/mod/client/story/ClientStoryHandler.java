package eu.shardline.mod.client.story;

import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.network.ChapterCardPayload;
import eu.shardline.mod.network.OpenEndingChoicePayload;
import eu.shardline.mod.network.OpenLorePagePayload;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.network.StoryStatePayload;
import eu.shardline.mod.network.TitlesPayload;
import eu.shardline.mod.network.TraderDialoguePayload;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.lore.LoreBooks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

/**
 * Verarbeitet die Story-Pakete des Servers auf dem Client (Hauptthread).
 */
public final class ClientStoryHandler {
    private ClientStoryHandler() {
    }

    public static void handleState(StoryStatePayload payload) {
        StoryClientState.update(payload.chapter(), payload.infected(), payload.paused(), StoryEnding.byId(payload.ending()), payload.ruler());
    }

    public static void handleTitles(TitlesPayload payload) {
        StoryClientState.updateTitles(payload.keepers(), payload.rulers());
    }

    public static void handleScreenEffect(ScreenEffectPayload payload) {
        StoryScreenEffects.start(payload.effect(), payload.duration());
    }

    public static void openEndingChoice(OpenEndingChoicePayload payload) {
        Minecraft.getInstance().setScreen(new EndingChoiceScreen(payload.vote(), payload.secondsLeft()));
    }

    /** Kapitelkarte einblenden; ist sie abgeschaltet, die schlichten Vanilla-Titel zeigen. */
    public static void showChapterCard(ChapterCardPayload payload) {
        // payload.chapter() ist ein Emblem-Wert: Kapitel plus Zweig (StoryChapter#emblemValue)
        StoryChapter chapter = StoryChapter.byEmblemValue(payload.chapter());
        if (chapter == null) {
            return;
        }
        if (ShardlineClientConfig.CHAPTER_CARD.get()) {
            ChapterCardOverlay.show(payload.chapter());
        } else {
            Minecraft.getInstance().gui.setTimes(15, 70, 25);
            Minecraft.getInstance().gui.setSubtitle(chapter.title(StoryChapter.branchOfEmblemValue(payload.chapter()))
                    .copy().withStyle(ChatFormatting.LIGHT_PURPLE));
            Minecraft.getInstance().gui.setTitle(chapter.label().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }
    }

    public static void openLorePage(OpenLorePagePayload payload) {
        LoreBooks.LoreBook book = LoreBooks.byId(payload.book());
        if (book != null) {
            Minecraft.getInstance().setScreen(new LorePageScreen(book, Math.max(1, Math.min(book.pages(), payload.page()))));
        }
    }

    public static void openTraderDialogue(TraderDialoguePayload payload) {
        Minecraft.getInstance().setScreen(new TraderDialogueScreen(payload));
    }
}
