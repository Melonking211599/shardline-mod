package eu.shardline.mod.datagen;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.item.ModItems;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Block-Loot. Bloecke mit {@code noLootTable()} (Portal, Altaere, Kristallnetz, Mottenlicht) fehlen absichtlich.
 */
public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        for (Block block : new Block[] {
                ModBlocks.SHARDSTONE.get(), ModBlocks.SHARDSTONE_BRICKS.get(), ModBlocks.CRACKED_SHARDSTONE_BRICKS.get(),
                ModBlocks.SHARDSTONE_BRICK_STAIRS.get(), ModBlocks.SHARDSTONE_BRICK_WALL.get(), ModBlocks.SHARD_BLOCK.get(),
                ModBlocks.GLOW_CRYSTAL_BLOCK.get(), ModBlocks.CRYSTAL_MOSS.get(), ModBlocks.ASH_BLOCK.get(), ModBlocks.CRYSTAL_LOG.get(),
                ModBlocks.CRYSTAL_PLANKS.get(), ModBlocks.CRYSTAL_SAPLING.get(), ModBlocks.GLOWBLOOM.get(), ModBlocks.GLOWCAP.get(),
                ModBlocks.SHARD_PORTAL_FRAME.get(), ModBlocks.COLOSSUS_TROPHY.get(), ModBlocks.QUEEN_TROPHY.get()}) {
            this.dropSelf(block);
        }

        this.dropSelf(ModBlocks.PURITY_ANCHOR.get());

        // --- Splitterhandwerk ---
        for (Block block : new Block[] {
                ModBlocks.POLISHED_SHARDSTONE.get(), ModBlocks.POLISHED_SHARDSTONE_STAIRS.get(), ModBlocks.CHISELED_SHARDSTONE_BRICKS.get(),
                ModBlocks.CRYSTAL_STAIRS.get(), ModBlocks.CRYSTAL_FENCE.get(), ModBlocks.CRYSTAL_FENCE_GATE.get(),
                ModBlocks.CRYSTAL_TRAPDOOR.get(), ModBlocks.CRYSTAL_BUTTON.get(), ModBlocks.CRYSTAL_PRESSURE_PLATE.get(),
                ModBlocks.SHARD_LANTERN.get()}) {
            this.dropSelf(block);
        }
        this.dropSelf(ModBlocks.KEEPER_RUIN_BRICKS.get());
        this.dropSelf(ModBlocks.KEEPER_RUIN_PILLAR.get());
        this.add(ModBlocks.KEEPER_RUNE_SLAB.get(), this::createSlabItemTable);
        this.add(ModBlocks.POLISHED_SHARDSTONE_SLAB.get(), this::createSlabItemTable);
        this.add(ModBlocks.CRYSTAL_SLAB.get(), this::createSlabItemTable);
        this.add(ModBlocks.CRYSTAL_DOOR.get(), this::createDoorTable);
        this.dropWhenSilkTouch(ModBlocks.CRYSTAL_GLASS.get());
        this.dropWhenSilkTouch(ModBlocks.CRYSTAL_GLASS_PANE.get());

        // Infizierte Bloecke: mit Behutsamkeit der Block selbst, sonst das gereinigte Material.
        infected(ModBlocks.INFECTED_STONE.get(), Items.COBBLESTONE);
        infected(ModBlocks.INFECTED_DIRT.get(), Items.DIRT);
        infected(ModBlocks.INFECTED_GRASS.get(), Items.DIRT);
        infected(ModBlocks.INFECTED_LOG.get(), Items.OAK_LOG);
        infected(ModBlocks.INFECTED_SAND.get(), Items.SAND);
        infected(ModBlocks.INFECTED_DEEPSLATE.get(), Items.COBBLED_DEEPSLATE);
        infected(ModBlocks.INFECTED_PLANKS.get(), Items.OAK_PLANKS);
        infected(ModBlocks.INFECTED_NETHERRACK.get(), Items.NETHERRACK);
        infected(ModBlocks.INFECTED_BLACKSTONE.get(), Items.BLACKSTONE);
        infected(ModBlocks.INFECTED_SOUL_SOIL.get(), Items.SOUL_SOIL);
        infected(ModBlocks.INFECTED_NYLIUM.get(), Items.NETHERRACK);
        infected(ModBlocks.INFECTED_WART_BLOCK.get(), Items.NETHER_WART_BLOCK);
        infected(ModBlocks.INFECTED_END_STONE.get(), Items.END_STONE);
        infected(ModBlocks.INFECTED_PURPUR.get(), Items.PURPUR_BLOCK);
        this.add(ModBlocks.INFECTED_LEAVES.get(), block -> this.createSilkTouchDispatchTable(block,
                this.applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))))));

        this.add(ModBlocks.SHARDSTONE_BRICK_SLAB.get(), this::createSlabItemTable);
        this.add(ModBlocks.SHARD_ORE.get(), block -> this.createOreDrop(block, ModItems.RAW_SHARD.get()));
        this.add(ModBlocks.DEEPSLATE_SHARD_ORE.get(), block -> this.createOreDrop(block, ModItems.RAW_SHARD.get()));
        this.add(ModBlocks.CRYSTAL_LEAVES.get(), block -> this.createLeavesDrops(block, ModBlocks.CRYSTAL_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(ModBlocks.SHARD_FERN.get(), block -> createShearsOnlyDrop(block));

        // Kristallcluster: mit Behutsamkeit der Block selbst, sonst Shard-Splitter (Glueck erhoeht die Menge).
        this.add(ModBlocks.GLOW_CRYSTAL_CLUSTER.get(), block -> this.createSilkTouchDispatchTable(block,
                this.applyExplosionDecay(block, LootItem.lootTableItem(ModItems.SHARD_FRAGMENT.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE))))));

        // Leuchtbeerenstrauch: der Strauch selbst plus 1-3 Leuchtbeeren.
        this.add(ModBlocks.LUMINOUS_BERRY_BUSH.get(), block -> LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block))))
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.LUMINOUS_BERRY.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))))));
    }

    private void infected(Block block, ItemLike cleanDrop) {
        this.add(block, b -> this.createSilkTouchDispatchTable(b, this.applyExplosionCondition(b, LootItem.lootTableItem(cleanDrop))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
    }
}
