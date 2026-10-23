package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; alle Clients: wer den Titel "Hueter" (Umhang) bzw. "Splitterherrscher" traegt.
 */
public record TitlesPayload(List<UUID> keepers, List<UUID> rulers) implements CustomPacketPayload {
    public static final Type<TitlesPayload> TYPE = new Type<>(Shardline.id("titles"));
    public static final StreamCodec<ByteBuf, TitlesPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), TitlesPayload::keepers,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), TitlesPayload::rulers,
            TitlesPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
