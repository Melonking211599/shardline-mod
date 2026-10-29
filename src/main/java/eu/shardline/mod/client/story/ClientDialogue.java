package eu.shardline.mod.client.story;

import eu.shardline.mod.network.CloseDialoguePayload;
import eu.shardline.mod.network.DialogueChoicePayload;
import eu.shardline.mod.network.OpenDialoguePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-Seite der Dialoge: nimmt Knoten vom Server an, oeffnet oder aktualisiert {@link DialogueScreen} und fuehrt
 * den Verlauf der laufenden Sitzung. Haelt keinen Spielzustand; jede Entscheidung trifft der Server.
 */
public final class ClientDialogue {
    /** Ein Eintrag im Verlauf: Sprecher (oder leer bei eigenen Antworten) und Text. */
    public record HistoryEntry(Component speaker, Component text, boolean ownChoice) {
    }

    private static final List<HistoryEntry> HISTORY = new ArrayList<>();
    private static int session = -1;

    private ClientDialogue() {
    }

    public static void open(OpenDialoguePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.session() != session) {
            HISTORY.clear();
            session = payload.session();
        }
        Component speaker = Component.translatable("npc.shardline." + payload.speaker());
        payload.text().forEach(page -> HISTORY.add(new HistoryEntry(speaker, page, false)));
        if (minecraft.screen instanceof DialogueScreen screen && screen.session() == payload.session()) {
            screen.show(payload);
        } else {
            minecraft.setScreen(new DialogueScreen(payload));
        }
    }

    public static void close(CloseDialoguePayload payload) {
        if (Minecraft.getInstance().screen instanceof DialogueScreen screen && screen.session() == payload.session()) {
            screen.closeFromServer();
        }
    }

    static void choose(int sessionId, int index, Component chosen) {
        if (chosen != null) {
            HISTORY.add(new HistoryEntry(Component.empty(), chosen, true));
        }
        PacketDistributor.sendToServer(new DialogueChoicePayload(sessionId, index));
    }

    static void sendClose(int sessionId) {
        PacketDistributor.sendToServer(new CloseDialoguePayload(sessionId));
    }

    static List<HistoryEntry> history() {
        return HISTORY;
    }
}
