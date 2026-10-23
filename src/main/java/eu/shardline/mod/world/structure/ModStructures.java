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

    // --- Bauteile ---
    public static final DeferredHolder<StructurePieceType, StructurePieceType> RUIN_TOWER_PIECE =
            STRUCTURE_PIECES.register("ruin_tower", () -> (StructurePieceType.ContextlessType) RuinTowerPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> COLOSSUS_ARENA_PIECE =
            STRUCTURE_PIECES.register("colossus_arena", () -> (StructurePieceType.ContextlessType) ColossusArenaPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> QUEEN_ARENA_PIECE =
            STRUCTURE_PIECES.register("queen_arena", () -> (StructurePieceType.ContextlessType) QueenArenaPiece::new);

    // --- Platzierung ---
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<FixedChunkPlacement>> FIXED_CHUNK =
            STRUCTURE_PLACEMENTS.register("fixed_chunk", () -> () -> FixedChunkPlacement.CODEC);

    // --- Datapack-Schluessel ---
    public static final ResourceKey<Structure> RUIN_TOWER_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("ruin_tower"));
    public static final ResourceKey<Structure> COLOSSUS_ARENA_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("colossus_arena"));
    public static final ResourceKey<Structure> QUEEN_ARENA_KEY = ResourceKey.create(Registries.STRUCTURE, Shardline.id("queen_arena"));
    public static final ResourceKey<StructureSet> RUIN_TOWER_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("ruin_towers"));
    public static final ResourceKey<StructureSet> COLOSSUS_ARENA_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("colossus_arenas"));
    public static final ResourceKey<StructureSet> QUEEN_ARENA_SET = ResourceKey.create(Registries.STRUCTURE_SET, Shardline.id("queen_arena"));

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
    }

    public static void bootstrapSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(RUIN_TOWER_SET, new StructureSet(structures.getOrThrow(RUIN_TOWER_KEY),
                new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 1846721)));
        context.register(COLOSSUS_ARENA_SET, new StructureSet(structures.getOrThrow(COLOSSUS_ARENA_KEY),
                new RandomSpreadStructurePlacement(36, 14, RandomSpreadType.LINEAR, 7712381)));
        context.register(QUEEN_ARENA_SET, new StructureSet(structures.getOrThrow(QUEEN_ARENA_KEY),
                new FixedChunkPlacement(0, 0, 90210)));
    }
}
