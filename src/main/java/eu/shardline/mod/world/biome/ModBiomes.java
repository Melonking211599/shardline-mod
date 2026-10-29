package eu.shardline.mod.world.biome;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.world.feature.ModPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Die drei Biome der Splitterweite.
 */
public final class ModBiomes {
    public static final ResourceKey<Biome> CRYSTAL_FOREST = key("crystal_forest");
    public static final ResourceKey<Biome> ASH_FIELDS = key("ash_fields");
    public static final ResourceKey<Biome> GLOWING_CAVES = key("glowing_caves");

    private ModBiomes() {
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Shardline.id(name));
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(CRYSTAL_FOREST, crystalForest(features, carvers));
        context.register(ASH_FIELDS, ashFields(features, carvers));
        context.register(GLOWING_CAVES, glowingCaves(features, carvers));
    }

    private static Biome crystalForest(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_CREEPER.get(), 40, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SPIDER.get(), 20, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHADOW_FOX.get(), 15, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_SCOUT.get(), 15, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHATTERED_KNIGHT.get(), 3, 1, 1));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_GOLEM.get(), 6, 1, 1));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SNAIL.get(), 10, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.VOID_RAY.get(), 4, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_TRADER.get(), 1, 1, 1));
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 12, 2, 3));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        commonFeatures(generation, features);
        vegetation(generation, features, ModPlacedFeatures.CRYSTAL_TREES, ModPlacedFeatures.GLOWBLOOM_PATCH, ModPlacedFeatures.SHARD_FERN_PATCH,
                ModPlacedFeatures.LUMINOUS_BERRY_PATCH, ModPlacedFeatures.HANGING_GLOW_CLUSTER);

        return biome(0.7F, spawns, generation, new BiomeSpecialEffects.Builder()
                .fogColor(0x3B2A66).skyColor(0x140C2A).waterColor(0x7A5CFF).waterFogColor(0x1A0F33)
                .grassColorOverride(0x6FE3D8).foliageColorOverride(0xC08BFF)
                .ambientParticle(new AmbientParticleSettings(ModParticles.SHARD_SPARKLE.get(), 0.006F)));
    }

    private static Biome ashFields(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.ASH_WALKER.get(), 50, 1, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_CREEPER.get(), 30, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHADOW_FOX.get(), 20, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_SCOUT.get(), 15, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHATTERED_KNIGHT.get(), 3, 1, 1));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.VOID_RAY.get(), 4, 1, 2));
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 4, 1, 2));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        commonFeatures(generation, features);
        vegetation(generation, features, ModPlacedFeatures.CRYSTAL_TREES_SPARSE, ModPlacedFeatures.SHARD_FERN_PATCH,
                ModPlacedFeatures.GLOW_CLUSTER_PATCH, ModPlacedFeatures.HANGING_GLOW_CLUSTER);

        return biome(1.2F, spawns, generation, new BiomeSpecialEffects.Builder()
                .fogColor(0x4A3A3A).skyColor(0x1C1414).waterColor(0x5A4A6A).waterFogColor(0x201820)
                .grassColorOverride(0x8A7A7A).foliageColorOverride(0x9A6A8A)
                .ambientParticle(new AmbientParticleSettings(net.minecraft.core.particles.ParticleTypes.WHITE_ASH, 0.08F)));
    }

    private static Biome glowingCaves(HolderGetter<PlacedFeature> features, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SPIDER.get(), 40, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_CREEPER.get(), 20, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SHARD_SCOUT.get(), 15, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.ASH_WALKER.get(), 10, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_GOLEM.get(), 8, 1, 1));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SNAIL.get(), 14, 1, 3));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.VOID_RAY.get(), 3, 1, 1));
        spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 20, 2, 4));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        generation.addCarver(GenerationStep.Carving.AIR, carvers.getOrThrow(Carvers.CAVE));
        generation.addCarver(GenerationStep.Carving.AIR, carvers.getOrThrow(Carvers.CAVE_EXTRA_UNDERGROUND));
        commonFeatures(generation, features);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, features.getOrThrow(ModPlacedFeatures.GLOW_CRYSTAL_VEIN_DENSE));
        vegetation(generation, features, ModPlacedFeatures.GLOWCAP_FLOOR, ModPlacedFeatures.HANGING_GLOW_CLUSTER_DENSE,
                ModPlacedFeatures.GLOW_CLUSTER_PATCH, ModPlacedFeatures.GLOWBLOOM_PATCH);

        return biome(0.4F, spawns, generation, new BiomeSpecialEffects.Builder()
                .fogColor(0x1E3A5A).skyColor(0x081428).waterColor(0x4AD9FF).waterFogColor(0x0A1E33)
                .grassColorOverride(0x4AD9C8).foliageColorOverride(0x6AC8FF)
                .ambientParticle(new AmbientParticleSettings(ModParticles.SHARD_SPARKLE.get(), 0.012F)));
    }

    /**
     * Fuegt Deko-Features in der globalen Reihenfolge {@link ModPlacedFeatures#VEGETATION_ORDER} hinzu.
     * Minecraft verlangt, dass alle Biome einer Dimension dieselbe relative Reihenfolge nutzen,
     * sonst bricht die Chunk-Generierung mit "Feature order cycle" ab.
     */
    @SafeVarargs
    private static void vegetation(BiomeGenerationSettings.Builder generation, HolderGetter<PlacedFeature> features,
                                   ResourceKey<PlacedFeature>... wanted) {
        java.util.Set<ResourceKey<PlacedFeature>> selection = java.util.Set.of(wanted);
        for (ResourceKey<PlacedFeature> key : ModPlacedFeatures.VEGETATION_ORDER) {
            if (selection.contains(key)) {
                generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, features.getOrThrow(key));
            }
        }
    }

    private static void commonFeatures(BiomeGenerationSettings.Builder generation, HolderGetter<PlacedFeature> features) {
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, features.getOrThrow(ModPlacedFeatures.SHARD_ORE));
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, features.getOrThrow(ModPlacedFeatures.GLOW_CRYSTAL_VEIN));
    }

    private static Biome biome(float temperature, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation,
                               BiomeSpecialEffects.Builder effects) {
        effects.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
                .backgroundMusic(new Music(ModSounds.MUSIC_SPLITTERWEITE, 12000, 24000, false));
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(temperature)
                .downfall(0.0F)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
