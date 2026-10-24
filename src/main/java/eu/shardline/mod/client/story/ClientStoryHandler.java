package eu.shardline.mod.client.story;

import eu.shardline.mod.network.OpenEndingChoicePayload;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.network.StoryStatePayload;
import eu.shardline.mod.network.TitlesPayload;
import eu.shardline.mod.network.TraderDialoguePayload;
import eu.shardline.mod.story.StoryEnding;
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

    public static void openTraderDialogue(TraderDialoguePayload payload) {
        Minecraft.getInstance().setScreen(new TraderDialogueScreen(payload));
    }
}
