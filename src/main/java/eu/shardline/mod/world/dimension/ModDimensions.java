package eu.shardline.mod.world.dimension;

import com.mojang.datafixers.util.Pair;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.world.biome.ModBiomes;
import eu.shardline.mod.world.noise.ModNoiseSettings;
import java.util.List;
import java.util.OptionalLong;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Die Splitterweite: schwebende Kristallinseln ueber einem dunklen Void.
 * Dauerhafte Nacht (fixed_time 18000), Massstab 4:1 zur Oberwelt, eigene Himmelseffekte.
 */
public final class ModDimensions {
    public static final ResourceKey<LevelStem> SPLITTERWEITE_STEM = ResourceKey.create(Registries.LEVEL_STEM, Shardline.id("splitterweite"));
    public static final ResourceKey<Level> SPLITTERWEITE_LEVEL = ResourceKey.create(Registries.DIMENSION, Shardline.id("splitterweite"));
    public static final ResourceKey<DimensionType> SPLITTERWEITE_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, Shardline.id("splitterweite"));
    /** Schluessel der DimensionSpecialEffects (Himmel/Nebel) auf dem Client. */
    public static final ResourceLocation SPLITTERWEITE_EFFECTS = Shardline.id("splitterweite");

    private ModDimensions() {
    }

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(SPLITTERWEITE_TYPE, new DimensionType(
                OptionalLong.of(18000L),
                true,
                false,
                false,
                false,
                4.0,
                false,
                false,
                0,
                256,
                256,
                BlockTags.INFINIBURN_END,
                SPLITTERWEITE_EFFECTS,
                0.08F,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        NoiseBasedChunkGenerator generator = createGenerator(context.lookup(Registries.BIOME), context.lookup(Registries.NOISE_SETTINGS));
        context.register(SPLITTERWEITE_STEM, new LevelStem(types.getOrThrow(SPLITTERWEITE_TYPE), generator));
    }

    /** Chunk-Generator der Splitterweite (auch von den GameTests genutzt). */
    public static NoiseBasedChunkGenerator createGenerator(HolderGetter<Biome> biomes, HolderGetter<NoiseGeneratorSettings> noiseSettings) {
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(List.of(
                Pair.of(Climate.parameters(0.0F, 0.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(ModBiomes.CRYSTAL_FOREST)),
                Pair.of(Climate.parameters(0.55F, -0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(ModBiomes.ASH_FIELDS)),
                Pair.of(Climate.parameters(-0.55F, -0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(ModBiomes.GLOWING_CAVES)))));
        return new NoiseBasedChunkGenerator(biomeSource, noiseSettings.getOrThrow(ModNoiseSettings.SPLITTERWEITE));
    }
}
