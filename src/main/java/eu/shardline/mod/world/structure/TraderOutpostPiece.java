package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Handelsposten: kleine Huette aus Kristallholz mit Ziegeldach, in der ein Splitterhaendler
 * wohnt. Mit Vorratstruhe, Werkbank und Laternenpfosten vor der Tuer.
 */
public class TraderOutpostPiece extends ShardlinePiece {
    private static final int HALF = 3;
    private static final int WALL_HEIGHT = 4;
    private static final int FOUNDATION = 7;

    public TraderOutpostPiece(BlockPos origin, long seed) {
        super(ModStructures.TRADER_OUTPOST_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - HALF - 2, origin.getY() - FOUNDATION, origin.getZ() - HALF - 3,
                origin.getX() + HALF + 2, origin.getY() + WALL_HEIGHT + 3, origin.getZ() + HALF + 2));
    }

    public TraderOutpostPiece(CompoundTag tag) {
        super(ModStructures.TRADER_OUTPOST_PIECE.get(), tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState planks = ModBlocks.CRYSTAL_PLANKS.get().defaultBlockState();
        BlockState log = ModBlocks.CRYSTAL_LOG.get().defaultBlockState();
        BlockState logX = log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState slab = ModBlocks.SHARDSTONE_BRICK_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState wall = ModBlocks.SHARDSTONE_BRICK_WALL.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState cluster = ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP);

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                double d = horizontalDistance(dx, dz);
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    if (d <= HALF + 2.5 - (depth - 1) * 0.7) {
                        this.fill(level, chunkBox, x, oy - depth, z, stone);
                    }
                }
                boolean inside = ax <= HALF && az <= HALF;
                boolean porch = ax <= 1 && dz < -HALF && dz >= -HALF - 2;
                if (!inside && !porch && Math.max(ax, az) > HALF + 1) {
                    continue;
                }
                // Boden und Freiraum
                this.set(level, chunkBox, x, oy, z, inside ? planks : bricks);
                for (int dy = 1; dy <= WALL_HEIGHT + 2; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }
                // Dach (ragt einen Block ueber)
                if (Math.max(ax, az) <= HALF + 1) {
                    this.set(level, chunkBox, x, oy + WALL_HEIGHT + 1, z, ax == 0 && az == 0 ? glow : slab);
                }
                if (!inside) {
                    continue;
                }
                boolean edgeX = ax == HALF;
                boolean edgeZ = az == HALF;
                for (int dy = 1; dy <= WALL_HEIGHT; dy++) {
                    int y = oy + dy;
                    if (edgeX && edgeZ) {
                        this.set(level, chunkBox, x, y, z, log);
                    } else if (edgeX || edgeZ) {
                        boolean door = dz == -HALF && dx == 0 && dy <= 2;
                        boolean window = dy == 2 && ((edgeX && dz == 0) || (dz == HALF && dx == 0));
                        if (door) {
                            continue;
                        }
                        if (window) {
                            this.set(level, chunkBox, x, y, z, Blocks.GLASS_PANE.defaultBlockState());
                        } else if (dy == WALL_HEIGHT) {
                            this.set(level, chunkBox, x, y, z, edgeZ ? logX : log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                        } else {
                            this.set(level, chunkBox, x, y, z, planks);
                        }
                    }
                }
            }
        }

        // Laternenpfosten vor der Tuer
        for (int side = -1; side <= 1; side += 2) {
            int px = ox + side * 2;
            int pz = oz - HALF - 2;
            this.set(level, chunkBox, px, oy, pz, bricks);
            this.set(level, chunkBox, px, oy + 1, pz, wall);
            this.set(level, chunkBox, px, oy + 2, pz, wall);
            this.set(level, chunkBox, px, oy + 3, pz, glow);
            this.set(level, chunkBox, px, oy + 4, pz, cluster);
        }

        // Einrichtung
        this.chest(level, chunkBox, random, ox + 2, oy + 1, oz + 2, Direction.WEST, ModLootTables.TRADER_OUTPOST_CHEST);
        this.set(level, chunkBox, ox - 2, oy + 1, oz + 2, Blocks.CRAFTING_TABLE.defaultBlockState());
        this.set(level, chunkBox, ox - 2, oy + 1, oz + 1, Blocks.BARREL.defaultBlockState());
        this.set(level, chunkBox, ox + 2, oy + 1, oz - 2, Blocks.SMOKER.defaultBlockState()
                .setValue(SmokerBlock.FACING, Direction.WEST));

        RandomSource structure = this.structureRandom();
        this.spawn(level, chunkBox, ModEntities.SHARD_TRADER.get(), ox, oy + 1, oz, structure.nextFloat() * 360.0F);
        if (structure.nextBoolean()) {
            this.spawn(level, chunkBox, ModEntities.GLOW_MOTH.get(), ox, oy + 3, oz - HALF - 2, structure.nextFloat() * 360.0F);
        }
    }
}
