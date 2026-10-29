package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -&gt; Client: Ein Dialogknoten. Der Server hat Startknoten und Bedingungen bereits ausgewertet; der Client zeigt
 * nur an. Texte sind uebersetzbare Components (jeder Client in seiner Sprache).
 *
 * @param session  Sitzungsnummer, die der Client bei jeder Antwort zurueckschickt
 * @param entityId sprechendes Wesen oder -1 (Befehl/Test)
 * @param speaker  NPC-ID (Name {@code npc.shardline.<id>}, Porträt {@code gui/portraits/<id>_<pose>.png})
 * @param portrait Pose des Porträts
 * @param text     Seiten des Textes
 * @param choices  Antworten (leer = "Weiter" bzw. "Schliessen")
 * @param ends     Knoten ohne Antworten, nach dem der Dialog endet
 */
public record OpenDialoguePayload(int session, int entityId, String speaker, String portrait, List<Component> text, List<Choice> choices,
                                  boolean ends) implements CustomPacketPayload {
    public static final Type<OpenDialoguePayload> TYPE = new Type<>(Shardline.id("open_dialogue"));

    /** Eine Antwort: Text, frei oder gesperrt, Hinweis fuer gesperrte Antworten. */
    public record Choice(Component text, boolean enabled, Component hint) {
        static final StreamCodec<RegistryFriendlyByteBuf, Choice> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, Choice::text,
                ByteBufCodecs.BOOL, Choice::enabled,
                ComponentSerialization.STREAM_CODEC, Choice::hint,
                Choice::new);
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Component>> TEXT = ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(16));
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Choice>> CHOICES = Choice.STREAM_CODEC.apply(ByteBufCodecs.list(8));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialoguePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.session());
                ByteBufCodecs.VAR_INT.encode(buf, payload.entityId());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.speaker());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.portrait());
                TEXT.encode(buf, payload.text());
                CHOICES.encode(buf, payload.choices());
                ByteBufCodecs.BOOL.encode(buf, payload.ends());
            },
            buf -> new OpenDialoguePayload(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf), TEXT.decode(buf), CHOICES.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
