package eu.shardline.mod.virus;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryManager;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Abfragen zu den Reinheitsankern einer Dimension. Die Positionen stehen in
 * {@link eu.shardline.mod.story.ShardlineWorldData} und werden vom Ankerblock selbst gepflegt.
 */
public final class PurityAnchors {
    private PurityAnchors() {
    }

    /** Ob die Position im (wachsenden) Schutzradius eines Reinheitsankers liegt. */
    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        ShardlineWorldData data = StoryManager.data(level.getServer());
        LongSet anchors = data.anchors(level.dimension());
        if (anchors.isEmpty()) {
            return false;
        }
        long now = level.getGameTime();
        for (LongIterator iterator = anchors.iterator(); iterator.hasNext(); ) {
            long packed = iterator.nextLong();
            double radius = radius(data.anchorSince(level.dimension(), packed), now);
            long dx = BlockPos.getX(packed) - pos.getX();
            long dy = BlockPos.getY(packed) - pos.getY();
            long dz = BlockPos.getZ(packed) - pos.getZ();
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    /** Aktueller Schutzradius eines Ankers an dieser Position. */
    public static double radius(ServerLevel level, BlockPos anchor) {
        return radius(StoryManager.data(level.getServer()).anchorSince(level.dimension(), anchor.asLong()), level.getGameTime());
    }

    /** Schutzradius nach der Config: vom Startradius linear bis zum vollen Radius; ohne Zeitstempel (alte Anker) voll. */
    public static double radius(long since, long now) {
        return radius(since, now, ShardlineConfig.ANCHOR_START_RADIUS.get(), ShardlineConfig.ANCHOR_RADIUS.get(),
                ShardlineConfig.ANCHOR_GROW_SECONDS.get() * 20L);
    }

    /** Gemeinsame Formel fuer Server und Client (der Renderer bekommt die Werte ueber den Block Entity). */
    public static double radius(long since, long now, int start, int max, long growTicks) {
        if (since < 0L || growTicks <= 0L || max <= start) {
            return Math.max(start, max);
        }
        double progress = Math.min(1.0, Math.max(0.0, (now - since) / (double) growTicks));
        return start + (max - start) * progress;
    }

    public static int count(ServerLevel level) {
        return StoryManager.data(level.getServer()).anchors(level.dimension()).size();
    }
}
