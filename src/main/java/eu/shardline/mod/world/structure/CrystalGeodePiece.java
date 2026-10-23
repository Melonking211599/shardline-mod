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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Kristallgeode: eine halb vergrabene Hohlkugel in den Leuchtenden Hoehlen. Aussen Splitterstein
 * mit Shard-Erz, innen eine Schicht Leuchtkristall voller Kristallbueschel. Ein Gang fuehrt hinein,
 * drinnen nisten Kristallspinnen um eine Truhe.
 */
public class CrystalGeodePiece extends ShardlinePiece {
    private static final int RADIUS = 7;
    private static final int FOUNDATION = 4;

    public CrystalGeodePiece(BlockPos origin, long seed) {
        super(ModStructures.CRYSTAL_GEODE_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - RADIUS - 1, origin.getY() - RADIUS - FOUNDATION, origin.getZ() - RADIUS - 2,
                origin.getX() + RADIUS + 1, origin.getY() + RADIUS + 2, origin.getZ() + RADIUS + 1));
    }

    public CrystalGeodePiece(CompoundTag tag) {
        super(ModStructures.CRYSTAL_GEODE_PIECE.get(), tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState ore = ModBlocks.SHARD_ORE.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState shardBlock = ModBlocks.SHARD_BLOCK.get().defaultBlockState();
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState web = ModBlocks.CRYSTAL_WEB.get().defaultBlockState();

        int ox = this.origin.getX();
        int cy = this.origin.getY() + 1;
        int oz = this.origin.getZ();
        int floor = cy - 3;
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                double flat = horizontalDistance(dx, dz);
                // Fundament unter der Kugel
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    if (flat <= RADIUS - 2 - depth) {
                        this.fill(level, chunkBox, x, cy - RADIUS - depth + 1, z, stone);
                    }
                }
                boolean tunnel = Math.abs(dx) <= 1 && dz < -RADIUS + 4;
                for (int y = cy - RADIUS; y <= cy + RADIUS; y++) {
                    int dy = y - cy;
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (tunnel && y > floor && y <= floor + 3) {
                        this.clear(level, chunkBox, x, y, z);
                        continue;
                    }
                    if (tunnel && y == floor && dz >= -RADIUS - 2) {
                        this.set(level, chunkBox, x, y, z, bricks);
                        continue;
                    }
                    if (d > RADIUS + 0.3) {
                        continue;
                    }
                    RandomSource block = this.positional(x, y, z);
                    if (d > RADIUS - 1.2) {
                        this.set(level, chunkBox, x, y, z, block.nextFloat() < 0.08F ? ore : stone);
                    } else if (d > RADIUS - 2.2 || y <= floor) {
                        this.set(level, chunkBox, x, y, z, block.nextFloat() < 0.02F ? shardBlock : glow);
                    } else {
                        this.clear(level, chunkBox, x, y, z);
                        Direction support = this.support(dx, dy, dz, floor - cy);
                        float roll = block.nextFloat();
                        if (support != null && roll < 0.18F) {
                            this.set(level, chunkBox, x, y, z, ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState()
                                    .setValue(AmethystClusterBlock.FACING, support.getOpposite()));
                        } else if (support == Direction.UP && roll < 0.24F) {
                            this.set(level, chunkBox, x, y, z, web);
                        }
                    }
                }
            }
        }

        this.chest(level, chunkBox, random, ox, floor + 1, oz + 2, Direction.NORTH, ModLootTables.CRYSTAL_GEODE_CHEST);
        RandomSource structure = this.structureRandom();
        this.spawn(level, chunkBox, ModEntities.CRYSTAL_SPIDER.get(), ox - 2, floor + 1, oz, structure.nextFloat() * 360.0F);
        this.spawn(level, chunkBox, ModEntities.CRYSTAL_SPIDER.get(), ox + 2, floor + 1, oz - 1, structure.nextFloat() * 360.0F);
    }

    /**
     * Richtung zur angrenzenden Kristallwand (von der Zelle aus gesehen) oder null, wenn die Zelle frei
     * im Raum liegt. Der flache Boden zaehlt als Wand nach unten.
     */
    private Direction support(int dx, int dy, int dz, int floorOffset) {
        if (dy - 1 <= floorOffset) {
            return Direction.DOWN;
        }
        for (Direction direction : Direction.values()) {
            int nx = dx + direction.getStepX();
            int ny = dy + direction.getStepY();
            int nz = dz + direction.getStepZ();
            if (Math.sqrt(nx * nx + ny * ny + nz * nz) > RADIUS - 2.2) {
                return direction;
            }
        }
        return null;
    }
}
