package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.item.ModItems;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Alle Rezepte (Werkbank, Ofen, Hochofen, Steinsaege, Schmiedetisch). Ausgabe: data/shardline/recipe/
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // --- Shard-Rohstoffe ---
        List<ItemLike> shardSmeltables = List.of(ModItems.RAW_SHARD.get(), ModBlocks.SHARD_ORE.get(), ModBlocks.DEEPSLATE_SHARD_ORE.get());
        for (ItemLike input : shardSmeltables) {
            String inputName = getItemName(input);
            SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, ModItems.SHARD_INGOT.get(), 0.8F, 200)
                    .group("shard_ingot").unlockedBy(getHasName(input), has(input))
                    .save(output, Shardline.id("shard_ingot_from_smelting_" + inputName));
            SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, ModItems.SHARD_INGOT.get(), 0.8F, 100)
                    .group("shard_ingot").unlockedBy(getHasName(input), has(input))
                    .save(output, Shardline.id("shard_ingot_from_blasting_" + inputName));
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARD_BLOCK.get())
                .pattern("III").pattern("III").pattern("III")
                .define('I', ModItems.SHARD_INGOT.get())
                .unlockedBy(getHasName(ModItems.SHARD_INGOT.get()), has(ModItems.SHARD_INGOT.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SHARD_INGOT.get(), 9)
                .requires(ModBlocks.SHARD_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.SHARD_BLOCK.get()), has(ModBlocks.SHARD_BLOCK.get()))
                .save(output, Shardline.id("shard_ingot_from_shard_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SHARD_FRAGMENT.get(), 4)
                .requires(ModItems.RAW_SHARD.get())
                .unlockedBy(getHasName(ModItems.RAW_SHARD.get()), has(ModItems.RAW_SHARD.get()))
                .save(output, Shardline.id("shard_fragment_from_raw_shard"));

        // --- Splitterstein ---
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARDSTONE_BRICKS.get(), 4)
                .pattern("SS").pattern("SS")
                .define('S', ModBlocks.SHARDSTONE.get())
                .unlockedBy(getHasName(ModBlocks.SHARDSTONE.get()), has(ModBlocks.SHARDSTONE.get()))
                .save(output);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SHARDSTONE_BRICKS.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SHARDSTONE_BRICKS.get(), 0.1F, 200)
                .unlockedBy(getHasName(ModBlocks.SHARDSTONE_BRICKS.get()), has(ModBlocks.SHARDSTONE_BRICKS.get()))
                .save(output);
        stairBuilder(ModBlocks.SHARDSTONE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.SHARDSTONE_BRICKS.get()))
                .unlockedBy(getHasName(ModBlocks.SHARDSTONE_BRICKS.get()), has(ModBlocks.SHARDSTONE_BRICKS.get()))
                .save(output);
        slab(output, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARDSTONE_BRICK_SLAB.get(), ModBlocks.SHARDSTONE_BRICKS.get());
        wall(output, RecipeCategory.DECORATIONS, ModBlocks.SHARDSTONE_BRICK_WALL.get(), ModBlocks.SHARDSTONE_BRICKS.get());
        stonecutting(output, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARDSTONE_BRICKS.get(), ModBlocks.SHARDSTONE.get(), 1);
        stonecutting(output, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARDSTONE_BRICK_STAIRS.get(), ModBlocks.SHARDSTONE_BRICKS.get(), 1);
        stonecutting(output, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARDSTONE_BRICK_SLAB.get(), ModBlocks.SHARDSTONE_BRICKS.get(), 2);
        stonecutting(output, RecipeCategory.DECORATIONS, ModBlocks.SHARDSTONE_BRICK_WALL.get(), ModBlocks.SHARDSTONE_BRICKS.get(), 1);

        // --- Kristallholz & Leuchtkristall ---
        planksFromLog(output, ModBlocks.CRYSTAL_PLANKS.get(), ModTags.Items.CRYSTAL_LOGS, 4);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOW_CRYSTAL_BLOCK.get())
                .requires(ModItems.SHARD_FRAGMENT.get(), 4).requires(Items.GLOWSTONE_DUST)
                .unlockedBy(getHasName(ModItems.SHARD_FRAGMENT.get()), has(ModItems.SHARD_FRAGMENT.get()))
                .save(output);

        // --- Portal ---
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SHARD_PORTAL_FRAME.get())
                .pattern(" F ").pattern("FOF").pattern(" F ")
                .define('F', ModItems.SHARD_FRAGMENT.get()).define('O', Items.OBSIDIAN)
                .unlockedBy(getHasName(ModItems.SHARD_FRAGMENT.get()), has(ModItems.SHARD_FRAGMENT.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SHARD_KEY.get())
                .pattern(" FE").pattern(" IF").pattern("I  ")
                .define('F', ModItems.SHARD_FRAGMENT.get()).define('E', Items.ENDER_EYE).define('I', ModItems.SHARD_INGOT.get())
                .unlockedBy(getHasName(ModItems.SHARD_INGOT.get()), has(ModItems.SHARD_INGOT.get()))
                .save(output);

        // --- Shard-Ruestung & -Werkzeuge ---
        Item ingot = ModItems.SHARD_INGOT.get();
        armor(output, ModItems.SHARD_HELMET.get(), ingot, "III", "I I");
        armor(output, ModItems.SHARD_CHESTPLATE.get(), ingot, "I I", "III", "III");
        armor(output, ModItems.SHARD_LEGGINGS.get(), ingot, "III", "I I", "I I");
        armor(output, ModItems.SHARD_BOOTS.get(), ingot, "I I", "I I");
        tool(output, ModItems.SHARD_SWORD.get(), ingot, "I", "I", "S");
        tool(output, ModItems.SHARD_PICKAXE.get(), ingot, "III", " S ", " S ");
        tool(output, ModItems.SHARD_AXE.get(), ingot, "II", "IS", " S");
        tool(output, ModItems.SHARD_SHOVEL.get(), ingot, "I", "S", "S");

        // --- Kristallbogen ---
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.CRYSTAL_BOW.get())
                .requires(Items.BOW).requires(ModItems.SHARD_INGOT.get(), 2).requires(ModItems.SCOUT_EYE.get())
                .requires(ModItems.VOID_RAY_HIDE.get())
                .unlockedBy(getHasName(ModItems.SCOUT_EYE.get()), has(ModItems.SCOUT_EYE.get()))
                .save(output);

        // --- Koloss ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COLOSSUS_SUMMONING_CRYSTAL.get())
                .pattern("ICI").pattern("DGD").pattern("ICI")
                .define('I', ModItems.SHARD_INGOT.get()).define('C', ModItems.KNIGHT_CREST.get())
                .define('D', Items.DIAMOND).define('G', ModBlocks.GLOW_CRYSTAL_BLOCK.get())
                .unlockedBy(getHasName(ModItems.KNIGHT_CREST.get()), has(ModItems.KNIGHT_CREST.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get(), 2)
                .pattern("DTD").pattern("DSD").pattern("DDD")
                .define('D', Items.DIAMOND).define('T', ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get())
                .define('S', ModBlocks.SHARD_BLOCK.get())
                .unlockedBy(getHasName(ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get()), has(ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get()))
                .save(output);

        colossusUpgrade(output, Items.NETHERITE_HELMET, ModItems.COLOSSUS_HELMET.get(), RecipeCategory.COMBAT);
        colossusUpgrade(output, Items.NETHERITE_CHESTPLATE, ModItems.COLOSSUS_CHESTPLATE.get(), RecipeCategory.COMBAT);
        colossusUpgrade(output, Items.NETHERITE_LEGGINGS, ModItems.COLOSSUS_LEGGINGS.get(), RecipeCategory.COMBAT);
        colossusUpgrade(output, Items.NETHERITE_BOOTS, ModItems.COLOSSUS_BOOTS.get(), RecipeCategory.COMBAT);
        colossusUpgrade(output, Items.NETHERITE_SWORD, ModItems.COLOSSUS_SWORD.get(), RecipeCategory.COMBAT);
        colossusUpgrade(output, Items.NETHERITE_PICKAXE, ModItems.COLOSSUS_PICKAXE.get(), RecipeCategory.TOOLS);
        colossusUpgrade(output, Items.NETHERITE_AXE, ModItems.COLOSSUS_AXE.get(), RecipeCategory.TOOLS);
        colossusUpgrade(output, Items.NETHERITE_SHOVEL, ModItems.COLOSSUS_SHOVEL.get(), RecipeCategory.TOOLS);

        // --- Nebenprodukte ---
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LEATHER)
                .requires(ModItems.SHADOW_PELT.get())
                .unlockedBy(getHasName(ModItems.SHADOW_PELT.get()), has(ModItems.SHADOW_PELT.get()))
                .save(output, Shardline.id("leather_from_shadow_pelt"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BLACK_DYE)
                .requires(ModItems.ASH_DUST.get(), 2)
                .unlockedBy(getHasName(ModItems.ASH_DUST.get()), has(ModItems.ASH_DUST.get()))
                .save(output, Shardline.id("black_dye_from_ash_dust"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LIGHT_BLUE_DYE)
                .requires(ModBlocks.GLOWBLOOM.get())
                .unlockedBy(getHasName(ModBlocks.GLOWBLOOM.get()), has(ModBlocks.GLOWBLOOM.get()))
                .save(output, Shardline.id("light_blue_dye_from_glowbloom"));
    }

    private static void stonecutting(RecipeOutput output, RecipeCategory category, ItemLike result, ItemLike material, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), category, result, count)
                .unlockedBy(getHasName(material), has(material))
                .save(output, Shardline.id(getItemName(result) + "_from_" + getItemName(material) + "_stonecutting"));
    }

    private static void armor(RecipeOutput output, Item result, Item material, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result).define('I', material);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.unlockedBy(getHasName(material), has(material)).save(output);
    }

    private static void tool(RecipeOutput output, Item result, Item material, String... pattern) {
        RecipeCategory category = result == ModItems.SHARD_SWORD.get() ? RecipeCategory.COMBAT : RecipeCategory.TOOLS;
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(category, result).define('I', material).define('S', Items.STICK);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.unlockedBy(getHasName(material), has(material)).save(output);
    }

    private static void colossusUpgrade(RecipeOutput output, Item netheriteItem, Item result, RecipeCategory category) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(netheriteItem),
                        Ingredient.of(ModItems.COLOSSUS_CORE.get()),
                        category, result)
                .unlocks(getHasName(ModItems.COLOSSUS_CORE.get()), has(ModItems.COLOSSUS_CORE.get()))
                .save(output, Shardline.id(getItemName(result) + "_smithing"));
    }
}
