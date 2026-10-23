package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.MemoryCrystalBlock;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.story.lore.Visions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.ChunkPos;

/**
 * Ruinen-Turm: runder, teilweise eingestuerzter Turm aus Splitterstein-Ziegeln auf einem
 * kegelfoermigen Fundament. Drei Stockwerke mit Leiter, Truhen und Gesplitterten Rittern als Wache.
 */
public class RuinTowerPiece extends ShardlinePiece {
    private static final int RADIUS = 4;
    private static final int FOUNDATION = 9;

    private final int height;

    public RuinTowerPiece(BlockPos origin, long seed) {
        super(ModStructures.RUIN_TOWER_PIECE.get(), origin, seed, box(origin, heightFor(seed)));
        this.height = heightFor(seed);
    }

    public RuinTowerPiece(CompoundTag tag) {
        super(ModStructures.RUIN_TOWER_PIECE.get(), tag);
        this.height = heightFor(this.seed);
    }

    private static int heightFor(long seed) {
        return 14 + RandomSource.create(seed).nextInt(7);
    }

    private static BoundingBox box(BlockPos origin, int height) {
        return new BoundingBox(origin.getX() - RADIUS - 1, origin.getY() - FOUNDATION, origin.getZ() - RADIUS - 1,
                origin.getX() + RADIUS + 1, origin.getY() + height + 2, origin.getZ() + RADIUS + 1);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState cracked = ModBlocks.CRACKED_SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState slab = ModBlocks.SHARDSTONE_BRICK_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState wallTop = ModBlocks.SHARDSTONE_BRICK_WALL.get().defaultBlockState();
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                double d = horizontalDistance(dx, dz);

                // Kegelfoermiges Fundament (traegt den Turm auch ueber dem Void).
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    double radius = RADIUS + 1.0 - (depth - 1) * 0.6;
                    if (d <= radius) {
                        this.fill(level, chunkBox, x, oy - depth, z, stone);
                    }
                }
                if (d > RADIUS + 0.5) {
                    continue;
                }
                RandomSource column = this.positional(x, oy, z);
                this.set(level, chunkBox, x, oy, z, column.nextFloat() < 0.2F ? cracked : bricks);

                boolean wall = d > RADIUS - 0.5;
                int columnTop = this.height - (wall && column.nextFloat() < 0.35F ? 1 + column.nextInt(5) : 0);
                for (int dy = 1; dy <= this.height + 1; dy++) {
                    int y = oy + dy;
                    if (wall) {
                        if (dy > columnTop) {
                            this.clear(level, chunkBox, x, y, z);
                        } else if (dy == columnTop && dy == this.height && (dx + dz) % 2 == 0) {
                            this.set(level, chunkBox, x, y, z, wallTop);
                        } else if (isDoor(dx, dz, dy) || isWindow(dx, dz, dy)) {
                            this.clear(level, chunkBox, x, y, z);
                        } else if (isLight(dx, dz, dy)) {
                            this.set(level, chunkBox, x, y, z, glow);
                        } else {
                            RandomSource block = this.positional(x, y, z);
                            float roll = block.nextFloat();
                            if (dy > 3 && roll < 0.05F) {
                                this.clear(level, chunkBox, x, y, z);
                            } else {
                                this.set(level, chunkBox, x, y, z, roll < 0.25F ? cracked : bricks);
                            }
                        }
                    } else if (dy % 5 == 0 && dy < this.height - 1) {
                        // Zwischenboden mit Leiterloch.
                        boolean ladderHole = dx == 0 && dz == RADIUS - 1;
                        if (ladderHole) {
                            this.set(level, chunkBox, x, y, z, ladder);
                        } else {
                            this.set(level, chunkBox, x, y, z, slab);
                        }
                    } else if (dx == 0 && dz == RADIUS - 1 && dy < this.lastFloor()) {
                        this.set(level, chunkBox, x, y, z, ladder);
                    } else {
                        this.clear(level, chunkBox, x, y, z);
                    }
                }
            }
        }

        RandomSource structure = this.structureRandom();
        // Truhen
        this.chest(level, chunkBox, random, ox + 2, oy + 1, oz - 1, Direction.WEST, ModLootTables.RUIN_TOWER_CHEST);
        if (this.lastFloor() >= 10 && structure.nextBoolean()) {
            this.chest(level, chunkBox, random, ox - 2, oy + this.lastFloor() + 1, oz - 1, Direction.EAST, ModLootTables.RUIN_TOWER_TREASURE);
        }
        // Erinnerungskristall im Erdgeschoss (eigener Zufall, damit die uebrige Ausstattung gleich bleibt)
        int vision = Visions.RUIN_VISIONS[this.positional(ox, oy, oz).nextInt(Visions.RUIN_VISIONS.length)];
        this.set(level, chunkBox, ox - 2, oy + 1, oz + 1, ModBlocks.MEMORY_CRYSTAL.get().defaultBlockState().setValue(MemoryCrystalBlock.VISION, vision));
        // Waechter
        int knights = 1 + structure.nextInt(2);
        this.spawn(level, chunkBox, ModEntities.SHATTERED_KNIGHT.get(), ox - 1, oy + 1, oz - 1, structure.nextFloat() * 360.0F);
        if (knights > 1) {
            this.spawn(level, chunkBox, ModEntities.SHATTERED_KNIGHT.get(), ox + 1, oy + 1, oz + 1, structure.nextFloat() * 360.0F);
        }
        // Gelegentlich ein gefangener Splitterhaendler im obersten Stockwerk.
        if (this.lastFloor() >= 10 && structure.nextFloat() < 0.25F) {
            this.spawn(level, chunkBox, ModEntities.SHARD_TRADER.get(), ox + 1, oy + this.lastFloor() + 1, oz, structure.nextFloat() * 360.0F);
        }
    }

    /** Hoehe des obersten Zwischenbodens. */
    private int lastFloor() {
        int floor = 0;
        for (int dy = 5; dy < this.height - 1; dy += 5) {
            floor = dy;
        }
        return floor;
    }

    private static boolean isDoor(int dx, int dz, int dy) {
        return dz < 0 && Math.abs(dx) <= 0 && dy <= 2;
    }

    private static boolean isWindow(int dx, int dz, int dy) {
        return dz == 0 && dy % 5 == 3;
    }

    private static boolean isLight(int dx, int dz, int dy) {
        return Math.abs(dx) == Math.abs(dz) && dy % 5 == 2;
    }
}
