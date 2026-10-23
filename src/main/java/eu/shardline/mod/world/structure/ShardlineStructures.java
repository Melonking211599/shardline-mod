package eu.shardline.mod.world.structure;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Die drei Strukturtypen. Alle bauen ihr eigenes Fundament und funktionieren daher auch
 * am Rand schwebender Inseln.
 */
public final class ShardlineStructures {
    /** Chunks rund um den Ursprung bleiben fuer die Koeniginnen-Arena frei. */
    private static final int CENTER_EXCLUSION_CHUNKS = 6;

    private ShardlineStructures() {
    }

    /** Hoehe des obersten festen Blocks oder -1 ueber dem Void. */
    static int groundHeight(Structure.GenerationContext context, int x, int z) {
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        return y <= context.heightAccessor().getMinBuildHeight() + 4 ? -1 : y;
    }

    static boolean nearCenter(ChunkPos chunk) {
        return Math.abs(chunk.x) <= CENTER_EXCLUSION_CHUNKS && Math.abs(chunk.z) <= CENTER_EXCLUSION_CHUNKS;
    }

    /** Ruinen-Turm: nur auf festem Boden, nicht im Zentrum. */
    public static class RuinTower extends Structure {
        public static final MapCodec<RuinTower> CODEC = simpleCodec(RuinTower::new);

        public RuinTower(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            ChunkPos chunk = context.chunkPos();
            if (nearCenter(chunk)) {
                return Optional.empty();
            }
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            int y = groundHeight(context, x, z);
            // Mindestens drei Ecken des Turms muessen Boden haben.
            int grounded = 0;
            for (int[] corner : new int[][] {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
                int cy = groundHeight(context, x + corner[0], z + corner[1]);
                if (cy >= 0 && Math.abs(cy - y) <= 6) {
                    grounded++;
                }
            }
            if (y < 0 || grounded < 3) {
                return Optional.empty();
            }
            BlockPos origin = new BlockPos(x, y, z);
            long seed = context.random().nextLong();
            return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new RuinTowerPiece(origin, seed))));
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.RUIN_TOWER.get();
        }
    }

    /** Koloss-Arena: auf dem Boden, sonst schwebend auf Hoehe 96. */
    public static class ColossusArena extends Structure {
        public static final MapCodec<ColossusArena> CODEC = simpleCodec(ColossusArena::new);

        public ColossusArena(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            ChunkPos chunk = context.chunkPos();
            if (nearCenter(chunk)) {
                return Optional.empty();
            }
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            int ground = groundHeight(context, x, z);
            int y = ground < 0 ? 96 : Math.min(ground, 170);
            BlockPos origin = new BlockPos(x, y, z);
            long seed = context.random().nextLong();
            return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new ColossusArenaPiece(origin, seed))));
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.COLOSSUS_ARENA.get();
        }
    }

    /** Koeniginnen-Arena: immer im Zentrumschunk auf fester Hoehe. */
    public static class QueenArena extends Structure {
        public static final MapCodec<QueenArena> CODEC = simpleCodec(QueenArena::new);

        public QueenArena(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            ChunkPos chunk = context.chunkPos();
            BlockPos origin = new BlockPos(chunk.getMiddleBlockX(), QueenArenaPiece.FLOOR_Y, chunk.getMiddleBlockZ());
            long seed = context.random().nextLong();
            return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new QueenArenaPiece(origin, seed))));
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.QUEEN_ARENA.get();
        }
    }
}
