package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Datengeneratoren. Ausfuehren mit {@code gradlew runData}; die Ausgabe landet in src/generated/resources.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        boolean client = event.includeClient();
        boolean server = event.includeServer();

        // Worldgen und Schadenstypen zuerst: andere Provider brauchen die erweiterten Registries.
        ModWorldGenProvider worldGen = generator.addProvider(server, new ModWorldGenProvider(output, lookup));
        CompletableFuture<HolderLookup.Provider> fullLookup = worldGen.getRegistryProvider();

        generator.addProvider(client, new ModBlockStateProvider(output, fileHelper));
        generator.addProvider(client, new ModItemModelProvider(output, fileHelper));
        generator.addProvider(client, new ModSoundDefinitionsProvider(output, fileHelper));

        ModBlockTagsProvider blockTags = generator.addProvider(server, new ModBlockTagsProvider(output, lookup, fileHelper));
        generator.addProvider(server, new ModItemTagsProvider(output, lookup, blockTags.contentsGetter(), fileHelper));
        generator.addProvider(server, new ModWorldTagsProvider.Biomes(output, fullLookup, fileHelper));
        generator.addProvider(server, new ModWorldTagsProvider.Structures(output, fullLookup, fileHelper));
        generator.addProvider(server, new ModWorldTagsProvider.DamageTypes(output, fullLookup, fileHelper));
        generator.addProvider(server, new ModWorldTagsProvider.EntityTypes(output, lookup, fileHelper));
        generator.addProvider(server, new ModWorldTagsProvider.Enchantments(output, fullLookup, fileHelper));

        generator.addProvider(server, new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModEntityLootProvider::new, LootContextParamSets.ENTITY),
                new LootTableProvider.SubProviderEntry(ModChestLootProvider::new, LootContextParamSets.CHEST)), lookup));
        generator.addProvider(server, new ModRecipeProvider(output, lookup));
        generator.addProvider(server, new AdvancementProvider(output, fullLookup, fileHelper, List.of(new ModAdvancements(), new ModStoryAdvancements())));
        generator.addProvider(server, new ModDataMapProvider(output, lookup));
    }
}
