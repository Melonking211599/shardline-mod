package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: Inhalt des Auftragsbuchs (QuestJournalScreen). Der Client zeigt nur an, was hier steht.
 *
 * @param open    true: Buch oeffnen (Taste J, Chronik-Tagebuch); false: nur aktualisieren, falls es offen ist
 * @param nextKey Sprachschluessel des naechsten Ziels
 * @param quests  begonnene und abgeschlossene Auftraege
 * @param npcs    getroffene Figuren mit dem passenden Beschreibungsschluessel
 * @param lore    gelesene Hueter-Schriften: Buch-ID und Bitmaske der gelesenen Seiten (Bit 0 = Seite 1)
 */
public record JournalPayload(boolean open, int chapter, String branch, String epilogue, String nextKey, List<Quest> quests,
                             List<Npc> npcs, List<Lore> lore) implements CustomPacketPayload {
    public static final Type<JournalPayload> TYPE = new Type<>(Shardline.id("journal"));

    /** Auftrag: aktueller Schritt (0-basiert) von {@code steps}, Fortschritt {@code count}/{@code needed} im Schritt. */
    public record Quest(String id, String giver, int step, int steps, int count, int needed, boolean active, int completions) {
        static final StreamCodec<ByteBuf, Quest> STREAM_CODEC = StreamCodec.of((buf, quest) -> {
            ByteBufCodecs.STRING_UTF8.encode(buf, quest.id());
            ByteBufCodecs.STRING_UTF8.encode(buf, quest.giver());
            ByteBufCodecs.VAR_INT.encode(buf, quest.step());
            ByteBufCodecs.VAR_INT.encode(buf, quest.steps());
            ByteBufCodecs.VAR_INT.encode(buf, quest.count());
            ByteBufCodecs.VAR_INT.encode(buf, quest.needed());
            ByteBufCodecs.BOOL.encode(buf, quest.active());
            ByteBufCodecs.VAR_INT.encode(buf, quest.completions());
        }, buf -> new Quest(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.VAR_INT.decode(buf)));
    }

    public record Npc(String id, String bioKey) {
        static final StreamCodec<ByteBuf, Npc> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Npc::id, ByteBufCodecs.STRING_UTF8, Npc::bioKey, Npc::new);
    }

    public record Lore(String book, int pages) {
        static final StreamCodec<ByteBuf, Lore> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Lore::book, ByteBufCodecs.VAR_INT, Lore::pages, Lore::new);
    }

    private static final StreamCodec<ByteBuf, List<Quest>> QUESTS = Quest.STREAM_CODEC.apply(ByteBufCodecs.list(256));
    private static final StreamCodec<ByteBuf, List<Npc>> NPCS = Npc.STREAM_CODEC.apply(ByteBufCodecs.list(32));
    private static final StreamCodec<ByteBuf, List<Lore>> LORE = Lore.STREAM_CODEC.apply(ByteBufCodecs.list(64));

    public static final StreamCodec<ByteBuf, JournalPayload> STREAM_CODEC = StreamCodec.of((buf, payload) -> {
        ByteBufCodecs.BOOL.encode(buf, payload.open());
        ByteBufCodecs.VAR_INT.encode(buf, payload.chapter());
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.branch());
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.epilogue());
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.nextKey());
        QUESTS.encode(buf, payload.quests());
        NPCS.encode(buf, payload.npcs());
        LORE.encode(buf, payload.lore());
    }, buf -> new JournalPayload(ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf),
            ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf), QUESTS.decode(buf), NPCS.decode(buf), LORE.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
