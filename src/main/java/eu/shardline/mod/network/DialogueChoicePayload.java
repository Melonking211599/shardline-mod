package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -&gt; Server: Antwort im Dialog. {@code index} ist die Position in der zuletzt gesendeten Antwortliste
 * (0 = "Weiter" bei Knoten ohne Antworten). Der Server prueft Sitzung, Abstand und Bedingungen erneut.
 */
public record DialogueChoicePayload(int session, int index) implements CustomPacketPayload {
    public static final Type<DialogueChoicePayload> TYPE = new Type<>(Shardline.id("dialogue_choice"));
    public static final StreamCodec<ByteBuf, DialogueChoicePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DialogueChoicePayload::session,
            ByteBufCodecs.VAR_INT, DialogueChoicePayload::index,
            DialogueChoicePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
