package eu.shardline.mod.datagen;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.item.ModItems;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
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
                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries)))));

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
                .add(item(ModItems.CRYSTAL_BOW.get(), 2, 1, 1))));

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
                        .apply(SetPotionFunction.setPotion(Potions.STRONG_HEALING)))));

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
                        .apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, UniformGenerator.between(25.0F, 39.0F))))));
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
