package eu.shardline.mod.story.quest;

import eu.shardline.mod.command.ShardlineCommands;
import eu.shardline.mod.network.JournalPayload;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.lore.LoreBooks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/**
 * Schickt dem Client den Inhalt des Auftragsbuchs ({@link JournalPayload}): Kapitel, Auftraege, getroffene Figuren und
 * gelesene Hueter-Schriften. Bei jeder Auftragsaenderung wird ein offenes Buch aktualisiert; mit {@code open} oeffnet
 * es sich (Taste J, Chronik-Tagebuch). Der Client zeigt nur an, was hier gesendet wird.
 */
public final class QuestSync {
    /** Figuren des Lagers in der Reihenfolge des Buchs; getroffen = Flagge {@code <id>:met}. */
    public static final List<String> NPCS = List.of("rhea", "kesh", "ilsa", "veyl");

    private QuestSync() {
    }

    public static void send(ServerPlayer player) {
        send(player, false);
    }

    public static void send(ServerPlayer player, boolean open) {
        if (player.connection != null && player.connection.hasChannel(JournalPayload.TYPE)) {
            StoryManager.send(player, build(player, open));
        }
    }

    public static JournalPayload build(ServerPlayer player, boolean open) {
        ShardlineWorldData data = StoryManager.data(player.server);
        UUID uuid = player.getUUID();
        int chapter = data.chapter(uuid);
        StoryEnding branch = data.branch(uuid);
        String epilogue = data.epilogue(uuid);

        List<JournalPayload.Quest> quests = new ArrayList<>();
        for (Map.Entry<String, QuestProgress> entry : data.quests(uuid).entrySet()) {
            QuestDefinition quest = Quests.get(entry.getKey());
            QuestProgress progress = entry.getValue();
            if (quest == null || quest.steps().isEmpty()) {
                continue;
            }
            int step = Math.min(progress.step(), quest.steps().size() - 1);
            quests.add(new JournalPayload.Quest(quest.id(), quest.giver(), progress.active() ? progress.step() : quest.steps().size(),
                    quest.steps().size(), progress.count(), quest.steps().get(step).count(), progress.active(), progress.completions()));
        }
        // Aktive zuerst, dann nach Name (stabil fuer den Client)
        quests.sort((a, b) -> a.active() != b.active() ? (a.active() ? -1 : 1) : a.id().compareTo(b.id()));

        List<JournalPayload.Npc> npcs = new ArrayList<>();
        for (String npc : NPCS) {
            if (data.hasFlag(uuid, npc + ":met")) {
                npcs.add(new JournalPayload.Npc(npc, bioKey(data, uuid, npc)));
            }
        }

        List<JournalPayload.Lore> lore = new ArrayList<>();
        for (LoreBooks.LoreBook book : LoreBooks.ALL) {
            int mask = 0;
            if (data.hasFlag(uuid, "book:" + book.id())) {
                mask = (1 << book.pages()) - 1;
            } else {
                for (int page = 1; page <= book.pages(); page++) {
                    if (data.hasFlag(uuid, "lore_read:" + book.id() + ":" + page)) {
                        mask |= 1 << (page - 1);
                    }
                }
            }
            if (mask != 0) {
                lore.add(new JournalPayload.Lore(book.id(), mask));
            }
        }
        return new JournalPayload(open, chapter, branch.id(), epilogue, ShardlineCommands.nextKey(chapter, branch, epilogue), quests, npcs, lore);
    }

    /** Beschreibung je nach Lage: Kesh geheilt, Ilsa bei den Stimmlosen (beherrscht), sonst die Grundfassung. */
    private static String bioKey(ShardlineWorldData data, UUID uuid, String npc) {
        String base = "journal.shardline.npc." + npc;
        if (npc.equals("kesh") && data.hasFlag(uuid, "kesh:healed")) {
            return base + ".healed";
        }
        if (npc.equals("ilsa") && data.ending() == StoryEnding.DOMINATED) {
            return base + ".dominate";
        }
        return base;
    }
}
