package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.MemoryCrystalBlock;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Aschegruft: eine vergrabene Grabkammer der letzten Ritter unter den Aschefeldern. Oben verraten
 * sie nur ein Aschefleck, ein gebrochener Obelisk und ein Schacht mit Leiter. Unten warten ein
 * Sarkophag, zwei Truhen, Kristallnetze und Aschewandler.
 */
public class AshCryptPiece extends ShardlinePiece {
    /** Halbe Breite der Kammer einschliesslich Wand. */
    private static final int HALF = 5;
    /** Abstand vom Boden der Oberflaeche zum Kammerboden. */
    private static final int DEPTH = 8;
    private static final int ROOM_HEIGHT = 4;

    public AshCryptPiece(BlockPos origin, long seed) {
        super(ModStructures.ASH_CRYPT_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - HALF, origin.getY() - DEPTH - 1, origin.getZ() - HALF,
                origin.getX() + HALF, origin.getY() + 4, origin.getZ() + HALF));
    }

    public AshCryptPiece(CompoundTag tag) {
        super(ModStructures.ASH_CRYPT_PIECE.get(), tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState cracked = ModBlocks.CRACKED_SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState ash = ModBlocks.ASH_BLOCK.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState web = ModBlocks.CRYSTAL_WEB.get().defaultBlockState();
        BlockState slab = ModBlocks.SHARDSTONE_BRICK_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState wall = ModBlocks.SHARDSTONE_BRICK_WALL.get().defaultBlockState();
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        int floor = oy - DEPTH;
        int ceiling = floor + ROOM_HEIGHT + 1;
        int shaftZ = oz - HALF + 1;
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                boolean shell = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                // Kammer: Boden, Waende, Decke; innen leer.
                for (int y = floor - 1; y <= ceiling; y++) {
                    RandomSource block = this.positional(x, y, z);
                    if (shell || y == floor - 1 || y == ceiling) {
                        this.set(level, chunkBox, x, y, z, block.nextFloat() < 0.35F ? cracked : bricks);
                    } else if (y == floor) {
                        this.set(level, chunkBox, x, y, z, block.nextFloat() < 0.3F ? ash : bricks);
                    } else {
                        this.clear(level, chunkBox, x, y, z);
                    }
                }
                // Aschefleck an der Oberflaeche
                double d = horizontalDistance(dx, dz);
                if (d <= HALF - 0.5 && this.positional(x, oy, z).nextFloat() < 0.8F) {
                    this.set(level, chunkBox, x, oy, z, ash);
                }
            }
        }

        // Stuetzpfeiler mit Leuchtkristall als Kapitell
        for (int[] p : new int[][] {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            for (int y = floor + 1; y < ceiling; y++) {
                this.set(level, chunkBox, ox + p[0], y, oz + p[1], y == ceiling - 1 ? glow : bricks);
            }
        }

        // Spinnweben aus Kristall in den Ecken unter der Decke
        for (int[] c : new int[][] {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}, {0, 3}, {-3, 0}}) {
            if (this.positional(ox + c[0], ceiling, oz + c[1]).nextFloat() < 0.7F) {
                this.set(level, chunkBox, ox + c[0], ceiling - 1, oz + c[1], web);
            }
        }

        // Sarkophag
        for (int dz = -1; dz <= 1; dz++) {
            this.set(level, chunkBox, ox, floor + 1, oz + dz, bricks);
            this.set(level, chunkBox, ox, floor + 2, oz + dz, slab);
        }

        // Schacht mit Leiter an der Nordwand
        for (int y = floor + 1; y <= oy + 1; y++) {
            if (y > ceiling) {
                // Schachtwaende oberhalb der Kammer
                this.set(level, chunkBox, ox - 1, y, shaftZ, bricks);
                this.set(level, chunkBox, ox + 1, y, shaftZ, bricks);
                this.set(level, chunkBox, ox, y, shaftZ + 1, bricks);
                this.set(level, chunkBox, ox, y, shaftZ - 1, bricks);
            }
            if (y <= oy) {
                this.set(level, chunkBox, ox, y, shaftZ, ladder);
            } else {
                this.clear(level, chunkBox, ox, y, shaftZ);
            }
        }
        this.set(level, chunkBox, ox - 1, oy + 1, shaftZ, wall);
        this.set(level, chunkBox, ox + 1, oy + 1, shaftZ, wall);
        this.set(level, chunkBox, ox, oy + 1, shaftZ - 1, wall);

        // Gebrochener Obelisk
        RandomSource structure = this.structureRandom();
        int obelisk = 2 + structure.nextInt(3);
        for (int dy = 1; dy <= obelisk; dy++) {
            this.set(level, chunkBox, ox + 3, oy + dy, oz + 2, dy == obelisk ? cracked : bricks);
        }

        // Erinnerung (Die letzten Ritter oder Das Zerspringen), Truhen, Waechter
        int vision = this.positional(ox, floor, oz).nextBoolean() ? 6 : 4;
        this.set(level, chunkBox, ox, floor + 1, oz + HALF - 1, ModBlocks.MEMORY_CRYSTAL.get().defaultBlockState().setValue(MemoryCrystalBlock.VISION, vision));
        this.chest(level, chunkBox, random, ox + HALF - 1, floor + 1, oz + HALF - 1, Direction.WEST, ModLootTables.ASH_CRYPT_CHEST);
        this.chest(level, chunkBox, random, ox - HALF + 1, floor + 1, oz + HALF - 1, Direction.EAST, ModLootTables.ASH_CRYPT_CHEST);
        this.spawn(level, chunkBox, ModEntities.ASH_WALKER.get(), ox - 3, floor + 1, oz, structure.nextFloat() * 360.0F);
        this.spawn(level, chunkBox, ModEntities.ASH_WALKER.get(), ox + 3, floor + 1, oz, structure.nextFloat() * 360.0F);
        this.spawn(level, chunkBox, ModEntities.CRYSTAL_SPIDER.get(), ox, floor + 1, oz + 3, structure.nextFloat() * 360.0F);
    }
}
