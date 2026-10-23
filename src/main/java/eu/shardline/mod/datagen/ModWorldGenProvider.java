package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.world.ModBiomeModifiers;
import eu.shardline.mod.world.biome.ModBiomes;
import eu.shardline.mod.world.dimension.ModDimensions;
import eu.shardline.mod.world.feature.ModConfiguredFeatures;
import eu.shardline.mod.world.feature.ModPlacedFeatures;
import eu.shardline.mod.world.noise.ModNoiseSettings;
import eu.shardline.mod.world.structure.ModStructures;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Erzeugt alle Datapack-Registry-Eintraege: Dimension, Rauschen, Biome, Features, Strukturen,
 * Schadenstypen und Biome-Modifier.
 */
public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
            .add(Registries.NOISE, ModNoiseSettings::bootstrapNoises)
            .add(Registries.NOISE_SETTINGS, ModNoiseSettings::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(Registries.BIOME, ModBiomes::bootstrap)
            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem)
            .add(Registries.STRUCTURE, ModStructures::bootstrapStructures)
            .add(Registries.STRUCTURE_SET, ModStructures::bootstrapSets)
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Shardline.MODID));
    }
}
