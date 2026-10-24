package eu.shardline.mod.virus;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.story.StoryManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Der Virus in der Oberwelt: Nach dem Bruch kriecht er durch die Portale und setzt in festen Abstaenden neue
 * Infektionsherde in der Naehe eines zufaelligen Spielers in der Oberwelt ({@code virus.overworldSeedMinutes}).
 * Gesaet wird nur in geladenen Chunks; Reinheitsanker schuetzen wie ueberall. Die Ausbreitung selbst laeuft danach
 * ueber die Random Ticks der infizierten Bloecke und die Warteschlange von {@link VirusSpreadManager}.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class VirusDrift {
    private static final int MIN_DISTANCE = 24;
    private static final int MAX_DISTANCE = 64;
    private static long nextSeedTick = -1L;

    private VirusDrift() {
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        int minutes = ShardlineConfig.VIRUS_OVERWORLD_SEED_MINUTES.get();
        if (minutes <= 0 || !StoryManager.isSpreading(server)) {
            nextSeedTick = -1L;
            return;
        }
        long now = server.getTickCount();
        if (nextSeedTick < 0L) {
            nextSeedTick = now + minutes * 1200L;
            return;
        }
        if (now < nextSeedTick) {
            return;
        }
        nextSeedTick = now + minutes * 1200L;
        ServerLevel overworld = server.overworld();
        List<ServerPlayer> players = overworld.players().stream().filter(player -> !player.isSpectator()).toList();
        if (!players.isEmpty()) {
            ServerPlayer player = players.get(overworld.getRandom().nextInt(players.size()));
            seedNear(overworld, player.blockPosition(), overworld.getRandom());
        }
    }

    /** Neuer Infektionsherd 24-64 Bloecke von der Position entfernt (an der Oberflaeche). */
    public static void seedNear(ServerLevel level, BlockPos around, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE + 1);
        BlockPos spot = around.offset((int) (Math.cos(angle) * distance), 0, (int) (Math.sin(angle) * distance));
        if (PurityAnchors.isProtected(level, spot)) {
            return;
        }
        StoryManager.data(level.getServer()).addHerd(level.dimension(), spot, VirusSpreadManager.HERD_SPACING);
        VirusSpreadManager.seedAround(level, spot, 5, 32, random);
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        nextSeedTick = -1L;
    }
}
