package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -&gt; Server: Splitterherrscher-Faehigkeit per Taste ausgeloest.
 */
public record AbilityPayload(int ability) implements CustomPacketPayload {
    public static final int SHARD_CALL = 0;
    public static final int CRYSTAL_WAVE = 1;

    public static final Type<AbilityPayload> TYPE = new Type<>(Shardline.id("ability"));
    public static final StreamCodec<ByteBuf, AbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AbilityPayload::ability,
            AbilityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
