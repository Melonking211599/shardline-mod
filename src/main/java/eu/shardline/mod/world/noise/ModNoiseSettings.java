package eu.shardline.mod.world.noise;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.world.biome.ModBiomes;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * Gelaendeform der Splitterweite: eine 3D-Rauschfunktion erzeugt Kristallinseln, eine 2D-Maske
 * reisst grosse Void-Luecken, und zwei Hoehengradienten begrenzen die Inseln auf ca. Y 40-190.
 */
public final class ModNoiseSettings {
    public static final ResourceKey<NoiseGeneratorSettings> SPLITTERWEITE = ResourceKey.create(Registries.NOISE_SETTINGS, Shardline.id("splitterweite"));
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLANDS = ResourceKey.create(Registries.NOISE, Shardline.id("splitterweite_islands"));
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_MASK = ResourceKey.create(Registries.NOISE, Shardline.id("splitterweite_island_mask"));

    private ModNoiseSettings() {
    }

    public static void bootstrapNoises(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(ISLANDS, new NormalNoise.NoiseParameters(-6, 1.0, 0.7, 0.4, 0.25));
        context.register(ISLAND_MASK, new NormalNoise.NoiseParameters(-8, 1.0, 0.5));
    }

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        HolderGetter<NormalNoise.NoiseParameters> noises = context.lookup(Registries.NOISE);

        DensityFunction islands = DensityFunctions.noise(noises.getOrThrow(ISLANDS), 1.0, 1.5);
        DensityFunction mask = DensityFunctions.noise(noises.getOrThrow(ISLAND_MASK), 1.0, 0.0);
        DensityFunction verticalFade = DensityFunctions.min(
                DensityFunctions.yClampedGradient(24, 76, -1.6, 0.0),
                DensityFunctions.yClampedGradient(140, 196, 0.0, -1.6));
        DensityFunction raw = DensityFunctions.add(
                DensityFunctions.add(islands, DensityFunctions.mul(DensityFunctions.constant(0.55), mask)),
                DensityFunctions.add(verticalFade, DensityFunctions.constant(-0.3)));
        DensityFunction finalDensity = DensityFunctions.mul(DensityFunctions.interpolated(raw), DensityFunctions.constant(0.64)).squeeze();

        DensityFunction temperature = DensityFunctions.noise(noises.getOrThrow(Noises.TEMPERATURE), 2.0, 0.0);
        DensityFunction vegetation = DensityFunctions.noise(noises.getOrThrow(Noises.VEGETATION), 2.0, 0.0);
        DensityFunction zero = DensityFunctions.zero();

        NoiseRouter router = new NoiseRouter(
                zero, zero, zero, zero,
                temperature, vegetation,
                zero, zero, zero, zero,
                finalDensity, finalDensity,
                zero, zero, zero);

        context.register(SPLITTERWEITE, new NoiseGeneratorSettings(
                NoiseSettings.create(0, 256, 1, 2),
                ModBlocks.SHARDSTONE.get().defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                router,
                surfaceRules(),
                List.of(),
                0,
                false,
                false,
                false,
                false));
    }

    private static SurfaceRules.RuleSource surfaceRules() {
        SurfaceRules.RuleSource moss = SurfaceRules.state(ModBlocks.CRYSTAL_MOSS.get().defaultBlockState());
        SurfaceRules.RuleSource ash = SurfaceRules.state(ModBlocks.ASH_BLOCK.get().defaultBlockState());
        SurfaceRules.RuleSource glow = SurfaceRules.state(ModBlocks.GLOW_CRYSTAL_BLOCK.get().defaultBlockState());

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.CRYSTAL_FOREST),
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, moss)),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.ASH_FIELDS), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, ash),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, ash))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.GLOWING_CAVES), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, moss),
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING,
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.2), glow)))));
    }
}
