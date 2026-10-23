package eu.shardline.mod.block;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.custom.ColossusAltarBlock;
import eu.shardline.mod.block.custom.CrystalSaplingBlock;
import eu.shardline.mod.block.custom.CrystalWebBlock;
import eu.shardline.mod.block.custom.MothLightBlock;
import eu.shardline.mod.block.custom.QueenAltarBlock;
import eu.shardline.mod.block.custom.ShardFlowerBlock;
import eu.shardline.mod.block.custom.ShardPlantBlock;
import eu.shardline.mod.block.custom.ShardPortalBlock;
import eu.shardline.mod.block.custom.TrophyBlock;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.world.feature.ModConfiguredFeatures;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Shardline.MODID);

    /** Alle Block-Items in Registrierungsreihenfolge (fuer den Kreativ-Tab). */
    public static final List<DeferredItem<BlockItem>> BLOCK_ITEMS = new ArrayList<>();

    public static final TreeGrower CRYSTAL_TREE_GROWER = new TreeGrower(Shardline.MODID + ":crystal",
            Optional.empty(), Optional.of(ModConfiguredFeatures.CRYSTAL_TREE), Optional.empty());

    // --- Splitterstein ---
    public static final DeferredBlock<Block> SHARDSTONE = registerWithItem("shardstone",
            () -> new Block(stone(MapColor.COLOR_PURPLE, 1.8F)));
    public static final DeferredBlock<Block> SHARDSTONE_BRICKS = registerWithItem("shardstone_bricks",
            () -> new Block(stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<Block> CRACKED_SHARDSTONE_BRICKS = registerWithItem("cracked_shardstone_bricks",
            () -> new Block(stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<StairBlock> SHARDSTONE_BRICK_STAIRS = registerWithItem("shardstone_brick_stairs",
            () -> new StairBlock(SHARDSTONE_BRICKS.get().defaultBlockState(), stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<SlabBlock> SHARDSTONE_BRICK_SLAB = registerWithItem("shardstone_brick_slab",
            () -> new SlabBlock(stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<WallBlock> SHARDSTONE_BRICK_WALL = registerWithItem("shardstone_brick_wall",
            () -> new WallBlock(stone(MapColor.COLOR_PURPLE, 2.0F).forceSolidOn()));

    // --- Erze & Rohstoffbloecke ---
    public static final DeferredBlock<DropExperienceBlock> SHARD_ORE = registerWithItem("shard_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), stone(MapColor.COLOR_PURPLE, 3.0F).lightLevel(state -> 3)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_SHARD_ORE = registerWithItem("deepslate_shard_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).lightLevel(state -> 3)));
    public static final DeferredBlock<Block> SHARD_BLOCK = registerWithItem("shard_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA)
                    .requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> GLOW_CRYSTAL_BLOCK = registerWithItem("glow_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.5F).sound(SoundType.AMETHYST).lightLevel(state -> 15)));
    public static final DeferredBlock<AmethystClusterBlock> GLOW_CRYSTAL_CLUSTER = registerWithItem("glow_crystal_cluster",
            () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.5F)
                    .lightLevel(state -> 8).pushReaction(PushReaction.DESTROY)));

    // --- Oberflaechen ---
    public static final DeferredBlock<Block> CRYSTAL_MOSS = registerWithItem("crystal_moss",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.8F).sound(SoundType.MOSS)));
    public static final DeferredBlock<Block> ASH_BLOCK = registerWithItem("ash_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.6F).sound(SoundType.SAND)));

    // --- Kristallholz ---
    public static final DeferredBlock<RotatedPillarBlock> CRYSTAL_LOG = registerWithItem("crystal_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.CHERRY_WOOD)));
    public static final DeferredBlock<Block> CRYSTAL_PLANKS = registerWithItem("crystal_planks",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.CHERRY_WOOD)));
    public static final DeferredBlock<LeavesBlock> CRYSTAL_LEAVES = registerWithItem("crystal_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.2F)
                    .randomTicks().sound(SoundType.CHERRY_LEAVES).noOcclusion().lightLevel(state -> 4)
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .pushReaction(PushReaction.DESTROY)
                    .isRedstoneConductor((state, level, pos) -> false)));
    public static final DeferredBlock<CrystalSaplingBlock> CRYSTAL_SAPLING = registerWithItem("crystal_sapling",
            () -> new CrystalSaplingBlock(CRYSTAL_TREE_GROWER, plant(MapColor.COLOR_PINK, 5).randomTicks()));

    // --- Leuchtende Pflanzen ---
    public static final DeferredBlock<ShardFlowerBlock> GLOWBLOOM = registerWithItem("glowbloom",
            () -> new ShardFlowerBlock(MobEffects.GLOWING, 8.0F, plant(MapColor.COLOR_LIGHT_BLUE, 10)));
    public static final DeferredBlock<ShardPlantBlock> SHARD_FERN = registerWithItem("shard_fern",
            () -> new ShardPlantBlock(plant(MapColor.COLOR_CYAN, 3).replaceable().offsetType(BlockBehaviour.OffsetType.XYZ)));
    public static final DeferredBlock<ShardPlantBlock> GLOWCAP = registerWithItem("glowcap",
            () -> new ShardPlantBlock(plant(MapColor.COLOR_LIGHT_BLUE, 12)));
    public static final DeferredBlock<ShardPlantBlock> LUMINOUS_BERRY_BUSH = registerWithItem("luminous_berry_bush",
            () -> new ShardPlantBlock(plant(MapColor.COLOR_MAGENTA, 6).offsetType(BlockBehaviour.OffsetType.XZ)));

    // --- Portal ---
    public static final DeferredBlock<Block> SHARD_PORTAL_FRAME = registerWithItem("shard_portal_frame",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(25.0F, 1200.0F).sound(SoundType.AMETHYST).lightLevel(state -> 4)));
    public static final DeferredBlock<ShardPortalBlock> SHARD_PORTAL = BLOCKS.register("shard_portal",
            () -> new ShardPortalBlock(BlockBehaviour.Properties.of().noCollission().strength(-1.0F).sound(SoundType.GLASS)
                    .lightLevel(state -> 11).pushReaction(PushReaction.BLOCK).noLootTable()));

    // --- Boss-Altaere & Trophaeen ---
    public static final DeferredBlock<ColossusAltarBlock> COLOSSUS_ALTAR = registerWithItem("colossus_altar",
            () -> new ColossusAltarBlock(altar()), new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredBlock<QueenAltarBlock> QUEEN_ALTAR = registerWithItem("queen_altar",
            () -> new QueenAltarBlock(altar()), new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredBlock<TrophyBlock> COLOSSUS_TROPHY = registerWithItem("colossus_trophy",
            () -> new TrophyBlock(trophy()), new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredBlock<TrophyBlock> QUEEN_TROPHY = registerWithItem("queen_trophy",
            () -> new TrophyBlock(trophy()), new Item.Properties().rarity(Rarity.EPIC).fireResistant());

    // --- Hilfsbloecke ohne Item ---
    public static final DeferredBlock<CrystalWebBlock> CRYSTAL_WEB = BLOCKS.register("crystal_web",
            () -> new CrystalWebBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).forceSolidOn()
                    .noCollission().strength(1.0F).sound(SoundType.AMETHYST_CLUSTER).noLootTable().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<MothLightBlock> MOTH_LIGHT = BLOCKS.register("moth_light",
            () -> new MothLightBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().noOcclusion()
                    .strength(-1.0F, 3600000.8F).mapColor(MapColor.NONE).lightLevel(state -> 12)));

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties stone(MapColor color, float hardness) {
        return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 6.0F).sound(SoundType.CALCITE);
    }

    private static BlockBehaviour.Properties plant(MapColor color, int light) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak().sound(SoundType.AZALEA)
                .lightLevel(state -> light).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties altar() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F)
                .sound(SoundType.AMETHYST).lightLevel(state -> 9).noOcclusion().noLootTable();
    }

    private static BlockBehaviour.Properties trophy() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 1200.0F)
                .sound(SoundType.AMETHYST).lightLevel(state -> 10).noOcclusion();
    }

    private static <B extends Block> DeferredBlock<B> registerWithItem(String name, Supplier<B> block) {
        return registerWithItem(name, block, new Item.Properties());
    }

    private static <B extends Block> DeferredBlock<B> registerWithItem(String name, Supplier<B> block, Item.Properties properties) {
        DeferredBlock<B> holder = BLOCKS.register(name, block);
        BLOCK_ITEMS.add(ModItems.ITEMS.registerSimpleBlockItem(name, holder, properties));
        return holder;
    }
}
