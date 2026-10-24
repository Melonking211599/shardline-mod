package eu.shardline.mod.virus;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Ausbreitung des Shardvirus (nur Server).
 *
 * <p>Infizierte Bloecke, Shard-Erz, Splitterportale in der Oberwelt und infizierte Mobs schlagen
 * bei ihren Random Ticks Zielpositionen vor. Diese landen in einer Warteschlange pro Dimension,
 * gruppiert nach Chunk. Am Tickende werden hoechstens {@code maxBlocksPerTick} Positionen
 * Chunk fuer Chunk abgearbeitet; Chunks, die inzwischen entladen sind, werden verworfen.
 * Random Ticks gibt es nur in geladenen Chunks nahe Spielern - die Ausbreitung laeuft also
 * nie in ungeladenen Gebieten.</p>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class VirusSpreadManager {
    private static final Map<ResourceKey<Level>, Queue> QUEUES = new HashMap<>();
    /** Abstand zwischen zwei gespeicherten Infektionsherden. */
    public static final int HERD_SPACING = 48;

    private VirusSpreadManager() {
    }

    /** Wartende Umwandlungen einer Dimension, gruppiert nach Chunk. */
    static final class Queue {
        final Long2ObjectLinkedOpenHashMap<LongArrayList> byChunk = new Long2ObjectLinkedOpenHashMap<>();
        final LongSet queued = new LongOpenHashSet();

        boolean add(BlockPos pos, int limit) {
            long packed = pos.asLong();
            if (this.queued.size() >= limit || !this.queued.add(packed)) {
                return false;
            }
            long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
            LongArrayList list = this.byChunk.get(chunk);
            if (list == null) {
                list = new LongArrayList();
                this.byChunk.put(chunk, list);
            }
            list.add(packed);
            return true;
        }

        int size() {
            return this.queued.size();
        }

        void clear() {
            this.byChunk.clear();
            this.queued.clear();
        }
    }

    private static Queue queue(ServerLevel level) {
        return QUEUES.computeIfAbsent(level.dimension(), key -> new Queue());
    }

    public static int queuedCount(ServerLevel level) {
        Queue queue = QUEUES.get(level.dimension());
        return queue == null ? 0 : queue.size();
    }

    public static void clearQueues() {
        QUEUES.values().forEach(Queue::clear);
    }

    /** Schlaegt eine Position zur Infektion vor (wird spaeter geprueft). */
    public static boolean queueInfection(ServerLevel level, BlockPos pos) {
        return queue(level).add(pos.immutable(), ShardlineConfig.VIRUS_QUEUE_LIMIT.get());
    }

    // ------------------------------------------------------------------ Quellen

    /** Random Tick eines infizierten Blocks (Shardvirus). */
    public static void spreadFrom(ServerLevel level, BlockPos source, RandomSource random) {
        spreadFrom(level, source, random, VirusStrain.SHARD);
    }

    /** Random Tick eines infizierten Blocks; der Stamm veraendert die Ausbreitungschance. */
    public static void spreadFrom(ServerLevel level, BlockPos source, RandomSource random, VirusStrain strain) {
        if (!StoryManager.isSpreading(level.getServer())
                || random.nextDouble() >= ShardlineConfig.VIRUS_SPREAD_CHANCE.get() * strain.spreadMultiplier()) {
            return;
        }
        int range = ShardlineConfig.VIRUS_SPREAD_RANGE.get();
        int attempts = ShardlineConfig.VIRUS_SPREAD_ATTEMPTS.get();
        for (int i = 0; i < attempts; i++) {
            BlockPos target = source.offset(random.nextIntBetweenInclusive(-range, range),
                    random.nextIntBetweenInclusive(-range, range), random.nextIntBetweenInclusive(-range, range));
            queueInfection(level, target);
        }
    }

    /**
     * Neue Infektionsstellen rund um eine Quelle (Erz, Portal, Ausbruchsort). Zielt bevorzugt
     * auf die Oberflaeche, laedt dabei aber keine Chunks.
     */
    public static void seedAround(ServerLevel level, BlockPos center, int radius, int count, RandomSource random) {
        for (int i = 0; i < count; i++) {
            int x = center.getX() + random.nextIntBetweenInclusive(-radius, radius);
            int z = center.getZ() + random.nextIntBetweenInclusive(-radius, radius);
            LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
            if (chunk == null) {
                continue;
            }
            int surface = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15);
            int y = Math.abs(surface - center.getY()) <= radius + 2
                    ? surface
                    : center.getY() + random.nextIntBetweenInclusive(-radius, radius);
            queueInfection(level, new BlockPos(x, y, z));
        }
    }

    /** Random Tick von Shard-Erz. */
    public static void seedFromOre(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!StoryManager.isSpreading(level.getServer()) || random.nextDouble() >= ShardlineConfig.VIRUS_ORE_SEED_CHANCE.get()) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            queueInfection(level, pos.offset(random.nextIntBetweenInclusive(-1, 1), random.nextIntBetweenInclusive(-1, 1),
                    random.nextIntBetweenInclusive(-1, 1)));
        }
        if (InfectionMap.isExposed(level, pos)) {
            StoryManager.data(level.getServer()).addHerd(level.dimension(), pos, HERD_SPACING);
        }
    }

    /** Random Tick eines Splitterportals ausserhalb der Splitterweite. */
    public static void seedFromPortal(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!StoryManager.isSpreading(level.getServer()) || random.nextDouble() >= ShardlineConfig.VIRUS_PORTAL_SEED_CHANCE.get()) {
            return;
        }
        seedAround(level, pos, ShardlineConfig.VIRUS_PORTAL_SEED_RADIUS.get(), 3, random);
        StoryManager.data(level.getServer()).addHerd(level.dimension(), pos, HERD_SPACING);
    }

    // ------------------------------------------------------------------ Umwandlung

    /**
     * Infiziert die Position sofort, wenn alle Bedingungen erfuellt sind: Dimension erlaubt ({@code virus.dimensions}),
     * Block befallbar, an der Oberflaeche oder im Gestein darunter ({@link InfectionMap#canInfectDeep}), kein
     * Reinheitsanker in der Naehe und innerhalb von {@code virus.maxRange} um einen Herd.
     *
     * @return true, wenn ein Block umgewandelt wurde
     */
    public static boolean tryInfect(ServerLevel level, BlockPos pos) {
        if (!isAllowedDimension(level)) {
            return false;
        }
        BlockState state = InfectionMap.stateIfLoaded(level, pos);
        if (state == null) {
            return false;
        }
        BlockState infected = InfectionMap.infectedFor(state);
        if (infected == null || PurityAnchors.isProtected(level, pos)
                || !InfectionMap.isExposed(level, pos) && !InfectionMap.canInfectDeep(level, pos)) {
            return false;
        }
        ShardlineWorldData data = StoryManager.data(level.getServer());
        int maxRange = ShardlineConfig.VIRUS_MAX_RANGE.get();
        if (maxRange > 0 && data.distanceSqrToNearestHerd(level.dimension(), pos) > (long) maxRange * maxRange) {
            return false;
        }
        if (!level.setBlock(pos, infected, Block.UPDATE_CLIENTS)) {
            return false;
        }
        int count = data.changeChunkInfection(level.dimension(), new ChunkPos(pos), 1);
        VirusHeartSpawner.onChunkInfectionChanged(level, new ChunkPos(pos), count);
        return true;
    }

    /** Ob sich der Virus in dieser Dimension ausbreiten darf ({@code virus.dimensions}). */
    public static boolean isAllowedDimension(ServerLevel level) {
        return ShardlineConfig.VIRUS_DIMENSIONS.get().contains(level.dimension().location().toString());
    }

    /**
     * Reinigt einen einzelnen Block.
     *
     * @return true, wenn der Block infiziert war und gereinigt wurde
     */
    public static boolean purify(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState clean = InfectionMap.purifiedFor(state, level);
        if (clean == null || !level.setBlock(pos, clean, Block.UPDATE_CLIENTS)) {
            return false;
        }
        StoryManager.data(level.getServer()).changeChunkInfection(level.dimension(), new ChunkPos(pos), -1);
        return true;
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Queue queue = QUEUES.get(level.dimension());
        if (queue == null || queue.size() == 0) {
            return;
        }
        if (!StoryManager.isSpreading(level.getServer())) {
            queue.clear();
            return;
        }
        int budget = ShardlineConfig.VIRUS_MAX_BLOCKS_PER_TICK.get();
        while (budget > 0 && !queue.byChunk.isEmpty()) {
            long chunkKey = queue.byChunk.firstLongKey();
            LongArrayList positions = queue.byChunk.get(chunkKey);
            if (level.getChunkSource().getChunkNow(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey)) == null) {
                // Chunk entladen: ganze Gruppe verwerfen.
                positions.forEach(queue.queued::remove);
                queue.byChunk.removeFirst();
                continue;
            }
            while (budget > 0 && !positions.isEmpty()) {
                long packed = positions.removeLong(positions.size() - 1);
                queue.queued.remove(packed);
                tryInfect(level, BlockPos.of(packed));
                budget--;
            }
            if (positions.isEmpty()) {
                queue.byChunk.removeFirst();
            }
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        QUEUES.clear();
    }
}
