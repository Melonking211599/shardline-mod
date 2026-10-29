package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: oeffnet das Lesefenster einer losen Lore-Seite (Buch-ID und Seitenzahl; den Text holt der
 * Client aus seinen Sprachdateien).
 */
public record OpenLorePagePayload(String book, int page) implements CustomPacketPayload {
    public static final Type<OpenLorePagePayload> TYPE = new Type<>(Shardline.id("open_lore_page"));
    public static final StreamCodec<ByteBuf, OpenLorePagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenLorePagePayload::book,
            ByteBufCodecs.VAR_INT, OpenLorePagePayload::page,
            OpenLorePagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
