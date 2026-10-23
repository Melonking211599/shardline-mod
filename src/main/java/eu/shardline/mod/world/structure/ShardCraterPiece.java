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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Splitterkrater (Oberwelt): Einschlagstelle eines Bruchstuecks der Splitterweite. Eine flache
 * Mulde aus Asche und Splitterstein, in der Mitte der Splitterbrocken voller Shard-Erz. Verstreut
 * liegen Teile eines alten Portalrahmens und eine halb verschuettete Truhe eines frueheren Wanderers.
 */
public class ShardCraterPiece extends ShardlinePiece {
    private static final int RADIUS = 7;
    private static final int MAX_DEPTH = 4;
    private static final int CLEARANCE = 10;

    public ShardCraterPiece(BlockPos origin, long seed) {
        super(ModStructures.SHARD_CRATER_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - RADIUS - 2, origin.getY() - MAX_DEPTH - 2, origin.getZ() - RADIUS - 2,
                origin.getX() + RADIUS + 2, origin.getY() + CLEARANCE, origin.getZ() + RADIUS + 2));
    }

    public ShardCraterPiece(CompoundTag tag) {
        super(ModStructures.SHARD_CRATER_PIECE.get(), tag);
    }

    /** Tiefe der Mulde an dieser Stelle (0 am Rand). */
    private static int depth(double d) {
        if (d > RADIUS) {
            return 0;
        }
        double t = d / RADIUS;
        return Math.max(1, (int) Math.round((1.0 - t * t) * MAX_DEPTH));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState ash = ModBlocks.ASH_BLOCK.get().defaultBlockState();
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState ore = ModBlocks.SHARD_ORE.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState frame = ModBlocks.SHARD_PORTAL_FRAME.get().defaultBlockState();
        BlockState coarse = Blocks.COARSE_DIRT.defaultBlockState();

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                double d = horizontalDistance(dx, dz);
                RandomSource column = this.positional(x, oy, z);
                if (d <= RADIUS) {
                    int bottom = oy - depth(d);
                    for (int y = bottom + 1; y <= oy + CLEARANCE; y++) {
                        this.clear(level, chunkBox, x, y, z);
                    }
                    this.set(level, chunkBox, x, bottom, z, column.nextFloat() < 0.55F ? ash : stone);
                    this.set(level, chunkBox, x, bottom - 1, z, stone);
                } else if (d <= RADIUS + 1.5 && column.nextFloat() < 0.6F) {
                    // Aufgeworfener Kraterrand
                    this.fill(level, chunkBox, x, oy + 1, z, column.nextFloat() < 0.3F ? stone : coarse);
                }
            }
        }

        // Der Splitterbrocken in der Mitte
        int coreY = oy - MAX_DEPTH + 1;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy * 1.4 + dz * dz);
                    if (d > 2.3) {
                        continue;
                    }
                    int x = ox + dx;
                    int y = coreY + dy;
                    int z = oz + dz;
                    float roll = this.positional(x, y, z).nextFloat();
                    this.set(level, chunkBox, x, y, z, roll < 0.35F ? ore : roll < 0.45F ? glow : stone);
                }
            }
        }
        for (int[] spike : new int[][] {{0, 0}, {1, 1}, {-1, 0}, {0, -1}}) {
            this.set(level, chunkBox, ox + spike[0], coreY + 2, oz + spike[1], ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, Direction.UP));
        }

        // Verstreute Portalrahmen-Bruchstuecke
        RandomSource structure = this.structureRandom();
        int pieces = 3 + structure.nextInt(3);
        for (int i = 0; i < pieces; i++) {
            float angle = structure.nextFloat() * Mth.TWO_PI;
            double dist = 3.5 + structure.nextFloat() * 2.5;
            int x = ox + Mth.floor(Mth.cos(angle) * dist);
            int z = oz + Mth.floor(Mth.sin(angle) * dist);
            int y = oy - depth(horizontalDistance(x - ox, z - oz)) + 1;
            this.set(level, chunkBox, x, y, z, frame);
            if (structure.nextFloat() < 0.3F) {
                this.set(level, chunkBox, x, y + 1, z, frame);
            }
        }

        // Halb verschuettete Truhe am Muldenrand
        float angle = structure.nextFloat() * Mth.TWO_PI;
        int cx = ox + Mth.floor(Mth.cos(angle) * 5.0F);
        int cz = oz + Mth.floor(Mth.sin(angle) * 5.0F);
        int cyBottom = oy - depth(horizontalDistance(cx - ox, cz - oz));
        this.chest(level, chunkBox, random, cx, cyBottom + 1, cz, Direction.Plane.HORIZONTAL.getRandomDirection(structure),
                ModLootTables.SHARD_CRATER_CHEST);
    }
}
