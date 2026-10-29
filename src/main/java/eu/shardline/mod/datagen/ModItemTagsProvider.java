package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.item.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                               CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper fileHelper) {
        super(output, lookup, blockTags, Shardline.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(ModTags.Blocks.CRYSTAL_LOGS, ModTags.Items.CRYSTAL_LOGS);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WALLS, ItemTags.WALLS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(Tags.Blocks.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS);
        copy(Tags.Blocks.GLASS_PANES, Tags.Items.GLASS_PANES);
        tag(Tags.Items.FOODS).add(ModItems.GLOWCAP_STEW.get(), ModItems.CRYSTAL_CANDY.get(), ModItems.BERRY_TART.get());
        tag(Tags.Items.FOODS_SOUP).add(ModItems.GLOWCAP_STEW.get());
        tag(Tags.Items.FOODS_CANDY).add(ModItems.CRYSTAL_CANDY.get());
        tag(Tags.Items.SLIME_BALLS).add(ModItems.CRYSTAL_SLIME.get());
        copy(ModTags.Blocks.ORES_SHARD, ModTags.Items.ORES_SHARD);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(ModTags.Blocks.STORAGE_BLOCKS_SHARD, ModTags.Items.STORAGE_BLOCKS_SHARD);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

        tag(ModTags.Items.INGOTS_SHARD).add(ModItems.SHARD_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(ModTags.Items.INGOTS_SHARD);
        tag(ModTags.Items.RAW_MATERIALS_SHARD).add(ModItems.RAW_SHARD.get());
        tag(Tags.Items.RAW_MATERIALS).addTag(ModTags.Items.RAW_MATERIALS_SHARD);
        tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.SHARD_INGOT.get());

        tag(ItemTags.SWORDS).add(ModItems.SHARD_SWORD.get(), ModItems.COLOSSUS_SWORD.get(), ModItems.QUEENS_SHARDBLADE.get());
        tag(ItemTags.PICKAXES).add(ModItems.SHARD_PICKAXE.get(), ModItems.COLOSSUS_PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.SHARD_AXE.get(), ModItems.COLOSSUS_AXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.SHARD_SHOVEL.get(), ModItems.COLOSSUS_SHOVEL.get());

        tag(ItemTags.HEAD_ARMOR).add(ModItems.SHARD_HELMET.get(), ModItems.COLOSSUS_HELMET.get());
        tag(ItemTags.CHEST_ARMOR).add(ModItems.SHARD_CHESTPLATE.get(), ModItems.COLOSSUS_CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR).add(ModItems.SHARD_LEGGINGS.get(), ModItems.COLOSSUS_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR).add(ModItems.SHARD_BOOTS.get(), ModItems.COLOSSUS_BOOTS.get());
        tag(ItemTags.TRIMMABLE_ARMOR).add(ModItems.SHARD_HELMET.get(), ModItems.SHARD_CHESTPLATE.get(), ModItems.SHARD_LEGGINGS.get(),
                ModItems.SHARD_BOOTS.get(), ModItems.COLOSSUS_HELMET.get(), ModItems.COLOSSUS_CHESTPLATE.get(),
                ModItems.COLOSSUS_LEGGINGS.get(), ModItems.COLOSSUS_BOOTS.get());
        tag(ModTags.Items.SHARD_SET).add(ModItems.SHARD_HELMET.get(), ModItems.SHARD_CHESTPLATE.get(), ModItems.SHARD_LEGGINGS.get(),
                ModItems.SHARD_BOOTS.get());
        tag(ModTags.Items.COLOSSUS_SET).add(ModItems.COLOSSUS_HELMET.get(), ModItems.COLOSSUS_CHESTPLATE.get(),
                ModItems.COLOSSUS_LEGGINGS.get(), ModItems.COLOSSUS_BOOTS.get());

        tag(ItemTags.BOW_ENCHANTABLE).add(ModItems.CRYSTAL_BOW.get());
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.CRYSTAL_BOW.get(), ModItems.COLOSSUS_GAUNTLET.get(), ModItems.SHARD_KEY.get());
        tag(ItemTags.VANISHING_ENCHANTABLE).add(ModItems.CRYSTAL_BOW.get(), ModItems.COLOSSUS_GAUNTLET.get());
        tag(Tags.Items.TOOLS_BOW).add(ModItems.CRYSTAL_BOW.get());
    }
}
