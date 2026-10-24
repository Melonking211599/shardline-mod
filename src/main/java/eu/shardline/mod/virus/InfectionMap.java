package eu.shardline.mod.virus;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.world.dimension.ModDimensions;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Welche Bloecke der Virus befaellt und wozu sie beim Reinigen wieder werden.
 * Die befallbaren Bloecke stehen in den Block-Tags {@code shardline:virus_infectable/*}
 * und lassen sich per Datapack erweitern.
 */
public final class InfectionMap {
    private InfectionMap() {
    }

    public static boolean isInfected(BlockState state) {
        return state.getBlock() instanceof Infected;
    }

    /** Infizierte Variante des Blocks oder {@code null}, wenn er nicht befallen werden kann. */
    @Nullable
    public static BlockState infectedFor(BlockState state) {
        if (state.isAir() || isInfected(state)) {
            return null;
        }
        Block strainBlock = strainBlockFor(state);
        if (strainBlock != null) {
            return strainBlock.defaultBlockState();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_GRASS)) {
            return ModBlocks.INFECTED_GRASS.get().defaultBlockState();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_DIRT)) {
            return ModBlocks.INFECTED_DIRT.get().defaultBlockState();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_STONE)) {
            return ModBlocks.INFECTED_STONE.get().defaultBlockState();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_SAND)) {
            return ModBlocks.INFECTED_SAND.get().defaultBlockState();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_LOG)) {
            BlockState log = ModBlocks.INFECTED_LOG.get().defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                log = log.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            return log;
        }
        if (state.is(ModTags.Blocks.INFECTABLE_LEAVES)) {
            return ModBlocks.INFECTED_LEAVES.get().defaultBlockState();
        }
        return null;
    }

    /**
     * Bloecke der Erweiterung "Nach dem Ausbruch": Tiefenschiefer und Bretter (Shardvirus), Nether (Glutbrand), End
     * (Leerenfaeule). Tiefenschiefer wird vor dem allgemeinen Stein geprueft, weil er auch in base_stone_overworld steht.
     */
    @Nullable
    private static Block strainBlockFor(BlockState state) {
        if (state.is(ModTags.Blocks.INFECTABLE_DEEPSLATE)) {
            return ModBlocks.INFECTED_DEEPSLATE.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_PLANKS)) {
            return ModBlocks.INFECTED_PLANKS.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_NYLIUM)) {
            return ModBlocks.INFECTED_NYLIUM.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_NETHERRACK)) {
            return ModBlocks.INFECTED_NETHERRACK.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_BLACKSTONE)) {
            return ModBlocks.INFECTED_BLACKSTONE.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_SOUL_SOIL)) {
            return ModBlocks.INFECTED_SOUL_SOIL.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_WART_BLOCK)) {
            return ModBlocks.INFECTED_WART_BLOCK.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_END_STONE)) {
            return ModBlocks.INFECTED_END_STONE.get();
        }
        if (state.is(ModTags.Blocks.INFECTABLE_PURPUR)) {
            return ModBlocks.INFECTED_PURPUR.get();
        }
        return null;
    }

    /** Gereinigte Variante; in der Splitterweite entstehen die dort heimischen Bloecke. */
    @Nullable
    public static BlockState purifiedFor(BlockState state, Level level) {
        if (!isInfected(state)) {
            return null;
        }
        boolean splitterweite = level.dimension() == ModDimensions.SPLITTERWEITE_LEVEL;
        Block block = state.getBlock();
        if (block == ModBlocks.INFECTED_STONE.get()) {
            return (splitterweite ? ModBlocks.SHARDSTONE.get() : Blocks.STONE).defaultBlockState();
        }
        if (block == ModBlocks.INFECTED_DIRT.get()) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (block == ModBlocks.INFECTED_GRASS.get()) {
            return (splitterweite ? ModBlocks.CRYSTAL_MOSS.get() : Blocks.GRASS_BLOCK).defaultBlockState();
        }
        if (block == ModBlocks.INFECTED_SAND.get()) {
            return (splitterweite ? ModBlocks.ASH_BLOCK.get() : Blocks.SAND).defaultBlockState();
        }
        if (block == ModBlocks.INFECTED_LOG.get()) {
            BlockState log = (splitterweite ? ModBlocks.CRYSTAL_LOG.get() : Blocks.OAK_LOG).defaultBlockState();
            return log.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        if (block == ModBlocks.INFECTED_LEAVES.get()) {
            BlockState leaves = (splitterweite ? ModBlocks.CRYSTAL_LEAVES.get() : Blocks.OAK_LEAVES).defaultBlockState();
            return leaves.setValue(LeavesBlock.PERSISTENT, true);
        }
        if (block == ModBlocks.INFECTED_PLANKS.get()) {
            return (splitterweite ? ModBlocks.CRYSTAL_PLANKS.get() : Blocks.OAK_PLANKS).defaultBlockState();
        }
        Block clean = purifiedTargets().get(block);
        return clean == null ? null : clean.defaultBlockState();
    }

    @Nullable
    private static Map<Block, Block> purified;

    /** Feste Reinigungsziele der Stammbloecke (unabhaengig von der Dimension); erst nach der Registrierung gebaut. */
    private static Map<Block, Block> purifiedTargets() {
        if (purified == null) {
            Map<Block, Block> map = new IdentityHashMap<>();
            map.put(ModBlocks.INFECTED_DEEPSLATE.get(), Blocks.DEEPSLATE);
            map.put(ModBlocks.INFECTED_NETHERRACK.get(), Blocks.NETHERRACK);
            map.put(ModBlocks.INFECTED_BLACKSTONE.get(), Blocks.BLACKSTONE);
            map.put(ModBlocks.INFECTED_SOUL_SOIL.get(), Blocks.SOUL_SOIL);
            map.put(ModBlocks.INFECTED_NYLIUM.get(), Blocks.CRIMSON_NYLIUM);
            map.put(ModBlocks.INFECTED_WART_BLOCK.get(), Blocks.NETHER_WART_BLOCK);
            map.put(ModBlocks.INFECTED_END_STONE.get(), Blocks.END_STONE);
            map.put(ModBlocks.INFECTED_PURPUR.get(), Blocks.PURPUR_BLOCK);
            purified = map;
        }
        return purified;
    }

    /** Blockzustand, ohne einen Chunk zu laden; {@code null}, wenn der Chunk nicht geladen ist. */
    @Nullable
    public static BlockState stateIfLoaded(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return null;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    /**
     * Tiefeninfektion: Ein Block ohne Luftkontakt darf befallen werden, wenn
     * <ul>
     *   <li>{@code virus.surfaceOnly} aus ist und {@code virus.maxDepth} &gt; 0,</li>
     *   <li>ein Nachbar schon befallen ist (der Virus frisst sich Block fuer Block hinein),</li>
     *   <li>die naechste Oberflaeche (Luft, Wasser, Hoehle) entlang einer Achse hoechstens {@code maxDepth} Bloecke
     *   entfernt ist, und</li>
     *   <li>der Block "poroes" ist: ein fester Anteil {@code virus.depthChance} aller Positionen, bestimmt aus der
     *   Position selbst. So entstehen im Gestein Adern statt voller Wuerfel, und das Ergebnis ist reproduzierbar.</li>
     * </ul>
     * Kostet hoechstens 6 + 6 * maxDepth Blockabfragen und laedt keine Chunks.
     */
    public static boolean canInfectDeep(ServerLevel level, BlockPos pos) {
        int maxDepth = ShardlineConfig.VIRUS_MAX_DEPTH.get();
        if (ShardlineConfig.VIRUS_SURFACE_ONLY.get() || maxDepth <= 0 || !isPorous(pos, ShardlineConfig.VIRUS_DEPTH_CHANCE.get())) {
            return false;
        }
        return hasInfectedNeighbor(level, pos) && depthBelowSurface(level, pos, maxDepth) <= maxDepth;
    }

    /** Fester, positionsabhaengiger Anteil {@code chance} aller Bloecke. */
    public static boolean isPorous(BlockPos pos, double chance) {
        long hash = Mth.getSeed(pos.getX(), pos.getY(), pos.getZ());
        return ((hash >>> 16) & 0xFFFF) / 65536.0 < chance;
    }

    public static boolean hasInfectedNeighbor(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighbor.setWithOffset(pos, direction);
            BlockState state = stateIfLoaded(level, neighbor);
            if (state != null && isInfected(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Abstand zur naechsten nicht-festen Position entlang der sechs Achsen; {@code limit + 1}, wenn keine innerhalb von
     * {@code limit} Bloecken liegt (oder der Chunk nicht geladen ist).
     */
    public static int depthBelowSurface(ServerLevel level, BlockPos pos, int limit) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int best = limit + 1;
        for (Direction direction : Direction.values()) {
            for (int step = 1; step < best; step++) {
                cursor.setWithOffset(pos, direction.getStepX() * step, direction.getStepY() * step, direction.getStepZ() * step);
                BlockState state = stateIfLoaded(level, cursor);
                if (state == null) {
                    break;
                }
                if (!state.isSolidRender(level, cursor)) {
                    best = step;
                    break;
                }
            }
        }
        return best;
    }

    /**
     * Der Virus befaellt vor allem Bloecke mit Kontakt zu Luft (Oberflaechen, Hoehlenwaende). Das haelt
     * die Zahl der Umwandlungen klein und laesst ihn sichtbar wachsen; in die Tiefe nur ueber {@link #canInfectDeep}.
     */
    public static boolean isExposed(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighbor.setWithOffset(pos, direction);
            BlockState state = stateIfLoaded(level, neighbor);
            if (state != null && !state.isSolidRender(level, neighbor)) {
                return true;
            }
        }
        return false;
    }
}
