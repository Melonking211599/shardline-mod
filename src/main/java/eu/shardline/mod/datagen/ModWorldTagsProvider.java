package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.world.biome.ModBiomes;
import eu.shardline.mod.world.structure.ModStructures;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Tags fuer Datapack-Registries (Biome, Strukturen, Schadenstypen).
 */
public final class ModWorldTagsProvider {
    private ModWorldTagsProvider() {
    }

    public static class Biomes extends BiomeTagsProvider {
        @SuppressWarnings("unchecked")
        private static final ResourceKey<Biome>[] CRATER_BIOMES = new ResourceKey[] {
                net.minecraft.world.level.biome.Biomes.PLAINS, net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS,
                net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS, net.minecraft.world.level.biome.Biomes.MEADOW,
                net.minecraft.world.level.biome.Biomes.SAVANNA, net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU,
                net.minecraft.world.level.biome.Biomes.DESERT, net.minecraft.world.level.biome.Biomes.BADLANDS,
                net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS};

        public Biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
            super(output, lookup, Shardline.MODID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Biomes.IS_SPLITTERWEITE).add(ModBiomes.CRYSTAL_FOREST, ModBiomes.ASH_FIELDS, ModBiomes.GLOWING_CAVES);
            tag(ModTags.Biomes.HAS_RUIN_TOWER).addTag(ModTags.Biomes.IS_SPLITTERWEITE);
            tag(ModTags.Biomes.HAS_QUEEN_ARENA).addTag(ModTags.Biomes.IS_SPLITTERWEITE);
            tag(ModTags.Biomes.HAS_COLOSSUS_ARENA).add(ModBiomes.CRYSTAL_FOREST);
            tag(ModTags.Biomes.HAS_KEEPER_SHRINE).add(ModBiomes.CRYSTAL_FOREST);
            tag(ModTags.Biomes.HAS_TRADER_OUTPOST).addTag(ModTags.Biomes.IS_SPLITTERWEITE);
            tag(ModTags.Biomes.HAS_ASH_CRYPT).add(ModBiomes.ASH_FIELDS);
            tag(ModTags.Biomes.HAS_CRYSTAL_GEODE).add(ModBiomes.GLOWING_CAVES);
            // Offene Landschaften, damit der Krater nicht in Baumkronen oder unter Wasser landet.
            tag(ModTags.Biomes.HAS_SHARD_CRATER).add(CRATER_BIOMES);
        }
    }

    public static class Structures extends StructureTagsProvider {
        public Structures(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
            super(output, lookup, Shardline.MODID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Structures.ON_COLOSSUS_MAPS).add(ModStructures.COLOSSUS_ARENA_KEY);
        }
    }

    public static class DamageTypes extends DamageTypeTagsProvider {
        public DamageTypes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
            super(output, lookup, Shardline.MODID, fileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(DamageTypeTags.IS_PROJECTILE).add(ModDamageTypes.SHARD);
            tag(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.VOID_ZONE, ModDamageTypes.SHARD_FEVER);
            tag(DamageTypeTags.NO_KNOCKBACK).add(ModDamageTypes.VOID_ZONE, ModDamageTypes.SHARD_FEVER);
            tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.VOID_ZONE, ModDamageTypes.CRYSTAL_PILLAR);
        }
    }
}
