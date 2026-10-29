package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -&gt; Server: Taste J gedrueckt; der Server antwortet mit {@link JournalPayload} (open = true). */
public record OpenJournalPayload() implements CustomPacketPayload {
    public static final Type<OpenJournalPayload> TYPE = new Type<>(Shardline.id("open_journal"));
    public static final StreamCodec<ByteBuf, OpenJournalPayload> STREAM_CODEC = StreamCodec.unit(new OpenJournalPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
