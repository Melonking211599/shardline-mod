package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -&gt; Server: Knopf im Dialogfenster des Splitterhaendlers. Der Server prueft Entfernung und Zustand selbst.
 */
public record TraderActionPayload(int entityId, int action) implements CustomPacketPayload {
    public static final int TRADE = 0;
    public static final int LORE = 1;
    public static final int MORE = 2;
    public static final int SELL_PAGES = 3;
    public static final int BIND_BOOK = 4;
    public static final int BACK = 5;
    public static final Type<TraderActionPayload> TYPE = new Type<>(Shardline.id("trader_action"));
    public static final StreamCodec<ByteBuf, TraderActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TraderActionPayload::entityId,
            ByteBufCodecs.VAR_INT, TraderActionPayload::action,
            TraderActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
