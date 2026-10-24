package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: oeffnet bzw. aktualisiert das Dialogfenster des Splitterhaendlers.
 *
 * @param entityId Haendler (fuer Name und Rueckmeldungen)
 * @param mode     {@link #MAIN} (Gespraech) oder {@link #LORE} (lose Seiten)
 * @param lines    Uebersetzungsschluessel der Zeilen, die der Haendler sagt
 * @param pages    lose Lore-Seiten im Inventar des Spielers
 * @param bindable Buecher, fuer die der Spieler vier Seiten hat
 */
public record TraderDialoguePayload(int entityId, int mode, List<String> lines, int pages, int bindable) implements CustomPacketPayload {
    public static final int MAIN = 0;
    public static final int LORE = 1;
    public static final Type<TraderDialoguePayload> TYPE = new Type<>(Shardline.id("trader_dialogue"));
    public static final StreamCodec<ByteBuf, TraderDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TraderDialoguePayload::entityId,
            ByteBufCodecs.VAR_INT, TraderDialoguePayload::mode,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(16)), TraderDialoguePayload::lines,
            ByteBufCodecs.VAR_INT, TraderDialoguePayload::pages,
            ByteBufCodecs.VAR_INT, TraderDialoguePayload::bindable,
            TraderDialoguePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
