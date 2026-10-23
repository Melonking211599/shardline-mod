package eu.shardline.mod.network;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.event.CrystalShieldHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registriert die Netzwerkpakete der Mod (Mod-Event-Bus).
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(CrystalShieldPayload.TYPE, CrystalShieldPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        CrystalShieldHandler.tryActivate(player);
                    }
                }));
    }
}
