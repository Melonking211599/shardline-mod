package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Itemmodelle. Texturen: assets/shardline/textures/item/&lt;name&gt;.png
 */
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, Shardline.MODID, fileHelper);
    }

    @Override
    protected void registerModels() {
        // Einfache Items
        for (Item item : new Item[] {
                ModItems.SHARD_FRAGMENT.get(), ModItems.RAW_SHARD.get(), ModItems.SHARD_INGOT.get(), ModItems.ASH_DUST.get(),
                ModItems.SCOUT_EYE.get(), ModItems.VOID_RAY_HIDE.get(), ModItems.SHADOW_PELT.get(), ModItems.KNIGHT_CREST.get(),
                ModItems.LUMINOUS_BERRY.get(), ModItems.SHARD_KEY.get(), ModItems.COLOSSUS_SUMMONING_CRYSTAL.get(),
                ModItems.COLOSSUS_CORE.get(), ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get(), ModItems.HEART_OF_THE_EXPANSE.get(),
                ModItems.SHARD_HELMET.get(), ModItems.SHARD_CHESTPLATE.get(), ModItems.SHARD_LEGGINGS.get(), ModItems.SHARD_BOOTS.get(),
                ModItems.COLOSSUS_HELMET.get(), ModItems.COLOSSUS_CHESTPLATE.get(), ModItems.COLOSSUS_LEGGINGS.get(), ModItems.COLOSSUS_BOOTS.get(),
                ModItems.LORE_BOOK.get(), ModItems.PURIFICATION_ESSENCE.get(), ModItems.BREACH_CORE.get()}) {
            basicItem(item);
        }

        // Werkzeuge und Waffen in der Hand
        for (Item item : new Item[] {
                ModItems.SHARD_SWORD.get(), ModItems.SHARD_PICKAXE.get(), ModItems.SHARD_AXE.get(), ModItems.SHARD_SHOVEL.get(),
                ModItems.COLOSSUS_SWORD.get(), ModItems.COLOSSUS_PICKAXE.get(), ModItems.COLOSSUS_AXE.get(), ModItems.COLOSSUS_SHOVEL.get(),
                ModItems.QUEENS_SHARDBLADE.get(), ModItems.COLOSSUS_GAUNTLET.get()}) {
            handheldItem(item);
        }

        crystalBow();

        // Spawn-Eier
        ModItems.SPAWN_EGGS.forEach(egg -> spawnEggItem(egg.get()));

        // Pflanzen als flache Items mit Blocktextur
        for (Block block : new Block[] {
                ModBlocks.CRYSTAL_SAPLING.get(), ModBlocks.GLOWBLOOM.get(), ModBlocks.SHARD_FERN.get(), ModBlocks.GLOWCAP.get(),
                ModBlocks.LUMINOUS_BERRY_BUSH.get(), ModBlocks.GLOW_CRYSTAL_CLUSTER.get()}) {
            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
        }

        wallInventory("shardstone_brick_wall", modLoc("block/shardstone_bricks"));
    }

    private void crystalBow() {
        ModelFile[] pulling = new ModelFile[3];
        for (int i = 0; i < 3; i++) {
            pulling[i] = withExistingParent("crystal_bow_pulling_" + i, mcLoc("item/bow"))
                    .texture("layer0", modLoc("item/crystal_bow_pulling_" + i));
        }
        ItemModelBuilder bow = withExistingParent("crystal_bow", mcLoc("item/bow")).texture("layer0", modLoc("item/crystal_bow"));
        bow.override().predicate(mcLoc("pulling"), 1.0F).model(pulling[0]).end();
        bow.override().predicate(mcLoc("pulling"), 1.0F).predicate(mcLoc("pull"), 0.65F).model(pulling[1]).end();
        bow.override().predicate(mcLoc("pulling"), 1.0F).predicate(mcLoc("pull"), 0.9F).model(pulling[2]).end();
    }
}
