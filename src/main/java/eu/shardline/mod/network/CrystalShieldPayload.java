package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -&gt; Server: Spieler hat die Taste fuer den Kristallschild gedrueckt.
 */
public record CrystalShieldPayload() implements CustomPacketPayload {
    public static final Type<CrystalShieldPayload> TYPE = new Type<>(Shardline.id("crystal_shield"));
    public static final StreamCodec<ByteBuf, CrystalShieldPayload> STREAM_CODEC = StreamCodec.unit(new CrystalShieldPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
