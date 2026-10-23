package eu.shardline.mod.world;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.world.feature.ModPlacedFeatures;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Aenderungen an Oberwelt-Biomen: seltenes Shard-Erz in der Tiefe und naechtliche Shardline-Mobs.
 * Die Seltenheit der Spawns wird zusaetzlich ueber die Config gesteuert.
 */
public final class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> OVERWORLD_SHARD_ORE = key("overworld_shard_ore");
    public static final ResourceKey<BiomeModifier> OVERWORLD_MONSTERS = key("overworld_monsters");
    public static final ResourceKey<BiomeModifier> OVERWORLD_GLOW_MOTHS = key("overworld_glow_moths");

    private ModBiomeModifiers() {
    }

    private static ResourceKey<BiomeModifier> key(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Shardline.id(name));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderSet<Biome> overworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);

        context.register(OVERWORLD_SHARD_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(features.getOrThrow(ModPlacedFeatures.OVERWORLD_SHARD_ORE)), GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(OVERWORLD_MONSTERS, new BiomeModifiers.AddSpawnsBiomeModifier(overworld, List.of(
                new MobSpawnSettings.SpawnerData(ModEntities.SHARD_CREEPER.get(), 8, 1, 1),
                new MobSpawnSettings.SpawnerData(ModEntities.ASH_WALKER.get(), 8, 1, 2),
                new MobSpawnSettings.SpawnerData(ModEntities.CRYSTAL_SPIDER.get(), 6, 1, 1),
                new MobSpawnSettings.SpawnerData(ModEntities.SHADOW_FOX.get(), 6, 1, 1))));

        context.register(OVERWORLD_GLOW_MOTHS, new BiomeModifiers.AddSpawnsBiomeModifier(overworld, List.of(
                new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 6, 1, 2))));
    }
}
