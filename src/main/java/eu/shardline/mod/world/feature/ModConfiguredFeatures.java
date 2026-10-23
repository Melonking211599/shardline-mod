package eu.shardline.mod.world.feature;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

/**
 * Konfigurierte Features: Erze, Kristallbaeume, leuchtende Pflanzen und Kristallcluster.
 */
public final class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SHARD_ORE = key("shard_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_SHARD_ORE = key("overworld_shard_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOW_CRYSTAL_VEIN = key("glow_crystal_vein");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTAL_TREE = key("crystal_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOWBLOOM_PATCH = key("glowbloom_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SHARD_FERN_PATCH = key("shard_fern_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LUMINOUS_BERRY_PATCH = key("luminous_berry_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOWCAP = key("glowcap");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HANGING_GLOW_CLUSTER = key("hanging_glow_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOW_CLUSTER_PATCH = key("glow_cluster_patch");

    private ModConfiguredFeatures() {
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        Block shardstone = ModBlocks.SHARDSTONE.get();

        register(context, SHARD_ORE, Feature.ORE, new OreConfiguration(new BlockMatchTest(shardstone),
                ModBlocks.SHARD_ORE.get().defaultBlockState(), 8));
        register(context, OVERWORLD_SHARD_ORE, Feature.ORE, new OreConfiguration(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                ModBlocks.DEEPSLATE_SHARD_ORE.get().defaultBlockState(), 5, 0.5F));
        register(context, GLOW_CRYSTAL_VEIN, Feature.ORE, new OreConfiguration(new BlockMatchTest(shardstone),
                ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState(), 12));

        register(context, CRYSTAL_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.CRYSTAL_LOG.get()),
                new CherryTrunkPlacer(6, 2, 0, UniformInt.of(1, 3), UniformInt.of(2, 4), UniformInt.of(-4, -3), UniformInt.of(-1, 0)),
                BlockStateProvider.simple(ModBlocks.CRYSTAL_LEAVES.get()),
                new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25F, 0.5F, 0.16666667F, 0.33333334F),
                new TwoLayersFeatureSize(1, 0, 2))
                .dirt(BlockStateProvider.simple(ModBlocks.CRYSTAL_MOSS.get()))
                .ignoreVines()
                .build());

        register(context, GLOWBLOOM_PATCH, Feature.RANDOM_PATCH, patch(ModBlocks.GLOWBLOOM.get(), 32));
        register(context, SHARD_FERN_PATCH, Feature.RANDOM_PATCH, patch(ModBlocks.SHARD_FERN.get(), 48));
        register(context, LUMINOUS_BERRY_PATCH, Feature.RANDOM_PATCH, patch(ModBlocks.LUMINOUS_BERRY_BUSH.get(), 16));
        register(context, GLOWCAP, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.GLOWCAP.get())));
        register(context, HANGING_GLOW_CLUSTER, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(
                ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN))));
        register(context, GLOW_CLUSTER_PATCH, Feature.RANDOM_PATCH, new RandomPatchConfiguration(12, 5, 2,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(
                        ModBlocks.GLOW_CRYSTAL_CLUSTER.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP))))));
    }

    private static RandomPatchConfiguration patch(Block block, int tries) {
        return new RandomPatchConfiguration(tries, 6, 2,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block))));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Shardline.id(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }
}
