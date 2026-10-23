package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.client.story.ClientStoryHandler;
import eu.shardline.mod.event.CrystalShieldHandler;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.ending.EndingManager;
import eu.shardline.mod.story.ending.RulerAbilities;
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
    private static final String PROTOCOL_VERSION = "2";

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

        // Server -> Client
        registrar.playToClient(StoryStatePayload.TYPE, StoryStatePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.handleState(payload)));
        registrar.playToClient(TitlesPayload.TYPE, TitlesPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.handleTitles(payload)));
        registrar.playToClient(ScreenEffectPayload.TYPE, ScreenEffectPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.handleScreenEffect(payload)));
        registrar.playToClient(OpenEndingChoicePayload.TYPE, OpenEndingChoicePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientStoryHandler.openEndingChoice(payload)));
    }
}
