package eu.shardline.mod.client.story;

import eu.shardline.mod.network.JournalPayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;

/** Letzter Stand des Auftragsbuchs vom Server; oeffnet das Buch oder aktualisiert ein offenes. */
public final class ClientJournal {
    @Nullable
    private static JournalPayload current;

    private ClientJournal() {
    }

    public static void receive(JournalPayload payload) {
        current = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof QuestJournalScreen screen) {
            screen.refresh(payload);
        } else if (payload.open() && minecraft.player != null) {
            minecraft.setScreen(new QuestJournalScreen(payload));
        }
    }

    @Nullable
    public static JournalPayload current() {
        return current;
    }

    /** Beim Verlassen einer Welt. */
    public static void reset() {
        current = null;
    }
}
