package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Thronarena der Splitterkoenigin im Zentrum der Splitterweite (Chunk 0/0): eine riesige schwebende
 * Plattform mit spiralfoermigen Leuchtadern, zwoelf Saeulen und dem Thron-Altar auf einem Podest.
 */
public class QueenArenaPiece extends ShardlinePiece {
    public static final int RADIUS = 28;
    public static final int FLOOR_Y = 112;
    private static final int FOUNDATION = 18;
    private static final int CLEARANCE = 32;

    public QueenArenaPiece(BlockPos origin, long seed) {
        super(ModStructures.QUEEN_ARENA_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - RADIUS - 1, origin.getY() - FOUNDATION, origin.getZ() - RADIUS - 1,
                origin.getX() + RADIUS + 1, origin.getY() + CLEARANCE, origin.getZ() + RADIUS + 1));
    }

    public QueenArenaPiece(CompoundTag tag) {
        super(ModStructures.QUEEN_ARENA_PIECE.get(), tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState cracked = ModBlocks.CRACKED_SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState slab = ModBlocks.SHARDSTONE_BRICK_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState clusterUp = ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP);
        BlockState clusterDown = ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN);

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                double d = horizontalDistance(dx, dz);
                if (d > RADIUS + 0.5) {
                    continue;
                }
                RandomSource column = this.positional(x, oy, z);

                // Fundament mit haengenden Kristallen an der Unterseite
                int bottom = 0;
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    double radius = RADIUS + 0.5 - depth * 1.45;
                    if (d <= radius) {
                        this.set(level, chunkBox, x, oy - depth, z, column.nextFloat() < 0.05F ? glow : stone);
                        bottom = depth;
                    }
                }
                if (bottom > 0 && column.nextFloat() < 0.08F) {
                    this.fill(level, chunkBox, x, oy - bottom - 1, z, clusterDown);
                }

                // Boden: Spiralarme aus Leuchtkristall
                double angle = Math.atan2(dz, dx);
                double spiral = ((angle / Math.PI) * 3.0 + d / 6.0) % 2.0;
                if (spiral < 0) {
                    spiral += 2.0;
                }
                BlockState floor;
                if (d > 5.0 && spiral < 0.12) {
                    floor = glow;
                } else {
                    floor = column.nextFloat() < 0.12F ? cracked : bricks;
                }
                this.set(level, chunkBox, x, oy, z, floor);

                for (int dy = 1; dy <= CLEARANCE; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }

                // Aeusserer Rand
                if (d > RADIUS - 1.0 && (Math.abs(dx) > 2 && Math.abs(dz) > 2)) {
                    this.set(level, chunkBox, x, oy + 1, z, bricks);
                }

                // Thronpodest (zwei Stufen)
                if (d <= 3.5) {
                    this.set(level, chunkBox, x, oy + 1, z, bricks);
                } else if (d <= 4.5) {
                    this.set(level, chunkBox, x, oy + 1, z, slab);
                }
                if (d <= 2.0) {
                    this.set(level, chunkBox, x, oy + 2, z, bricks);
                } else if (d <= 3.5) {
                    this.set(level, chunkBox, x, oy + 2, z, slab);
                }
            }
        }

        // Zwoelf Saeulen, einige eingestuerzt
        RandomSource structure = this.structureRandom();
        for (int i = 0; i < 12; i++) {
            double angle = i * Mth.TWO_PI / 12.0;
            int px = ox + Mth.floor(Math.cos(angle) * 24.0);
            int pz = oz + Mth.floor(Math.sin(angle) * 24.0);
            boolean broken = structure.nextFloat() < 0.3F;
            int pillarHeight = broken ? 4 + structure.nextInt(5) : 12 + structure.nextInt(7);
            for (int dy = 1; dy <= pillarHeight; dy++) {
                BlockState block = this.positional(px, oy + dy, pz).nextFloat() < 0.2F ? cracked : bricks;
                for (int sx = 0; sx <= 1; sx++) {
                    for (int sz = 0; sz <= 1; sz++) {
                        this.set(level, chunkBox, px + sx, oy + dy, pz + sz, block);
                    }
                }
            }
            if (!broken) {
                for (int sx = 0; sx <= 1; sx++) {
                    for (int sz = 0; sz <= 1; sz++) {
                        this.set(level, chunkBox, px + sx, oy + pillarHeight + 1, pz + sz, glow);
                        this.set(level, chunkBox, px + sx, oy + pillarHeight + 2, pz + sz, clusterUp);
                    }
                }
            }
        }

        // Thron
        this.set(level, chunkBox, ox, oy + 3, oz, ModBlocks.QUEEN_ALTAR.get().defaultBlockState());
        for (Direction side : Direction.Plane.HORIZONTAL) {
            this.set(level, chunkBox, ox + side.getStepX() * 2, oy + 3, oz + side.getStepZ() * 2, clusterUp);
        }

        // Schatztruhen
        this.chest(level, chunkBox, random, ox + 7, oy + 1, oz, Direction.WEST, ModLootTables.QUEEN_ARENA_CHEST);
        this.chest(level, chunkBox, random, ox - 7, oy + 1, oz, Direction.EAST, ModLootTables.QUEEN_ARENA_CHEST);
    }
}
