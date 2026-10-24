package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * In beide Richtungen: Dialog beenden. Der Client schickt es bei Esc, der Server bei Schaden, Tod, zu grossem Abstand,
 * verschwundenem NPC oder Dialogende.
 */
public record CloseDialoguePayload(int session) implements CustomPacketPayload {
    public static final Type<CloseDialoguePayload> TYPE = new Type<>(Shardline.id("close_dialogue"));
    public static final StreamCodec<ByteBuf, CloseDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CloseDialoguePayload::session,
            CloseDialoguePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
