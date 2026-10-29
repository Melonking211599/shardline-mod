package eu.shardline.mod.datagen;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.lore.LoreBooks;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Truhen-Loot der Strukturen.
 */
public class ModChestLootProvider implements LootTableSubProvider {
    private final HolderLookup.Provider registries;

    public ModChestLootProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ModLootTables.RUIN_TOWER_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(4.0F, 7.0F))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 20, 2, 8))
                .add(item(ModItems.RAW_SHARD.get(), 10, 1, 3))
                .add(item(ModItems.SHARD_INGOT.get(), 6, 1, 2))
                .add(item(ModItems.LUMINOUS_BERRY.get(), 12, 2, 5))
                .add(item(ModBlocks.GLOWBLOOM.get(), 8, 1, 3))
                .add(item(Items.IRON_INGOT, 10, 1, 4))
                .add(item(Items.GOLD_INGOT, 5, 1, 3))
                .add(item(Items.ENDER_PEARL, 4, 1, 1))
                .add(item(Items.OBSIDIAN, 4, 1, 2))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries))))
                .withPool(loreBooks(4, "wanderer_notes", "crystal_kingdom", "keepers_oath", "the_resonance", "first_infection", "traders_ledger"))
                .withPool(lorePages(1.0F, 2.0F, 12, "wanderer_notes", "crystal_kingdom", "keepers_oath", "the_resonance", "traders_ledger")));

        output.accept(ModLootTables.RUIN_TOWER_TREASURE, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(2.0F, 4.0F))
                .add(item(ModItems.SHARD_INGOT.get(), 10, 2, 4))
                .add(item(Items.DIAMOND, 6, 1, 2))
                .add(item(ModItems.KNIGHT_CREST.get(), 5, 1, 1))
                .add(item(ModItems.COLOSSUS_SUMMONING_CRYSTAL.get(), 2, 1, 1))
                .add(item(Items.GOLDEN_APPLE, 4, 1, 2))
                .add(enchanted(ModItems.SHARD_SWORD.get(), 3))
                .add(enchanted(ModItems.SHARD_HELMET.get(), 2))
                .add(enchanted(ModItems.SHARD_CHESTPLATE.get(), 2))
                .add(enchanted(ModItems.SHARD_LEGGINGS.get(), 2))
                .add(enchanted(ModItems.SHARD_BOOTS.get(), 2))
                .add(item(ModItems.CRYSTAL_BOW.get(), 2, 1, 1)))
                .withPool(loreBooks(2, "the_colossus", "the_last_knights", "queen_selvaine", "the_shattering")));

        output.accept(ModLootTables.COLOSSUS_ARENA_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 5.0F))
                .add(item(ModBlocks.GLOW_CRYSTAL_BLOCK.get(), 8, 2, 6))
                .add(item(ModItems.LUMINOUS_BERRY.get(), 10, 3, 8))
                .add(item(ModItems.SHARD_INGOT.get(), 8, 1, 3))
                .add(item(Items.ARROW, 8, 8, 24))
                .add(item(Items.GOLDEN_CARROT, 8, 2, 6))
                .add(item(Items.COOKED_BEEF, 6, 3, 8))
                .add(LootItem.lootTableItem(Items.POTION).setWeight(6)
                        .apply(SetPotionFunction.setPotion(Potions.STRONG_HEALING)))
                .add(LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(4)
                        .apply(SetPotionFunction.setPotion(Potions.STRONG_HEALING))))
                .withPool(loreBooks(3, "the_colossus", "the_last_knights", "the_breach_core")));

        output.accept(ModLootTables.QUEEN_ARENA_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(4.0F, 6.0F))
                .add(item(ModItems.SHARD_INGOT.get(), 10, 4, 8))
                .add(item(Items.DIAMOND, 8, 2, 5))
                .add(item(ModBlocks.SHARD_BLOCK.get(), 5, 1, 2))
                .add(item(Items.NETHERITE_SCRAP, 4, 1, 2))
                .add(item(ModItems.COLOSSUS_CORE.get(), 3, 1, 1))
                .add(item(Items.ENDER_PEARL, 6, 2, 6))
                .add(item(Items.TOTEM_OF_UNDYING, 2, 1, 1))
                .add(item(Items.ENCHANTED_GOLDEN_APPLE, 2, 1, 1))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(4)
                        .apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, UniformGenerator.between(25.0F, 39.0F)))))
                .withPool(loreBooks(1, "the_seal", "the_shattering", "the_breach_core")));

        output.accept(ModLootTables.KEEPER_SHRINE_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 5.0F))
                .add(item(ModBlocks.GLOWBLOOM.get(), 10, 1, 4))
                .add(item(ModItems.LUMINOUS_BERRY.get(), 10, 2, 6))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 12, 2, 6))
                .add(item(ModItems.SHARD_INGOT.get(), 6, 1, 2))
                .add(item(ModBlocks.GLOW_CRYSTAL_BLOCK.get(), 6, 1, 4))
                .add(item(Items.EXPERIENCE_BOTTLE, 6, 2, 5))
                .add(LootItem.lootTableItem(Items.POTION).setWeight(5)
                        .apply(SetPotionFunction.setPotion(Potions.REGENERATION)))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(4)
                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries))))
                .withPool(loreBooks(2, "keepers_oath", "crystal_kingdom", "queen_selvaine")));

        // Hueter-Ruinen: die Bibliothek der Hueter - immer mindestens ein Lore-Buch
        // Nach dem Ausbruch: Vorraete des Lagers (Essen, Licht, Splitter) und das Archiv der Bibliothek (viele Schriften)
        output.accept(ModLootTables.SURVIVOR_CAMP_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 6.0F))
                .add(item(Items.BREAD, 12, 1, 4))
                .add(item(ModItems.BERRY_TART.get(), 8, 1, 3))
                .add(item(ModItems.LUMINOUS_BERRY.get(), 10, 2, 6))
                .add(item(Items.TORCH, 8, 2, 8))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 10, 2, 6))
                .add(item(ModBlocks.CRYSTAL_PLANKS.get(), 8, 4, 12))
                .add(item(ModItems.PURIFICATION_ESSENCE.get(), 2, 1, 1))));
        output.accept(ModLootTables.KEEPER_LIBRARY_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(2.0F, 4.0F))
                .add(item(Items.PAPER, 12, 2, 6))
                .add(item(Items.BOOK, 8, 1, 3))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 8, 2, 5))
                .add(item(ModItems.ECHO_SHARD.get(), 6, 1, 2))
                .add(item(ModBlocks.KEEPER_RUNE_SLAB.get(), 6, 2, 4)))
                .withPool(loreBooks(0, "keepers_oath", "the_resonance", "the_seal", "the_shattering", "queen_selvaine"))
                .withPool(loreBooks(2, "keepers_oath", "the_seal", "crystal_kingdom")));

        output.accept(ModLootTables.KEEPER_RUIN_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 5.0F))
                .add(item(Items.GOLD_NUGGET, 12, 3, 9))
                .add(item(Items.PAPER, 10, 2, 6))
                .add(item(ModBlocks.GLOWBLOOM.get(), 8, 1, 3))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 10, 2, 6))
                .add(item(ModItems.SHARD_INGOT.get(), 5, 1, 2))
                .add(item(ModBlocks.KEEPER_RUNE_SLAB.get(), 6, 2, 4))
                .add(item(Items.EXPERIENCE_BOTTLE, 5, 1, 3))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(4)
                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries))))
                .withPool(loreBooks(0, "keepers_oath", "crystal_kingdom", "the_resonance", "first_infection", "queen_selvaine"))
                .withPool(loreBooks(3, "keepers_oath", "the_resonance", "the_shattering"))
                .withPool(lorePages(2.0F, 4.0F, 4, "keepers_oath", "crystal_kingdom", "the_resonance", "queen_selvaine", "the_seal")));

        output.accept(ModLootTables.TRADER_OUTPOST_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(4.0F, 6.0F))
                .add(item(ModItems.LUMINOUS_BERRY.get(), 14, 3, 8))
                .add(item(Items.BREAD, 10, 2, 5))
                .add(item(Items.COOKED_MUTTON, 6, 2, 4))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 14, 3, 10))
                .add(item(ModItems.RAW_SHARD.get(), 8, 1, 3))
                .add(item(Items.EMERALD, 6, 1, 4))
                .add(item(Items.PAPER, 6, 2, 6))
                .add(item(Items.TORCH, 6, 4, 12))
                .add(item(ModItems.SHARD_KEY.get(), 2, 1, 1)))
                .withPool(loreBooks(2, "traders_ledger", "wanderer_notes")));

        output.accept(ModLootTables.ASH_CRYPT_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 6.0F))
                .add(item(ModItems.ASH_DUST.get(), 14, 2, 8))
                .add(item(Items.BONE, 12, 2, 6))
                .add(item(ModItems.SHARD_INGOT.get(), 8, 1, 3))
                .add(item(ModItems.KNIGHT_CREST.get(), 5, 1, 1))
                .add(item(Items.GOLD_INGOT, 6, 2, 5))
                .add(item(Items.DIAMOND, 3, 1, 2))
                .add(enchanted(ModItems.SHARD_SWORD.get(), 2))
                .add(enchanted(ModItems.SHARD_HELMET.get(), 2))
                .add(enchanted(ModItems.SHARD_BOOTS.get(), 2))
                .add(item(ModItems.COLOSSUS_SUMMONING_CRYSTAL.get(), 1, 1, 1)))
                .withPool(loreBooks(3, "the_last_knights", "the_shattering", "first_infection"))
                .withPool(lorePages(1.0F, 2.0F, 10, "the_last_knights", "the_shattering", "first_infection")));

        output.accept(ModLootTables.CRYSTAL_GEODE_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 5.0F))
                .add(item(ModItems.RAW_SHARD.get(), 14, 2, 6))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 12, 4, 12))
                .add(item(ModItems.SHARD_INGOT.get(), 6, 1, 3))
                .add(item(ModBlocks.GLOW_CRYSTAL_BLOCK.get(), 8, 2, 6))
                .add(item(Items.AMETHYST_SHARD, 8, 2, 8))
                .add(item(Items.LAPIS_LAZULI, 6, 4, 10))
                .add(item(Items.DIAMOND, 3, 1, 2))
                .add(enchanted(ModItems.SHARD_PICKAXE.get(), 3)))
                .withPool(loreBooks(4, "the_resonance", "first_infection"))
                .withPool(lorePages(1.0F, 1.0F, 8, "the_resonance", "first_infection")));

        output.accept(ModLootTables.SHARD_CRATER_CHEST, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(3.0F, 5.0F))
                .add(item(ModItems.SHARD_FRAGMENT.get(), 16, 2, 6))
                .add(item(ModItems.RAW_SHARD.get(), 10, 1, 3))
                .add(item(ModBlocks.SHARD_PORTAL_FRAME.get(), 6, 1, 3))
                .add(item(Items.BREAD, 8, 1, 4))
                .add(item(Items.IRON_INGOT, 8, 1, 4))
                .add(item(Items.COMPASS, 3, 1, 1))
                .add(item(Items.OBSIDIAN, 4, 1, 3))
                .add(item(ModItems.SHARD_KEY.get(), 2, 1, 1)))
                .withPool(loreBooks(1, "wanderer_notes")));
    }

    /** Ein Lore-Buch aus der Liste oder (mit Gewicht {@code emptyWeight}) keines. */
    private static LootPool.Builder loreBooks(int emptyWeight, String... ids) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        for (String id : ids) {
            LoreBooks.LoreBook book = LoreBooks.byId(id);
            if (book == null) {
                throw new IllegalArgumentException("Unbekanntes Lore-Buch: " + id);
            }
            pool.add(LootItem.lootTableItem(ModItems.LORE_BOOK.get()).setWeight(1)
                    .apply(SetComponentsFunction.setComponent(ModDataComponents.LORE_BOOK.get(), book.id()))
                    .apply(SetComponentsFunction.setComponent(DataComponents.WRITTEN_BOOK_CONTENT, LoreBooks.content(book))));
        }
        return pool;
    }

    /** Lose Lore-Seiten: je ein Eintrag pro Buch und Seite, dazu ein Leereintrag. */
    private static LootPool.Builder lorePages(float minRolls, float maxRolls, int emptyWeight, String... ids) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(minRolls, maxRolls))
                .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        for (String id : ids) {
            LoreBooks.LoreBook book = LoreBooks.byId(id);
            if (book == null) {
                throw new IllegalArgumentException("Unbekanntes Lore-Buch: " + id);
            }
            for (int page = 1; page <= book.pages(); page++) {
                pool.add(LootItem.lootTableItem(ModItems.LORE_PAGE.get()).setWeight(1)
                        .apply(SetComponentsFunction.setComponent(ModDataComponents.LORE_BOOK.get(), book.id()))
                        .apply(SetComponentsFunction.setComponent(ModDataComponents.LORE_PAGE.get(), page)));
            }
        }
        return pool;
    }

    private static LootPoolSingletonContainer.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private LootPoolSingletonContainer.Builder<?> enchanted(ItemLike item, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, UniformGenerator.between(15.0F, 30.0F)));
    }
}
