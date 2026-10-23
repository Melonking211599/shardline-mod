package eu.shardline.mod.client;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.network.CrystalShieldPayload;
import eu.shardline.mod.world.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-Spielereignisse: Tastendruck fuer den Kristallschild, Portal-Overlay und Nebel der Splitterweite.
 */
@EventBusSubscriber(modid = Shardline.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (KeyBindings.CRYSTAL_SHIELD.consumeClick()) {
            if (minecraft.player != null && minecraft.getConnection() != null) {
                PacketDistributor.sendToServer(new CrystalShieldPayload());
            }
        }
        PortalOverlay.tick(minecraft);
    }

    private static boolean inSplitterweite(Minecraft minecraft) {
        return minecraft.level != null && minecraft.level.dimension() == ModDimensions.SPLITTERWEITE_LEVEL;
    }

    @SubscribeEvent
    static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!inSplitterweite(minecraft) || !ShardlineClientConfig.DENSE_FOG.get() || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }
        float distance = Math.min(event.getFarPlaneDistance(), ShardlineClientConfig.FOG_DISTANCE.get());
        event.setNearPlaneDistance(distance * 0.2F);
        event.setFarPlaneDistance(distance);
        event.setCanceled(true);
    }

    @SubscribeEvent
    static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (inSplitterweite(Minecraft.getInstance())) {
            event.setRed(event.getRed() * 0.6F + 0.06F);
            event.setGreen(event.getGreen() * 0.5F + 0.03F);
            event.setBlue(event.getBlue() * 0.7F + 0.1F);
        }
    }
}
