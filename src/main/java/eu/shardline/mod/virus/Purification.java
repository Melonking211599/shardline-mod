package eu.shardline.mod.virus;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.story.StoryManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Reinigt infizierte Bloecke in einer Kugel. Kleine Radien werden sofort bearbeitet, grosse als
 * Hintergrundauftrag: pro Tick nur eine begrenzte Zahl Chunk-Abschnitte, nur geladene Chunks,
 * und Abschnitte ohne infizierte Bloecke werden ueber die Palette uebersprungen.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class Purification {
    /** Bis zu diesem Radius wird sofort gereinigt. */
    private static final int INSTANT_RADIUS = 8;
    /** Chunk-Abschnitte (16x16x16), die pro Tick und Dimension durchsucht werden. */
    private static final int SECTIONS_PER_TICK = 12;
    private static final List<Job> JOBS = new ArrayList<>();

    private Purification() {
    }

    /**
     * Startet eine Reinigung.
     *
     * @param onDone erhaelt die Zahl gereinigter Bloecke (kann {@code null} sein)
     */
    public static void purifySphere(ServerLevel level, BlockPos center, int radius, @Nullable IntConsumer onDone) {
        StoryManager.data(level.getServer()).removeHerdsNear(level.dimension(), center, radius);
        if (radius <= INSTANT_RADIUS) {
            int purified = purifyNow(level, center, radius);
            if (onDone != null) {
                onDone.accept(purified);
            }
            return;
        }
        JOBS.add(new Job(level, center.immutable(), -1, radius, onDone));
    }

    /**
     * Reinigt eine Kugelschale (innerer Radius ausgenommen), etwa die neu hinzugekommene Zone eines wachsenden
     * Reinheitsankers. Gleiche Regeln wie {@link #purifySphere}: kleine Schalen sofort, grosse im Hintergrund.
     */
    public static void purifyShell(ServerLevel level, BlockPos center, int inner, int outer, @Nullable IntConsumer onDone) {
        if (outer <= INSTANT_RADIUS) {
            int purified = purifyNow(level, center, inner, outer);
            if (onDone != null) {
                onDone.accept(purified);
            }
            return;
        }
        JOBS.add(new Job(level, center.immutable(), inner, outer, onDone));
    }

    private static int purifyNow(ServerLevel level, BlockPos center, int radius) {
        return purifyNow(level, center, -1, radius);
    }

    private static int purifyNow(ServerLevel level, BlockPos center, int inner, int radius) {
        int purified = 0;
        long radiusSqr = (long) radius * radius;
        long innerSqr = inner < 0 ? -1L : (long) inner * inner;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            long d = (long) pos.distSqr(center);
            if (d > radiusSqr || d <= innerSqr) {
                continue;
            }
            BlockState state = InfectionMap.stateIfLoaded(level, pos);
            if (state != null && InfectionMap.isInfected(state) && VirusSpreadManager.purify(level, pos.immutable(), state)) {
                purified++;
            }
        }
        return purified;
    }

    public static int runningJobs() {
        return JOBS.size();
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || JOBS.isEmpty()) {
            return;
        }
        int budget = SECTIONS_PER_TICK;
        for (Iterator<Job> iterator = JOBS.iterator(); iterator.hasNext() && budget > 0; ) {
            Job job = iterator.next();
            if (job.level != level) {
                continue;
            }
            budget = job.work(budget);
            if (job.isDone()) {
                iterator.remove();
                if (job.onDone != null) {
                    job.onDone.accept(job.purified);
                }
            }
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        JOBS.clear();
    }

    /** Hintergrundauftrag: geht die Chunks der Kugel von innen nach aussen durch. */
    private static final class Job {
        final ServerLevel level;
        final BlockPos center;
        final int radius;
        final long radiusSqr;
        /** Innen liegender, bereits sauberer Bereich (-1 = keiner). */
        final long innerSqr;
        @Nullable
        final IntConsumer onDone;
        final List<ChunkPos> chunks = new ArrayList<>();
        final int minSection;
        final int maxSection;
        int chunkIndex;
        int sectionIndex;
        int purified;

        Job(ServerLevel level, BlockPos center, int inner, int radius, @Nullable IntConsumer onDone) {
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.radiusSqr = (long) radius * radius;
            this.innerSqr = inner < 0 ? -1L : (long) inner * inner;
            this.onDone = onDone;
            int minChunkX = (center.getX() - radius) >> 4;
            int maxChunkX = (center.getX() + radius) >> 4;
            int minChunkZ = (center.getZ() - radius) >> 4;
            int maxChunkZ = (center.getZ() + radius) >> 4;
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    this.chunks.add(new ChunkPos(cx, cz));
                }
            }
            ChunkPos centerChunk = new ChunkPos(center);
            this.chunks.sort(Comparator.comparingLong(chunk -> chunk.distanceSquared(centerChunk)));
            this.minSection = level.getSectionIndex(Math.max(level.getMinBuildHeight(), center.getY() - radius));
            this.maxSection = level.getSectionIndex(Math.min(level.getMaxBuildHeight() - 1, center.getY() + radius));
            this.sectionIndex = this.minSection;
        }

        boolean isDone() {
            return this.chunkIndex >= this.chunks.size();
        }

        /** Arbeitet bis das Budget verbraucht ist. @return verbleibendes Budget */
        int work(int budget) {
            while (budget > 0 && !this.isDone()) {
                ChunkPos chunkPos = this.chunks.get(this.chunkIndex);
                LevelChunk chunk = this.level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
                if (chunk == null) {
                    this.nextChunk();
                    continue;
                }
                LevelChunkSection section = chunk.getSection(this.sectionIndex);
                if (!section.hasOnlyAir() && section.maybeHas(InfectionMap::isInfected)) {
                    this.purifySection(chunk, chunkPos, this.sectionIndex);
                    budget--;
                }
                this.sectionIndex++;
                if (this.sectionIndex > this.maxSection) {
                    this.nextChunk();
                }
            }
            return budget;
        }

        private void nextChunk() {
            this.chunkIndex++;
            this.sectionIndex = this.minSection;
        }

        private void purifySection(LevelChunk chunk, ChunkPos chunkPos, int sectionIndex) {
            int baseY = this.level.getSectionYFromSectionIndex(sectionIndex) << 4;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        pos.set(chunkPos.getMinBlockX() + x, baseY + y, chunkPos.getMinBlockZ() + z);
                        double d = pos.distSqr(this.center);
                        if (d > this.radiusSqr || d <= this.innerSqr) {
                            continue;
                        }
                        BlockState state = chunk.getBlockState(pos);
                        if (InfectionMap.isInfected(state) && VirusSpreadManager.purify(this.level, pos.immutable(), state)) {
                            this.purified++;
                        }
                    }
                }
            }
        }
    }
}
