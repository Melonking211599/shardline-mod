package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.npc.VeylEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Hueter-Bibliothek (Kapitel 8-9, versiegelter Zweig): ein langer Saal aus Hueterstein mit Saeulen, hohen
 * Kristallglasfenstern und einem teilweise eingestuerzten Runendach. Zwei Reihen Buecherregale bilden einen Mittelgang mit
 * Lesepulten, am Nordende steht das Archiv (Truhe mit Hueter-Schriften). Veyl, der Geist des letzten Archivars, schwebt
 * in der Mitte; hier erwacht in Kapitel 9 der Nachhall der Koenigin.
 */
public class KeeperLibraryPiece extends ShardlinePiece {
    public static final int HALF_W = 7;
    public static final int HALF_L = 9;
    private static final int WALL = 7;
    private static final int FOUNDATION = 6;

    public KeeperLibraryPiece(BlockPos origin, long seed) {
        super(ModStructures.KEEPER_LIBRARY_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - HALF_W - 1, origin.getY() - FOUNDATION, origin.getZ() - HALF_L - 1,
                origin.getX() + HALF_W + 1, origin.getY() + WALL + 2, origin.getZ() + HALF_L + 1));
    }

    public KeeperLibraryPiece(CompoundTag tag) {
        super(ModStructures.KEEPER_LIBRARY_PIECE.get(), tag);
    }

    public static BlockPos veylPos(BlockPos origin) {
        return origin.offset(0, 1, 0);
    }

    public static BlockPos chestPos(BlockPos origin) {
        return origin.offset(0, 1, -HALF_L + 2);
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
        BlockState glass = ModBlocks.CRYSTAL_GLASS.get().defaultBlockState();
        BlockState shelf = Blocks.BOOKSHELF.defaultBlockState();
        BlockState cluster = ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP);
        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                if (Math.abs(dx) > HALF_W || Math.abs(dz) > HALF_L) {
                    continue;
                }
                RandomSource column = this.positional(x, oy, z);
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    if (Math.abs(dx) <= HALF_W - depth / 2 && Math.abs(dz) <= HALF_L - depth / 2) {
                        this.fill(level, chunkBox, x, oy - depth, z, stone);
                    }
                }
                this.set(level, chunkBox, x, oy, z, dx == 0 ? runeDouble : bricks);
                for (int dy = 1; dy <= WALL + 1; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }
                boolean wallX = Math.abs(dx) == HALF_W;
                boolean wallZ = Math.abs(dz) == HALF_L;
                if (wallX || wallZ) {
                    boolean pillarSpot = (wallX && wallZ) || (wallX && Math.floorMod(dz, 3) == 0) || (wallZ && Math.floorMod(dx, 4) == 0);
                    boolean door = dz == HALF_L && Math.abs(dx) <= 1;
                    for (int dy = 1; dy <= WALL; dy++) {
                        if (door && dy <= 3) {
                            continue;
                        }
                        boolean window = !pillarSpot && dy >= 3 && dy <= 5 && (wallX ? Math.floorMod(dz, 3) != 0 : Math.abs(dx) == 2);
                        this.set(level, chunkBox, x, oy + dy, z, pillarSpot ? pillar : window ? glass : bricks);
                    }
                    this.set(level, chunkBox, x, oy + WALL + 1, z, runeTop);
                    continue;
                }
                // Dach: Runenplatten, zur Suedostecke hin eingestuerzt
                double collapse = (dx + dz + HALF_W + HALF_L) / (double) (2 * (HALF_W + HALF_L));
                if (column.nextFloat() > collapse * 0.6F) {
                    this.set(level, chunkBox, x, oy + WALL + 1, z, rune);
                } else if (column.nextFloat() < 0.35F) {
                    this.set(level, chunkBox, x, oy + 1, z, column.nextBoolean() ? cluster : rune);   // Truemmer am Boden
                }
                // Zwei Regalreihen mit Durchgaengen, Lesepulte im Mittelgang
                boolean shelfRow = Math.abs(dx) == 4 || Math.abs(dx) == HALF_W - 1;
                boolean gap = Math.floorMod(dz, 4) == 0;
                if (shelfRow && !gap && Math.abs(dz) <= HALF_L - 2) {
                    int height = Math.abs(dx) == HALF_W - 1 ? 4 : 3 - (column.nextFloat() < 0.2F ? 1 : 0);
                    for (int dy = 1; dy <= height; dy++) {
                        this.set(level, chunkBox, x, oy + dy, z, shelf);
                    }
                }
            }
        }

        // Lesepulte und Kristalllicht im Mittelgang
        for (int dz : new int[] {-4, 4}) {
            for (int dx : new int[] {-2, 2}) {
                this.set(level, chunkBox, ox + dx, oy + 1, oz + dz, Blocks.LECTERN.defaultBlockState()
                        .setValue(LecternBlock.FACING, dx < 0 ? Direction.EAST : Direction.WEST));
            }
        }
        for (int dz : new int[] {-7, 7}) {
            this.set(level, chunkBox, ox - 2, oy + 1, oz + dz, cluster);
            this.set(level, chunkBox, ox + 2, oy + 1, oz + dz, cluster);
        }
        // Archiv am Nordende: Podest mit Truhe
        for (int dx = -2; dx <= 2; dx++) {
            this.set(level, chunkBox, ox + dx, oy + 1, oz - HALF_L + 1, Math.abs(dx) == 2 ? pillar : rune);
        }
        BlockPos chest = chestPos(this.origin);
        this.clear(level, chunkBox, chest.getX(), chest.getY(), chest.getZ());
        this.chest(level, chunkBox, random, chest.getX(), chest.getY(), chest.getZ(), Direction.SOUTH, ModLootTables.KEEPER_LIBRARY_CHEST);

        BlockPos veyl = veylPos(this.origin);
        VeylEntity ghost = this.spawn(level, chunkBox, ModEntities.VEYL.get(), veyl.getX(), veyl.getY(), veyl.getZ(), 180.0F);
        if (ghost != null) {
            ghost.setHome(veyl);
        }
    }
}
