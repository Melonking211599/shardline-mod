package eu.shardline.mod.datagen;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Drops aller Mobs und Bosse.
 */
public class ModEntityLootProvider extends EntityLootSubProvider {
    public ModEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(ModEntities.SHARD_CREEPER.get(), LootTable.lootTable()
                .withPool(drop(ModItems.SHARD_FRAGMENT.get(), 1, 3))
                .withPool(drop(Items.GUNPOWDER, 0, 2)));

        add(ModEntities.CRYSTAL_GOLEM.get(), LootTable.lootTable()
                .withPool(drop(ModItems.SHARD_FRAGMENT.get(), 2, 5))
                .withPool(drop(ModBlocks.SHARDSTONE.get(), 1, 3))
                .withPool(drop(ModItems.RAW_SHARD.get(), 0, 1)));

        add(ModEntities.GLOW_MOTH.get(), LootTable.lootTable()
                .withPool(drop(Items.GLOWSTONE_DUST, 0, 1)));

        add(ModEntities.ASH_WALKER.get(), LootTable.lootTable()
                .withPool(drop(ModItems.ASH_DUST.get(), 1, 3))
                .withPool(drop(Items.BONE, 0, 1))
                .withPool(rare(ModItems.RAW_SHARD.get(), 0.05F, 0.02F)));

        add(ModEntities.SHARD_SCOUT.get(), LootTable.lootTable()
                .withPool(drop(ModItems.SHARD_FRAGMENT.get(), 1, 2))
                .withPool(rare(ModItems.SCOUT_EYE.get(), 0.33F, 0.05F)));

        add(ModEntities.VOID_RAY.get(), LootTable.lootTable()
                .withPool(drop(ModItems.VOID_RAY_HIDE.get(), 1, 2))
                .withPool(drop(Items.PHANTOM_MEMBRANE, 0, 1)));

        add(ModEntities.CRYSTAL_SPIDER.get(), LootTable.lootTable()
                .withPool(drop(Items.STRING, 0, 2))
                .withPool(drop(ModItems.SHARD_FRAGMENT.get(), 0, 2))
                .withPool(rare(Items.SPIDER_EYE, 0.33F, 0.05F)));

        add(ModEntities.SHARD_TRADER.get(), LootTable.lootTable()
                .withPool(drop(ModItems.SHARD_FRAGMENT.get(), 2, 4)));

        add(ModEntities.SHADOW_FOX.get(), LootTable.lootTable()
                .withPool(rare(ModItems.SHADOW_PELT.get(), 0.5F, 0.1F)));

        add(ModEntities.SHATTERED_KNIGHT.get(), LootTable.lootTable()
                .withPool(guaranteed(ModItems.KNIGHT_CREST.get(), 1))
                .withPool(drop(ModItems.SHARD_INGOT.get(), 1, 2))
                .withPool(rare(ModItems.SHARD_SWORD.get(), 0.08F, 0.02F)));

        add(ModEntities.CRYSTAL_COLOSSUS.get(), LootTable.lootTable()
                .withPool(guaranteed(ModItems.COLOSSUS_CORE.get(), 3, 4))
                .withPool(guaranteed(ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE.get(), 1))
                .withPool(guaranteed(ModBlocks.COLOSSUS_TROPHY.get(), 1))
                .withPool(guaranteed(ModItems.COLOSSUS_GAUNTLET.get(), 1))
                .withPool(guaranteed(ModItems.SHARD_INGOT.get(), 6, 10))
                .withPool(guaranteed(ModBlocks.SHARD_BLOCK.get(), 1, 2)));

        add(ModEntities.SHARD_QUEEN.get(), LootTable.lootTable()
                .withPool(guaranteed(ModItems.HEART_OF_THE_EXPANSE.get(), 1))
                .withPool(guaranteed(ModItems.QUEENS_SHARDBLADE.get(), 1))
                .withPool(guaranteed(ModBlocks.QUEEN_TROPHY.get(), 1))
                .withPool(guaranteed(ModBlocks.SHARD_BLOCK.get(), 3, 5))
                .withPool(guaranteed(ModItems.COLOSSUS_CORE.get(), 1, 2)));

        // Klone lassen nichts fallen.
        add(ModEntities.QUEEN_CLONE.get(), LootTable.lootTable());
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return ModEntities.ENTITY_TYPES.getEntries().stream().map(holder -> holder.get());
    }

    /** Normale Beute mit Pluenderungs-Bonus. */
    private LootPool.Builder drop(ItemLike item, int min, int max) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F))));
    }

    /** Seltene Beute, nur wenn ein Spieler toetet. */
    private LootPool.Builder rare(ItemLike item, float chance, float perLooting) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.registries, chance, perLooting));
    }

    private static LootPool.Builder guaranteed(ItemLike item, int count) {
        return guaranteed(item, count, count);
    }

    private static LootPool.Builder guaranteed(ItemLike item, int min, int max) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }
}
