package eu.shardline.mod.world.structure;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Die Strukturtypen. Alle bauen ihr eigenes Fundament und funktionieren daher auch
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

    /**
     * Wie viele der vier Ecken ({@code reach} Bloecke von der Mitte) Boden in hoechstens
     * {@code tolerance} Bloecken Hoehenunterschied haben.
     */
    static int groundedCorners(Structure.GenerationContext context, int x, int y, int z, int reach, int tolerance) {
        int grounded = 0;
        for (int[] corner : new int[][] {{-reach, -reach}, {reach, -reach}, {-reach, reach}, {reach, reach}}) {
            int cy = groundHeight(context, x + corner[0], z + corner[1]);
            if (cy >= 0 && Math.abs(cy - y) <= tolerance) {
                grounded++;
            }
        }
        return grounded;
    }

    /**
     * Gemeinsame Platzierung der kleinen Splitterweite-Bauwerke: Chunk-Mitte auf festem Boden,
     * mindestens drei Ecken mit Boden, nicht im Zentrum.
     */
    static Optional<Structure.GenerationStub> placeOnGround(Structure.GenerationContext context, int reach,
                                                            BiFunction<BlockPos, Long, ShardlinePiece> piece) {
        ChunkPos chunk = context.chunkPos();
        if (nearCenter(chunk)) {
            return Optional.empty();
        }
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int y = groundHeight(context, x, z);
        if (y < 0 || groundedCorners(context, x, y, z, reach, 4) < 3) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, y, z);
        long seed = context.random().nextLong();
        return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(piece.apply(origin, seed))));
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

    /** Hueter-Schrein im Kristallwald. */
    public static class KeeperShrine extends Structure {
        public static final MapCodec<KeeperShrine> CODEC = simpleCodec(KeeperShrine::new);

        public KeeperShrine(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 5, KeeperShrinePiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.KEEPER_SHRINE.get();
        }
    }

    /** Hueter-Ruinen: eingestuerzter Saal mit Erinnerungskristall und Lore-Truhe, in allen Biomen der Splitterweite. */
    public static class KeeperRuin extends Structure {
        public static final MapCodec<KeeperRuin> CODEC = simpleCodec(KeeperRuin::new);

        public KeeperRuin(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 6, KeeperRuinPiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.KEEPER_RUIN.get();
        }
    }

    /** Handelsposten mit Splitterhaendler. */
    public static class TraderOutpost extends Structure {
        public static final MapCodec<TraderOutpost> CODEC = simpleCodec(TraderOutpost::new);

        public TraderOutpost(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 4, TraderOutpostPiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.TRADER_OUTPOST.get();
        }
    }

    /** Vergrabene Aschegruft in den Aschefeldern. */
    public static class AshCrypt extends Structure {
        public static final MapCodec<AshCrypt> CODEC = simpleCodec(AshCrypt::new);

        public AshCrypt(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 5, AshCryptPiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.ASH_CRYPT.get();
        }
    }

    /** Kristallgeode in den Leuchtenden Hoehlen. */
    public static class CrystalGeode extends Structure {
        public static final MapCodec<CrystalGeode> CODEC = simpleCodec(CrystalGeode::new);

        public CrystalGeode(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 6, CrystalGeodePiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.CRYSTAL_GEODE.get();
        }
    }

    /** Splitterkrater in der Oberwelt: nur an Land, nicht unter Wasser. */
    public static class ShardCrater extends Structure {
        public static final MapCodec<ShardCrater> CODEC = simpleCodec(ShardCrater::new);

        public ShardCrater(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            ChunkPos chunk = context.chunkPos();
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            int floor = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            if (y != floor || y <= context.chunkGenerator().getSeaLevel() || groundedCorners(context, x, y, z, 6, 3) < 4) {
                return Optional.empty();
            }
            BlockPos origin = new BlockPos(x, y, z);
            long seed = context.random().nextLong();
            return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new ShardCraterPiece(origin, seed))));
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.SHARD_CRATER.get();
        }
    }

    /** Ueberlebendenlager (Kapitel 7-9): runde Lichtung, braucht ebenen Boden in allen Richtungen. */
    public static class SurvivorCamp extends Structure {
        public static final MapCodec<SurvivorCamp> CODEC = simpleCodec(SurvivorCamp::new);

        public SurvivorCamp(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 8, SurvivorCampPiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.SURVIVOR_CAMP.get();
        }
    }

    /** Hueter-Bibliothek (Kapitel 8-9): Saal mit Veyl. */
    public static class KeeperLibrary extends Structure {
        public static final MapCodec<KeeperLibrary> CODEC = simpleCodec(KeeperLibrary::new);

        public KeeperLibrary(StructureSettings settings) {
            super(settings);
        }

        @Override
        protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
            return placeOnGround(context, 7, KeeperLibraryPiece::new);
        }

        @Override
        public StructureType<?> type() {
            return ModStructures.KEEPER_LIBRARY.get();
        }
    }
}
