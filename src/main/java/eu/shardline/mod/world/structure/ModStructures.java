package eu.shardline.mod.world.structure;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.entity.ModEntities;
import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registrierung der Strukturtypen, Bauteile und Platzierungen sowie die Datengenerator-Eintraege.
 */
public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Shardline.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Shardline.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, Shardline.MODID);

    // --- Typen ---
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.RuinTower>> RUIN_TOWER =
            STRUCTURE_TYPES.register("ruin_tower", () -> () -> ShardlineStructures.RuinTower.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.ColossusArena>> COLOSSUS_ARENA =
            STRUCTURE_TYPES.register("colossus_arena", () -> () -> ShardlineStructures.ColossusArena.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.QueenArena>> QUEEN_ARENA =
            STRUCTURE_TYPES.register("queen_arena", () -> () -> ShardlineStructures.QueenArena.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.KeeperShrine>> KEEPER_SHRINE =
            STRUCTURE_TYPES.register("keeper_shrine", () -> () -> ShardlineStructures.KeeperShrine.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.TraderOutpost>> TRADER_OUTPOST =
            STRUCTURE_TYPES.register("trader_outpost", () -> () -> ShardlineStructures.TraderOutpost.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.AshCrypt>> ASH_CRYPT =
            STRUCTURE_TYPES.register("ash_crypt", () -> () -> ShardlineStructures.AshCrypt.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.CrystalGeode>> CRYSTAL_GEODE =
            STRUCTURE_TYPES.register("crystal_geode", () -> () -> ShardlineStructures.CrystalGeode.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.ShardCrater>> SHARD_CRATER =
            STRUCTURE_TYPES.register("shard_crater", () -> () -> ShardlineStructures.ShardCrater.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<ShardlineStructures.KeeperRuin>> KEEPER_RUIN =
            STRUCTURE_TYPES.register("keeper_ruin", () -> () -> ShardlineStructures.KeeperRuin.CODEC);

    // --- Bauteile ---
    public static final DeferredHolder<StructurePieceType, StructurePieceType> RUIN_TOWER_PIECE =
            STRUCTURE_PIECES.register("ruin_tower", () -> (StructurePieceType.ContextlessType) RuinTowerPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> COLOSSUS_ARENA_PIECE =
            STRUCTURE_PIECES.register("colossus_arena", () -> (StructurePieceType.ContextlessType) ColossusArenaPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> QUEEN_ARENA_PIECE =
            STRUCTURE_PIECES.register("queen_arena", () -> (StructurePieceType.ContextlessType) QueenArenaPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> KEEPER_SHRINE_PIECE =
            STRUCTURE_PIECES.register("keeper_shrine", () -> (StructurePieceType.ContextlessType) KeeperShrinePiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> TRADER_OUTPOST_PIECE =
            STRUCTURE_PIECES.register("trader_outpost", () -> (StructurePieceType.ContextlessType) TraderOutpostPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ASH_CRYPT_PIECE =
            STRUCTURE_PIECES.register("ash_crypt", () -> (StructurePieceType.ContextlessType) AshCryptPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> CRYSTAL_GEODE_PIECE =
            STRUCTURE_PIECES.register("crystal_geode", () -> (StructurePieceType.ContextlessType) CrystalGeodePiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SHARD_CRATER_PIECE =
            STRUCTURE_PIECES.register("shard_crater", () -> (StructurePieceType.ContextlessType) ShardCraterPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> KEEPER_RUIN_PIECE =
            STRUCTURE_PIECES.register("keeper_ruin", () -> (StructurePieceType.ContextlessType) KeeperRuinPiece::new);

    // --- Platzierung ---
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<FixedChunkPlacement>> FIXED_CHUNK =
            STRUCTURE_PLACEMENTS.register("fixed_chunk", () -> () -> FixedChunkPlacement.CODEC);

    // --- Datapack-Schluessel ---
    public static final ResourceKey<Structure> RUIN_TOWER_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("ruin_tower"));
    public static final ResourceKey<Structure> COLOSSUS_ARENA_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("colossus_arena"));
    public static final ResourceKey<Structure> QUEEN_ARENA_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("queen_arena"));
    public static final ResourceKey<Structure> KEEPER_SHRINE_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("keeper_shrine"));
    public static final ResourceKey<Structure> TRADER_OUTPOST_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("trader_outpost"));
    public static final ResourceKey<Structure> ASH_CRYPT_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("ash_crypt"));
    public static final ResourceKey<Structure> CRYSTAL_GEODE_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("crystal_geode"));
    public static final ResourceKey<Structure> SHARD_CRATER_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("shard_crater"));
    public static final ResourceKey<Structure> KEEPER_RUIN_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("keeper_ruin"));
    public static final ResourceKey<StructureSet> RUIN_TOWER_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("ruin_towers"));
    public static final ResourceKey<StructureSet> COLOSSUS_ARENA_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("colossus_arenas"));
    public static final ResourceKey<StructureSet> QUEEN_ARENA_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("queen_arena"));
    public static final ResourceKey<StructureSet> KEEPER_SHRINE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("keeper_shrines"));
    public static final ResourceKey<StructureSet> TRADER_OUTPOST_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("trader_outposts"));
    public static final ResourceKey<StructureSet> ASH_CRYPT_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("ash_crypts"));
    public static final ResourceKey<StructureSet> CRYSTAL_GEODE_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("crystal_geodes"));
    public static final ResourceKey<StructureSet> SHARD_CRATER_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("shard_craters"));
    public static final ResourceKey<StructureSet> KEEPER_RUIN_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("keeper_ruins"));

    private ModStructures() {
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(RUIN_TOWER_KEY, new ShardlineStructures.RuinTower(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_RUIN_TOWER),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(ModEntities.SHATTERED_KNIGHT.get(), 1, 1, 1)))),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(COLOSSUS_ARENA_KEY, new ShardlineStructures.ColossusArena(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_COLOSSUS_ARENA),
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(QUEEN_ARENA_KEY, new ShardlineStructures.QueenArena(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_QUEEN_ARENA),
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(KEEPER_SHRINE_KEY, new ShardlineStructures.KeeperShrine(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_KEEPER_SHRINE),
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(TRADER_OUTPOST_KEY, new ShardlineStructures.TraderOutpost(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_TRADER_OUTPOST),
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(ASH_CRYPT_KEY, new ShardlineStructures.AshCrypt(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_ASH_CRYPT),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(ModEntities.ASH_WALKER.get(), 2, 1, 1),
                                new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SPIDER.get(), 1, 1, 1)))),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(CRYSTAL_GEODE_KEY, new ShardlineStructures.CrystalGeode(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_CRYSTAL_GEODE),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SPIDER.get(), 1, 1, 2)))),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(KEEPER_RUIN_KEY, new ShardlineStructures.KeeperRuin(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_KEEPER_RUIN),
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));

        context.register(SHARD_CRATER_KEY, new ShardlineStructures.ShardCrater(new Structure.StructureSettings(
                biomes.getOrThrow(ModTags.Biomes.HAS_SHARD_CRATER),
                Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));
    }

    public static void bootstrapSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(RUIN_TOWER_SET, new StructureSet(structures.getOrThrow(RUIN_TOWER_KEY),
                new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 1846721)));
        context.register(COLOSSUS_ARENA_SET, new StructureSet(structures.getOrThrow(COLOSSUS_ARENA_KEY),
                new RandomSpreadStructurePlacement(36, 14, RandomSpreadType.LINEAR, 7712381)));
        context.register(QUEEN_ARENA_SET, new StructureSet(structures.getOrThrow(QUEEN_ARENA_KEY),
                new FixedChunkPlacement(0, 0, 90210)));
        context.register(KEEPER_SHRINE_SET, new StructureSet(structures.getOrThrow(KEEPER_SHRINE_KEY),
                new RandomSpreadStructurePlacement(26, 9, RandomSpreadType.LINEAR, 4183920)));
        context.register(TRADER_OUTPOST_SET, new StructureSet(structures.getOrThrow(TRADER_OUTPOST_KEY),
                new RandomSpreadStructurePlacement(32, 12, RandomSpreadType.LINEAR, 5529017)));
        context.register(ASH_CRYPT_SET, new StructureSet(structures.getOrThrow(ASH_CRYPT_KEY),
                new RandomSpreadStructurePlacement(24, 8, RandomSpreadType.LINEAR, 6610443)));
        context.register(CRYSTAL_GEODE_SET, new StructureSet(structures.getOrThrow(CRYSTAL_GEODE_KEY),
                new RandomSpreadStructurePlacement(22, 7, RandomSpreadType.LINEAR, 3307751)));
        context.register(KEEPER_RUIN_SET, new StructureSet(structures.getOrThrow(KEEPER_RUIN_KEY),
                new RandomSpreadStructurePlacement(28, 10, RandomSpreadType.LINEAR, 9127734)));
        context.register(SHARD_CRATER_SET, new StructureSet(structures.getOrThrow(SHARD_CRATER_KEY),
                new RandomSpreadStructurePlacement(40, 14, RandomSpreadType.LINEAR, 8812634)));
    }
}
