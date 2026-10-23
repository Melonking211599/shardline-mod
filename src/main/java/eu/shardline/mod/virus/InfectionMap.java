package eu.shardline.mod.virus;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.world.dimension.ModDimensions;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
        return null;
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
     * Der Virus befaellt nur Bloecke mit Kontakt zu Luft (Oberflaechen, Hoehlenwaende). Das haelt
     * die Zahl der Umwandlungen klein und laesst ihn sichtbar wachsen.
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
