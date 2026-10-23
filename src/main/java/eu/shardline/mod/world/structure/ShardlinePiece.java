package eu.shardline.mod.world.structure;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Basis fuer die prozeduralen Shardline-Strukturen. Arbeitet mit absoluten Koordinaten und einem
 * gespeicherten Seed, damit ein Bauwerk ueber Chunkgrenzen hinweg identisch generiert wird.
 */
public abstract class ShardlinePiece extends StructurePiece {
    protected final BlockPos origin;
    protected final long seed;

    protected ShardlinePiece(StructurePieceType type, BlockPos origin, long seed, BoundingBox box) {
        super(type, 0, box);
        this.origin = origin;
        this.seed = seed;
    }

    protected ShardlinePiece(StructurePieceType type, CompoundTag tag) {
        super(type, tag);
        this.origin = NbtUtils.readBlockPos(tag, "Origin").orElse(BlockPos.ZERO);
        this.seed = tag.getLong("Seed");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put("Origin", NbtUtils.writeBlockPos(this.origin));
        tag.putLong("Seed", this.seed);
    }

    /** Zufall, der nur von der Position und dem Bauwerk abhaengt (identisch in jedem Chunk-Durchlauf). */
    protected RandomSource positional(int x, int y, int z) {
        return RandomSource.create(Mth.getSeed(x, y, z) ^ this.seed);
    }

    /** Zufall fuer Entscheidungen, die das ganze Bauwerk betreffen. */
    protected RandomSource structureRandom() {
        return RandomSource.create(this.seed);
    }

    protected static double horizontalDistance(int dx, int dz) {
        return Math.sqrt(dx * dx + dz * dz);
    }

    protected void set(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    /** Setzt den Block nur, wenn dort noch Luft oder ersetzbares Material ist. */
    protected void fill(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos)) {
            BlockState current = level.getBlockState(pos);
            if (current.isAir() || current.canBeReplaced()) {
                level.setBlock(pos, state, 2);
            }
        }
    }

    protected void clear(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos) && !level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    protected void chest(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, int x, int y, int z, Direction facing,
                         ResourceKey<LootTable> lootTable) {
        this.createChest(level, chunkBox, random, new BlockPos(x, y, z), lootTable,
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
    }

    /** Erzeugt eine Kreatur als Teil des Bauwerks (nur im Chunk, der die Position enthaelt). */
    @Nullable
    protected <T extends Mob> T spawn(WorldGenLevel level, BoundingBox chunkBox, EntityType<T> type, int x, int y, int z, float yaw) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!chunkBox.isInside(pos)) {
            return null;
        }
        T mob = type.create(level.getLevel());
        if (mob == null) {
            return null;
        }
        mob.moveTo(x + 0.5, y, z + 0.5, yaw, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        mob.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mob);
        return mob;
    }

    /** Begrenzt eine Schleife auf den Schnitt von Bauwerk und aktuellem Chunk. */
    protected static int minX(BoundingBox a, BoundingBox b) {
        return Math.max(a.minX(), b.minX());
    }

    protected static int maxX(BoundingBox a, BoundingBox b) {
        return Math.min(a.maxX(), b.maxX());
    }

    protected static int minZ(BoundingBox a, BoundingBox b) {
        return Math.max(a.minZ(), b.minZ());
    }

    protected static int maxZ(BoundingBox a, BoundingBox b) {
        return Math.min(a.maxZ(), b.maxZ());
    }
}
