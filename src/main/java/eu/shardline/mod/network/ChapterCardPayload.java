package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: Ein neues Kapitel wurde erreicht; der Client blendet die Kapitelkarte ein
 * (Emblem, "Kapitel n", Titel, Zusammenfassung). Ersetzt die Vanilla-Titelzeilen beim Kapitelwechsel.
 */
public record ChapterCardPayload(int chapter) implements CustomPacketPayload {
    public static final Type<ChapterCardPayload> TYPE = new Type<>(Shardline.id("chapter_card"));
    public static final StreamCodec<ByteBuf, ChapterCardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChapterCardPayload::chapter,
            ChapterCardPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
