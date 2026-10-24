package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.MemoryCrystalBlock;
import eu.shardline.mod.core.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Hueter-Ruinen: die Reste eines Versammlungssaals der Hueter aus hellem Hueterstein. Halb eingestuerzte
 * Mauern mit Saeulen, ein Runenpfad fuehrt zu einem Podest mit Erinnerungskristall ("Der Rat der Hueter"
 * oder eine andere fruehe Vision) und einer Truhe mit Lore-Buechern. Kristallwuchs zeigt, wie lange der
 * Saal verlassen ist. Steht in allen Biomen der Splitterweite.
 */
public class KeeperRuinPiece extends ShardlinePiece {
    /** Halbe Breite (x) und halbe Laenge (z) des Saals. */
    public static final int HALF_W = 6;
    public static final int HALF_L = 7;
    private static final int WALL = 5;
    private static final int FOUNDATION = 6;
    /** Visionen, die zu den Hueterruinen passen: Aurelis, der falsche Ton, der Rat der Hueter. */
    private static final int[] VISIONS = {0, 1, 2, 2};

    public KeeperRuinPiece(BlockPos origin, long seed) {
        super(ModStructures.KEEPER_RUIN_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - HALF_W - 1, origin.getY() - FOUNDATION, origin.getZ() - HALF_L - 1,
                origin.getX() + HALF_W + 1, origin.getY() + WALL + 3, origin.getZ() + HALF_L + 1));
    }

    public KeeperRuinPiece(CompoundTag tag) {
        super(ModStructures.KEEPER_RUIN_PIECE.get(), tag);
    }

    /** Position des Erinnerungskristalls (auf dem Podest am Nordende). */
    public static BlockPos crystalPos(BlockPos origin) {
        return origin.offset(0, 2, -HALF_L + 2);
    }

    /** Position der Truhe neben dem Podest. */
    public static BlockPos chestPos(BlockPos origin) {
        return origin.offset(2, 1, -HALF_L + 2);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState bricks = ModBlocks.KEEPER_RUIN_BRICKS.get().defaultBlockState();
        BlockState pillar = ModBlocks.KEEPER_RUIN_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState rune = ModBlocks.KEEPER_RUNE_SLAB.get().defaultBlockState();
        BlockState runeDouble = rune.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        BlockState runeTop = rune.setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState moss = ModBlocks.CRYSTAL_MOSS.get().defaultBlockState();
        BlockState cluster = ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP);

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                boolean inside = Math.abs(dx) <= HALF_W && Math.abs(dz) <= HALF_L;
                if (!inside) {
                    continue;
                }
                RandomSource column = this.positional(x, oy, z);
                // Fundament nach unten schmaler werdend, damit der Saal nicht in der Luft haengt
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    if (Math.abs(dx) <= HALF_W - depth / 2 && Math.abs(dz) <= HALF_L - depth / 2) {
                        this.fill(level, chunkBox, x, oy - depth, z, stone);
                    }
                }
                // Boden: Runenpfad in der Mitte, sonst Hueterstein mit Moosflecken
                BlockState floor;
                if (dx == 0 && dz > -HALF_L + 3) {
                    floor = runeDouble;
                } else {
                    floor = column.nextFloat() < 0.18F ? moss : bricks;
                }
                this.set(level, chunkBox, x, oy, z, floor);
                for (int dy = 1; dy <= WALL + 2; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }

                boolean edge = Math.abs(dx) == HALF_W || Math.abs(dz) == HALF_L;
                if (edge) {
                    boolean corner = Math.abs(dx) == HALF_W && Math.abs(dz) == HALF_L;
                    boolean pillarSpot = corner || (Math.abs(dx) == HALF_W && Math.floorMod(dz, 4) == 1);
                    boolean door = dz == HALF_L && Math.abs(dx) <= 1;
                    if (door) {
                        continue;
                    }
                    // Einsturz: Hoehe faellt zur Suedseite hin ab, mit zufaelligen Luecken
                    int height = pillarSpot ? WALL : WALL - 1 - Math.max(0, (dz + HALF_L) / 4) + column.nextInt(2);
                    if (!pillarSpot && column.nextFloat() < 0.15F) {
                        height = 1;
                    }
                    for (int dy = 1; dy <= height; dy++) {
                        this.set(level, chunkBox, x, oy + dy, z, pillarSpot ? pillar : bricks);
                    }
                    if (pillarSpot && height == WALL) {
                        this.set(level, chunkBox, x, oy + WALL + 1, z, runeTop);
                    } else if (height > 1 && column.nextFloat() < 0.3F) {
                        this.set(level, chunkBox, x, oy + height + 1, z, rune);
                    }
                } else if (column.nextFloat() < 0.05F && dx != 0) {
                    this.set(level, chunkBox, x, oy + 1, z, cluster);
                }
            }
        }

        // Podest mit Erinnerungskristall und Truhe am Nordende
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -HALF_L + 1; dz <= -HALF_L + 3; dz++) {
                this.set(level, chunkBox, ox + dx, oy + 1, oz + dz, Math.abs(dx) == 2 || dz == -HALF_L + 3 ? rune : bricks);
            }
        }
        BlockPos crystal = crystalPos(this.origin);
        int vision = VISIONS[this.positional(ox, oy + 2, oz).nextInt(VISIONS.length)];
        this.set(level, chunkBox, crystal.getX(), crystal.getY(), crystal.getZ(),
                ModBlocks.MEMORY_CRYSTAL.get().defaultBlockState().setValue(MemoryCrystalBlock.VISION, vision));
        BlockPos chest = chestPos(this.origin);
        this.clear(level, chunkBox, chest.getX(), chest.getY(), chest.getZ());
        this.chest(level, chunkBox, random, chest.getX(), chest.getY(), chest.getZ(), Direction.SOUTH, ModLootTables.KEEPER_RUIN_CHEST);
    }
}
