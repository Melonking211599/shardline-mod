package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: oeffnet die Endentscheidung (Versiegeln / Beherrschen).
 * Bei {@code vote} laeuft eine Server-Abstimmung mit {@code secondsLeft} Restzeit.
 */
public record OpenEndingChoicePayload(boolean vote, int secondsLeft) implements CustomPacketPayload {
    public static final Type<OpenEndingChoicePayload> TYPE = new Type<>(Shardline.id("open_ending_choice"));
    public static final StreamCodec<ByteBuf, OpenEndingChoicePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OpenEndingChoicePayload::vote,
            ByteBufCodecs.VAR_INT, OpenEndingChoicePayload::secondsLeft,
            OpenEndingChoicePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
