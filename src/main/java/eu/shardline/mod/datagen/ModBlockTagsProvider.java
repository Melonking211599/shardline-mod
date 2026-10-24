package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, Shardline.MODID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.SHARDSTONE.get(), ModBlocks.SHARDSTONE_BRICKS.get(), ModBlocks.CRACKED_SHARDSTONE_BRICKS.get(),
                ModBlocks.SHARDSTONE_BRICK_STAIRS.get(), ModBlocks.SHARDSTONE_BRICK_SLAB.get(), ModBlocks.SHARDSTONE_BRICK_WALL.get(),
                ModBlocks.SHARD_ORE.get(), ModBlocks.DEEPSLATE_SHARD_ORE.get(), ModBlocks.SHARD_BLOCK.get(),
                ModBlocks.GLOW_CRYSTAL_BLOCK.get(), ModBlocks.GLOW_CRYSTAL_CLUSTER.get(), ModBlocks.SHARD_PORTAL_FRAME.get(),
                ModBlocks.COLOSSUS_TROPHY.get(), ModBlocks.QUEEN_TROPHY.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.CRYSTAL_LOG.get(), ModBlocks.CRYSTAL_PLANKS.get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.CRYSTAL_MOSS.get(), ModBlocks.ASH_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.CRYSTAL_LEAVES.get());

        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.SHARD_ORE.get(), ModBlocks.DEEPSLATE_SHARD_ORE.get(), ModBlocks.SHARD_BLOCK.get());
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.SHARD_PORTAL_FRAME.get());

        tag(BlockTags.STAIRS).add(ModBlocks.SHARDSTONE_BRICK_STAIRS.get());
        tag(BlockTags.SLABS).add(ModBlocks.SHARDSTONE_BRICK_SLAB.get());
        tag(BlockTags.WALLS).add(ModBlocks.SHARDSTONE_BRICK_WALL.get());
        tag(ModTags.Blocks.CRYSTAL_LOGS).add(ModBlocks.CRYSTAL_LOG.get());
        tag(BlockTags.LOGS).addTag(ModTags.Blocks.CRYSTAL_LOGS);
        tag(BlockTags.PLANKS).add(ModBlocks.CRYSTAL_PLANKS.get());
        tag(BlockTags.LEAVES).add(ModBlocks.CRYSTAL_LEAVES.get());
        tag(BlockTags.SAPLINGS).add(ModBlocks.CRYSTAL_SAPLING.get());
        tag(BlockTags.SMALL_FLOWERS).add(ModBlocks.GLOWBLOOM.get());
        tag(BlockTags.REPLACEABLE_BY_TREES).add(ModBlocks.SHARD_FERN.get());
        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.SHARD_BLOCK.get());
        tag(BlockTags.PORTALS).add(ModBlocks.SHARD_PORTAL.get());
        tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.SHARD_PORTAL.get(), ModBlocks.COLOSSUS_ALTAR.get(), ModBlocks.QUEEN_ALTAR.get());
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.SHARD_PORTAL.get(), ModBlocks.COLOSSUS_ALTAR.get(), ModBlocks.QUEEN_ALTAR.get());
        tag(BlockTags.OVERWORLD_CARVER_REPLACEABLES).add(ModBlocks.SHARDSTONE.get(), ModBlocks.CRYSTAL_MOSS.get(),
                ModBlocks.ASH_BLOCK.get(), ModBlocks.SHARD_ORE.get(), ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        tag(BlockTags.CRYSTAL_SOUND_BLOCKS).add(ModBlocks.SHARD_BLOCK.get(), ModBlocks.GLOW_CRYSTAL_BLOCK.get());

        tag(ModTags.Blocks.SHARD_PLANT_SOIL).add(ModBlocks.CRYSTAL_MOSS.get(), ModBlocks.ASH_BLOCK.get(), ModBlocks.SHARDSTONE.get(),
                ModBlocks.GLOW_CRYSTAL_BLOCK.get(), Blocks.MOSS_BLOCK).addTag(BlockTags.DIRT);
        tag(ModTags.Blocks.ANGERS_CRYSTAL_GOLEM).add(ModBlocks.SHARDSTONE.get(), ModBlocks.SHARD_ORE.get(),
                ModBlocks.DEEPSLATE_SHARD_ORE.get(), ModBlocks.GLOW_CRYSTAL_BLOCK.get(), ModBlocks.GLOW_CRYSTAL_CLUSTER.get(),
                ModBlocks.SHARDSTONE_BRICKS.get(), ModBlocks.CRACKED_SHARDSTONE_BRICKS.get());

        // --- Shardvirus ---
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.INFECTED_STONE.get(), ModBlocks.PURITY_ANCHOR.get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.INFECTED_DIRT.get(), ModBlocks.INFECTED_GRASS.get(), ModBlocks.INFECTED_SAND.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.INFECTED_LOG.get());
        tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.INFECTED_LEAVES.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.PURITY_ANCHOR.get());
        tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.MEMORY_CRYSTAL.get(), ModBlocks.DECISION_ALTAR.get());
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.MEMORY_CRYSTAL.get(), ModBlocks.DECISION_ALTAR.get());
        tag(ModTags.Blocks.INFECTED).add(ModBlocks.INFECTED_STONE.get(), ModBlocks.INFECTED_DIRT.get(), ModBlocks.INFECTED_GRASS.get(),
                ModBlocks.INFECTED_LOG.get(), ModBlocks.INFECTED_LEAVES.get(), ModBlocks.INFECTED_SAND.get());
        tag(ModTags.Blocks.INFECTABLE_STONE).addTag(BlockTags.BASE_STONE_OVERWORLD)
                .add(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.COBBLED_DEEPSLATE, Blocks.CALCITE,
                        ModBlocks.SHARDSTONE.get(), ModBlocks.CRACKED_SHARDSTONE_BRICKS.get());
        tag(ModTags.Blocks.INFECTABLE_GRASS).add(Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.MOSS_BLOCK,
                ModBlocks.CRYSTAL_MOSS.get());
        tag(ModTags.Blocks.INFECTABLE_DIRT).add(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.MUD, Blocks.DIRT_PATH,
                Blocks.FARMLAND);
        tag(ModTags.Blocks.INFECTABLE_SAND).addTag(BlockTags.SAND).add(Blocks.GRAVEL, ModBlocks.ASH_BLOCK.get());
        tag(ModTags.Blocks.INFECTABLE_LOG).addTag(BlockTags.OVERWORLD_NATURAL_LOGS).addTag(ModTags.Blocks.CRYSTAL_LOGS);
        tag(ModTags.Blocks.INFECTABLE_LEAVES).addTag(BlockTags.LEAVES);

        // --- Splitterhandwerk ---
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.POLISHED_SHARDSTONE.get(), ModBlocks.POLISHED_SHARDSTONE_STAIRS.get(),
                ModBlocks.POLISHED_SHARDSTONE_SLAB.get(), ModBlocks.CHISELED_SHARDSTONE_BRICKS.get(), ModBlocks.SHARD_LANTERN.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.CRYSTAL_STAIRS.get(), ModBlocks.CRYSTAL_SLAB.get(), ModBlocks.CRYSTAL_FENCE.get(),
                ModBlocks.CRYSTAL_FENCE_GATE.get(), ModBlocks.CRYSTAL_DOOR.get(), ModBlocks.CRYSTAL_TRAPDOOR.get(),
                ModBlocks.CRYSTAL_BUTTON.get(), ModBlocks.CRYSTAL_PRESSURE_PLATE.get());
        tag(BlockTags.STAIRS).add(ModBlocks.POLISHED_SHARDSTONE_STAIRS.get());
        tag(BlockTags.SLABS).add(ModBlocks.POLISHED_SHARDSTONE_SLAB.get());
        tag(BlockTags.WOODEN_STAIRS).add(ModBlocks.CRYSTAL_STAIRS.get());
        tag(BlockTags.WOODEN_SLABS).add(ModBlocks.CRYSTAL_SLAB.get());
        tag(BlockTags.WOODEN_FENCES).add(ModBlocks.CRYSTAL_FENCE.get());
        tag(BlockTags.FENCE_GATES).add(ModBlocks.CRYSTAL_FENCE_GATE.get());
        tag(BlockTags.WOODEN_DOORS).add(ModBlocks.CRYSTAL_DOOR.get());
        tag(BlockTags.WOODEN_TRAPDOORS).add(ModBlocks.CRYSTAL_TRAPDOOR.get());
        tag(BlockTags.WOODEN_BUTTONS).add(ModBlocks.CRYSTAL_BUTTON.get());
        tag(BlockTags.WOODEN_PRESSURE_PLATES).add(ModBlocks.CRYSTAL_PRESSURE_PLATE.get());

        // --- Hueter-Ruinen ---
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.KEEPER_RUIN_BRICKS.get(), ModBlocks.KEEPER_RUIN_PILLAR.get(),
                ModBlocks.KEEPER_RUNE_SLAB.get());
        tag(BlockTags.SLABS).add(ModBlocks.KEEPER_RUNE_SLAB.get());
        tag(BlockTags.IMPERMEABLE).add(ModBlocks.CRYSTAL_GLASS.get());
        tag(Tags.Blocks.GLASS_BLOCKS).add(ModBlocks.CRYSTAL_GLASS.get());
        tag(Tags.Blocks.GLASS_PANES).add(ModBlocks.CRYSTAL_GLASS_PANE.get());

        tag(ModTags.Blocks.ORES_SHARD).add(ModBlocks.SHARD_ORE.get(), ModBlocks.DEEPSLATE_SHARD_ORE.get());
        tag(Tags.Blocks.ORES).addTag(ModTags.Blocks.ORES_SHARD);
        tag(ModTags.Blocks.STORAGE_BLOCKS_SHARD).add(ModBlocks.SHARD_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(ModTags.Blocks.STORAGE_BLOCKS_SHARD);
    }
}
