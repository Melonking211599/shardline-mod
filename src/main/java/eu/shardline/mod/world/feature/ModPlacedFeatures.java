package eu.shardline.mod.world.feature;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

/**
 * Platzierte Features. Die Pflanzen nutzen die Hoehenkarte und brauchen daher keinen Bodencheck -
 * ueber dem Void liegt die Hoehenkarte bei Y 0, dort ueberlebt keine Pflanze.
 */
public final class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> SHARD_ORE = key("shard_ore");
    public static final ResourceKey<PlacedFeature> OVERWORLD_SHARD_ORE = key("overworld_shard_ore");
    public static final ResourceKey<PlacedFeature> GLOW_CRYSTAL_VEIN = key("glow_crystal_vein");
    public static final ResourceKey<PlacedFeature> GLOW_CRYSTAL_VEIN_DENSE = key("glow_crystal_vein_dense");
    public static final ResourceKey<PlacedFeature> CRYSTAL_TREES = key("crystal_trees");
    public static final ResourceKey<PlacedFeature> CRYSTAL_TREES_SPARSE = key("crystal_trees_sparse");
    public static final ResourceKey<PlacedFeature> GLOWBLOOM_PATCH = key("glowbloom_patch");
    public static final ResourceKey<PlacedFeature> SHARD_FERN_PATCH = key("shard_fern_patch");
    public static final ResourceKey<PlacedFeature> LUMINOUS_BERRY_PATCH = key("luminous_berry_patch");
    public static final ResourceKey<PlacedFeature> GLOWCAP_FLOOR = key("glowcap_floor");
    public static final ResourceKey<PlacedFeature> HANGING_GLOW_CLUSTER = key("hanging_glow_cluster");
    public static final ResourceKey<PlacedFeature> HANGING_GLOW_CLUSTER_DENSE = key("hanging_glow_cluster_dense");
    public static final ResourceKey<PlacedFeature> GLOW_CLUSTER_PATCH = key("glow_cluster_patch");

    /** Einheitliche Reihenfolge der Deko-Features fuer alle Biome der Splitterweite. */
    public static final List<ResourceKey<PlacedFeature>> VEGETATION_ORDER = List.of(
            CRYSTAL_TREES, CRYSTAL_TREES_SPARSE, GLOWCAP_FLOOR, GLOWBLOOM_PATCH, SHARD_FERN_PATCH, LUMINOUS_BERRY_PATCH,
            GLOW_CLUSTER_PATCH, HANGING_GLOW_CLUSTER, HANGING_GLOW_CLUSTER_DENSE);

    private ModPlacedFeatures() {
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, SHARD_ORE, features.getOrThrow(ModConfiguredFeatures.SHARD_ORE),
                List.of(CountPlacement.of(14), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(200)), BiomeFilter.biome()));
        register(context, OVERWORLD_SHARD_ORE, features.getOrThrow(ModConfiguredFeatures.OVERWORLD_SHARD_ORE),
                List.of(CountPlacement.of(3), InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0)), BiomeFilter.biome()));
        register(context, GLOW_CRYSTAL_VEIN, features.getOrThrow(ModConfiguredFeatures.GLOW_CRYSTAL_VEIN),
                List.of(CountPlacement.of(4), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(200)), BiomeFilter.biome()));
        register(context, GLOW_CRYSTAL_VEIN_DENSE, features.getOrThrow(ModConfiguredFeatures.GLOW_CRYSTAL_VEIN),
                List.of(CountPlacement.of(16), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(200)), BiomeFilter.biome()));

        register(context, CRYSTAL_TREES, features.getOrThrow(ModConfiguredFeatures.CRYSTAL_TREE),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(4, 0.1F, 1), ModBlocks.CRYSTAL_SAPLING.get()));
        register(context, CRYSTAL_TREES_SPARSE, features.getOrThrow(ModConfiguredFeatures.CRYSTAL_TREE),
                VegetationPlacements.treePlacement(RarityFilter.onAverageOnceEvery(3), ModBlocks.CRYSTAL_SAPLING.get()));

        register(context, GLOWBLOOM_PATCH, features.getOrThrow(ModConfiguredFeatures.GLOWBLOOM_PATCH), surfacePatch(2));
        register(context, SHARD_FERN_PATCH, features.getOrThrow(ModConfiguredFeatures.SHARD_FERN_PATCH), surfacePatch(1));
        register(context, LUMINOUS_BERRY_PATCH, features.getOrThrow(ModConfiguredFeatures.LUMINOUS_BERRY_PATCH), surfacePatch(4));

        register(context, GLOWCAP_FLOOR, features.getOrThrow(ModConfiguredFeatures.GLOWCAP),
                List.of(CountPlacement.of(40), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE,
                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                        RandomOffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome()));
        register(context, HANGING_GLOW_CLUSTER, features.getOrThrow(ModConfiguredFeatures.HANGING_GLOW_CLUSTER), hanging(20));
        register(context, HANGING_GLOW_CLUSTER_DENSE, features.getOrThrow(ModConfiguredFeatures.HANGING_GLOW_CLUSTER), hanging(70));
        register(context, GLOW_CLUSTER_PATCH, features.getOrThrow(ModConfiguredFeatures.GLOW_CLUSTER_PATCH), surfacePatch(6));
    }

    private static List<PlacementModifier> surfacePatch(int rarity) {
        return List.of(RarityFilter.onAverageOnceEvery(rarity), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
    }

    /** Haengende Kristallcluster an Inselunterseiten und Hoehlendecken. */
    private static List<PlacementModifier> hanging(int count) {
        return List.of(CountPlacement.of(count), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, 16),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)), BiomeFilter.biome());
    }

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Shardline.id(name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(feature, List.copyOf(modifiers)));
    }
}
