package eu.shardline.mod.virus;

import eu.shardline.mod.config.ShardlineConfig;
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

    /** Ob die Position im Schutzradius eines Reinheitsankers liegt. */
    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        LongSet anchors = StoryManager.data(level.getServer()).anchors(level.dimension());
        if (anchors.isEmpty()) {
            return false;
        }
        long radius = ShardlineConfig.ANCHOR_RADIUS.get();
        long radiusSqr = radius * radius;
        for (LongIterator iterator = anchors.iterator(); iterator.hasNext(); ) {
            long packed = iterator.nextLong();
            long dx = BlockPos.getX(packed) - pos.getX();
            long dy = BlockPos.getY(packed) - pos.getY();
            long dz = BlockPos.getZ(packed) - pos.getZ();
            if (dx * dx + dy * dy + dz * dz <= radiusSqr) {
                return true;
            }
        }
        return false;
    }

    public static int count(ServerLevel level) {
        return StoryManager.data(level.getServer()).anchors(level.dimension()).size();
    }
}
