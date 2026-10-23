package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: startet einen Bildschirmeffekt (Ausbruch, Versiegelung, Herrschaft, Vision).
 */
public record ScreenEffectPayload(int effect, int duration) implements CustomPacketPayload {
    public static final int OUTBREAK = 0;
    public static final int SEALED = 1;
    public static final int DOMINATED = 2;
    public static final int VISION = 3;
    public static final int PURIFY = 4;

    public static final Type<ScreenEffectPayload> TYPE = new Type<>(Shardline.id("screen_effect"));
    public static final StreamCodec<ByteBuf, ScreenEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ScreenEffectPayload::effect,
            ByteBufCodecs.VAR_INT, ScreenEffectPayload::duration,
            ScreenEffectPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
