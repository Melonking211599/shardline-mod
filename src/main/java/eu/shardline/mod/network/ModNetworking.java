package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.client.story.ClientDialogue;
import eu.shardline.mod.client.story.ClientStoryHandler;
import eu.shardline.mod.event.CrystalShieldHandler;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.dialogue.DialogueSessions;
import eu.shardline.mod.story.ending.EndingManager;
import eu.shardline.mod.story.ending.RulerAbilities;
import eu.shardline.mod.story.lore.TraderDialogue;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registriert die Netzwerkpakete der Mod (Mod-Event-Bus). Client-Pakete werden ueber Lambdas an
 * {@link ClientStoryHandler} weitergereicht, damit der Server diese Klasse nie laedt.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "5";

    private ModNetworking() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Client -> Server
        registrar.playToServer(CrystalShieldPayload.TYPE, CrystalShieldPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        CrystalShieldHandler.tryActivate(player);
                    }
                }));
        registrar.playToServer(AbilityPayload.TYPE, AbilityPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        if (payload.ability() == AbilityPayload.SHARD_CALL) {
                            RulerAbilities.shardCall(player);
                        } else if (payload.ability() == AbilityPayload.CRYSTAL_WAVE) {
                            RulerAbilities.crystalWave(player);
                        }
                    }
                }));
        registrar.playToServer(EndingChoicePayload.TYPE, EndingChoicePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        EndingManager.onChoice(player, StoryEnding.byId(payload.choice()));
                    }
                }));

        registrar.playToServer(TraderActionPayload.TYPE, TraderActionPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        TraderDialogue.handleAction(player, payload.entityId(), payload.action());
                    }
                }));

        registrar.playToServer(DialogueChoicePayload.TYPE, DialogueChoicePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        DialogueSessions.onChoice(player, payload.session(), payload.index());
                    }
                }));
        registrar.playBidirectional(CloseDialoguePayload.TYPE, CloseDialoguePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        DialogueSessions.onClientClose(player, payload.session());
                    } else {
                        ClientDialogue.close(payload);
                    }
                }));

        // Server -> Client
        registrar.playToClient(OpenDialoguePayload.TYPE, OpenDialoguePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientDialogue.open(payload)));
        registrar.playToClient(StoryStatePayload.TYPE, StoryStatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.handleState(payload)));
        registrar.playToClient(TitlesPayload.TYPE, TitlesPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.handleTitles(payload)));
        registrar.playToClient(ScreenEffectPayload.TYPE, ScreenEffectPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.handleScreenEffect(payload)));
        registrar.playToClient(OpenEndingChoicePayload.TYPE, OpenEndingChoicePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.openEndingChoice(payload)));
        registrar.playToClient(ChapterCardPayload.TYPE, ChapterCardPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.showChapterCard(payload)));
        registrar.playToClient(OpenLorePagePayload.TYPE, OpenLorePagePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.openLorePage(payload)));
        registrar.playToClient(TraderDialoguePayload.TYPE, TraderDialoguePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.openTraderDialogue(payload)));
    }
}
