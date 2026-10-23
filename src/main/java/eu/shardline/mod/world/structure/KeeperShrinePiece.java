package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.MemoryCrystalBlock;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.entity.ModEntities;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Hueter-Schrein: runde Plattform mit sechs teils eingestuerzten Saeulen im Kristallwald. In der
 * Mitte steht ein Erinnerungskristall, daneben eine Opfertruhe der Hueter. Leuchtmotten umkreisen ihn.
 */
public class KeeperShrinePiece extends ShardlinePiece {
    private static final int RADIUS = 6;
    private static final int FOUNDATION = 8;
    private static final int PILLARS = 6;
    private static final int PILLAR_HEIGHT = 5;

    public KeeperShrinePiece(BlockPos origin, long seed) {
        super(ModStructures.KEEPER_SHRINE_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - RADIUS - 1, origin.getY() - FOUNDATION, origin.getZ() - RADIUS - 1,
                origin.getX() + RADIUS + 1, origin.getY() + PILLAR_HEIGHT + 3, origin.getZ() + RADIUS + 1));
    }

    public KeeperShrinePiece(CompoundTag tag) {
        super(ModStructures.KEEPER_SHRINE_PIECE.get(), tag);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState bricks = ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState cracked = ModBlocks.CRACKED_SHARDSTONE_BRICKS.get().defaultBlockState();
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState glow = ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState wall = ModBlocks.SHARDSTONE_BRICK_WALL.get().defaultBlockState();
        BlockState cluster = ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP);

        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                double d = horizontalDistance(dx, dz);
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    if (d <= RADIUS + 0.5 - (depth - 1) * 0.75) {
                        this.fill(level, chunkBox, x, oy - depth, z, stone);
                    }
                }
                if (d > RADIUS + 0.5) {
                    continue;
                }
                RandomSource column = this.positional(x, oy, z);
                // Boden: Ring aus Ziegeln, innerer Kreis aus Leuchtkristall.
                BlockState floor = d > 1.5 && d <= 2.5 ? glow : (column.nextFloat() < 0.25F ? cracked : bricks);
                this.set(level, chunkBox, x, oy, z, floor);
                for (int dy = 1; dy <= PILLAR_HEIGHT + 2; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }
                if (d > 3.0 && d < RADIUS - 0.5 && column.nextFloat() < 0.08F) {
                    this.set(level, chunkBox, x, oy + 1, z, cluster);
                }
            }
        }

        // Saeulen
        for (int i = 0; i < PILLARS; i++) {
            float angle = i * Mth.TWO_PI / PILLARS;
            int px = ox + Math.round(Mth.cos(angle) * (RADIUS - 1));
            int pz = oz + Math.round(Mth.sin(angle) * (RADIUS - 1));
            RandomSource pillar = this.positional(px, oy, pz);
            int height = pillar.nextFloat() < 0.4F ? 2 + pillar.nextInt(PILLAR_HEIGHT - 2) : PILLAR_HEIGHT;
            for (int dy = 1; dy <= height; dy++) {
                this.set(level, chunkBox, px, oy + dy, pz, dy == 1 || dy == height ? bricks : wall);
            }
            if (height == PILLAR_HEIGHT) {
                this.set(level, chunkBox, px, oy + height + 1, pz, glow);
            } else {
                this.set(level, chunkBox, px, oy + height, pz, cracked);
            }
        }

        // Altar mit Erinnerungskristall und Opfertruhe
        this.set(level, chunkBox, ox, oy + 1, oz, bricks);
        int vision = Visions.RUIN_VISIONS[this.positional(ox, oy + 1, oz).nextInt(Visions.RUIN_VISIONS.length)];
        this.set(level, chunkBox, ox, oy + 2, oz, ModBlocks.MEMORY_CRYSTAL.get().defaultBlockState().setValue(MemoryCrystalBlock.VISION, vision));
        this.chest(level, chunkBox, random, ox, oy + 1, oz + 2, Direction.NORTH, ModLootTables.KEEPER_SHRINE_CHEST);

        RandomSource structure = this.structureRandom();
        for (int i = 0; i < 2; i++) {
            this.spawn(level, chunkBox, ModEntities.GLOW_MOTH.get(), ox + (i == 0 ? 2 : -2), oy + 3, oz - 2, structure.nextFloat() * 360.0F);
        }
    }
}
