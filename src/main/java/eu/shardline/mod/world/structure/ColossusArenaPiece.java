package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.MemoryCrystalBlock;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.story.lore.Visions;
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
 * Kristallwald-Arena des Kolosses: runde Kampfflaeche mit Leuchtring, acht Saeulen und dem
 * Koloss-Altar in der Mitte. Ruht auf einem eigenen Fundament und raeumt den Luftraum frei.
 */
public class ColossusArenaPiece extends ShardlinePiece {
    public static final int RADIUS = 18;
    private static final int FOUNDATION = 12;
    private static final int CLEARANCE = 22;

    public ColossusArenaPiece(BlockPos origin, long seed) {
        super(ModStructures.COLOSSUS_ARENA_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - RADIUS - 1, origin.getY() - FOUNDATION, origin.getZ() - RADIUS - 1,
                origin.getX() + RADIUS + 1, origin.getY() + CLEARANCE, origin.getZ() + RADIUS + 1));
    }

    public ColossusArenaPiece(CompoundTag tag) {
        super(ModStructures.COLOSSUS_ARENA_PIECE.get(), tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState cracked = ModBlocks.CRACKED_SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState wall = ModBlocks.SHARDSTONE_BRICK_WALL.get().defaultBlockState();
        BlockState slab = ModBlocks.SHARDSTONE_BRICK_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState moss = ModBlocks.CRYSTAL_MOSS.get().defaultBlockState();

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

                // Fundament
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    double radius = RADIUS + 0.5 - depth * 1.35;
                    if (d <= radius) {
                        this.set(level, chunkBox, x, oy - depth, z, column.nextFloat() < 0.04F ? glow : stone);
                    }
                }

                // Boden mit Leuchtring
                BlockState floor;
                if (Math.abs(d - 9.0) < 0.5 && (dx + dz) % 2 == 0) {
                    floor = glow;
                } else if (d > RADIUS - 2.5 && column.nextFloat() < 0.3F) {
                    floor = moss;
                } else {
                    floor = column.nextFloat() < 0.15F ? cracked : bricks;
                }
                this.set(level, chunkBox, x, oy, z, floor);

                // Luftraum freiraeumen
                for (int dy = 1; dy <= CLEARANCE; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }

                // Randmauer mit vier Eingaengen
                if (d > RADIUS - 0.5 && Math.abs(dx) > 1 && Math.abs(dz) > 1) {
                    this.set(level, chunkBox, x, oy + 1, z, wall);
                }

                // Podest um den Altar
                if (d > 1.0 && d <= 3.0) {
                    this.set(level, chunkBox, x, oy + 1, z, slab);
                }
            }
        }

        // Acht Saeulen
        RandomSource structure = this.structureRandom();
        for (int i = 0; i < 8; i++) {
            double angle = (i + 0.5) * Mth.TWO_PI / 8.0;
            int px = ox + Mth.floor(Math.cos(angle) * 14.0);
            int pz = oz + Mth.floor(Math.sin(angle) * 14.0);
            int pillarHeight = 7 + structure.nextInt(4);
            for (int dy = 1; dy <= pillarHeight; dy++) {
                BlockState block = this.positional(px, oy + dy, pz).nextFloat() < 0.2F ? cracked : bricks;
                this.set(level, chunkBox, px, oy + dy, pz, block);
                if (dy <= 2) {
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        this.set(level, chunkBox, px + side.getStepX(), oy + dy, pz + side.getStepZ(), block);
                    }
                }
            }
            this.set(level, chunkBox, px, oy + pillarHeight + 1, pz, glow);
            this.set(level, chunkBox, px, oy + pillarHeight + 2, pz,
                    ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
        }

        // Altar und Kristalle
        this.set(level, chunkBox, ox, oy + 1, oz, ModBlocks.COLOSSUS_ALTAR.get().defaultBlockState());
        for (int i = 0; i < 4; i++) {
            double angle = (i + 0.5) * Mth.TWO_PI / 4.0;
            int cx = ox + Mth.floor(Math.cos(angle) * 2.9);
            int cz = oz + Mth.floor(Math.sin(angle) * 2.9);
            this.set(level, chunkBox, cx, oy + 1, cz,
                    ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
        }

        // Erinnerungskristall zwischen zwei Saeulen
        this.set(level, chunkBox, ox - 10, oy + 1, oz + 10, ModBlocks.MEMORY_CRYSTAL.get().defaultBlockState()
                .setValue(MemoryCrystalBlock.VISION, Visions.COLOSSUS_VISION));

        // Vorratstruhe am Rand
        this.chest(level, chunkBox, random, ox, oy + 1, oz - RADIUS + 2, Direction.SOUTH, ModLootTables.COLOSSUS_ARENA_CHEST);
    }
}
