package eu.shardline.mod.story.quest;

import net.minecraft.server.level.ServerPlayer;

/**
 * Schickt dem Client den Auftragsstand fuer das Auftragsbuch. Der Client zeigt nur an, was hier gesendet wird.
 * Das Paket kommt mit dem Auftragsbuch (QuestJournalScreen); bis dahin ist dies der einzige Aufrufpunkt.
 */
public final class QuestSync {
    private QuestSync() {
    }

    public static void send(ServerPlayer player) {
    }
}
