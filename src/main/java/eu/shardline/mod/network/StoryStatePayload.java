package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: Story-Zustand fuer den eigenen Spieler (Kapitel, Virus, Ende, Herrscher-Status).
 * Steuert Himmel, Nebel, Musik und die Anzeige der Faehigkeiten.
 */
public record StoryStatePayload(int chapter, boolean infected, boolean paused, String ending, boolean ruler) implements CustomPacketPayload {
    public static final Type<StoryStatePayload> TYPE = new Type<>(Shardline.id("story_state"));
    public static final StreamCodec<ByteBuf, StoryStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StoryStatePayload::chapter,
            ByteBufCodecs.BOOL, StoryStatePayload::infected,
            ByteBufCodecs.BOOL, StoryStatePayload::paused,
            ByteBufCodecs.STRING_UTF8, StoryStatePayload::ending,
            ByteBufCodecs.BOOL, StoryStatePayload::ruler,
            StoryStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
