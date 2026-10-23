package eu.shardline.mod.datagen;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.item.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data-Maps: Aschestaub und Kristallholz als Brennstoff, Pflanzen kompostierbar.
 */
public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(NeoForgeDataMaps.FURNACE_FUELS)
                .add(ModItems.ASH_DUST, new FurnaceFuel(800), false)
                .add(ModBlocks.CRYSTAL_LOG.getId(), new FurnaceFuel(300), false)
                .add(ModBlocks.CRYSTAL_PLANKS.getId(), new FurnaceFuel(300), false);
        builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(ModItems.LUMINOUS_BERRY, new Compostable(0.3F), false)
                .add(ModBlocks.GLOWBLOOM.getId(), new Compostable(0.65F), false)
                .add(ModBlocks.SHARD_FERN.getId(), new Compostable(0.3F), false)
                .add(ModBlocks.GLOWCAP.getId(), new Compostable(0.65F), false)
                .add(ModBlocks.CRYSTAL_LEAVES.getId(), new Compostable(0.3F), false)
                .add(ModBlocks.CRYSTAL_SAPLING.getId(), new Compostable(0.3F), false);
    }
}
