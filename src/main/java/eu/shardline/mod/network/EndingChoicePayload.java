package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -&gt; Server: Wahl in der Endentscheidung ("seal" oder "dominate").
 */
public record EndingChoicePayload(String choice) implements CustomPacketPayload {
    public static final Type<EndingChoicePayload> TYPE = new Type<>(Shardline.id("ending_choice"));
    public static final StreamCodec<ByteBuf, EndingChoicePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(16), EndingChoicePayload::choice,
            EndingChoicePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
