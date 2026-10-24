package eu.shardline.mod.block;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.custom.ColossusAltarBlock;
import eu.shardline.mod.block.custom.CrystalSaplingBlock;
import eu.shardline.mod.block.custom.CrystalWebBlock;
import eu.shardline.mod.block.custom.DecisionAltarBlock;
import eu.shardline.mod.block.custom.InfectedBlock;
import eu.shardline.mod.block.custom.InfectedPillarBlock;
import eu.shardline.mod.block.custom.MemoryCrystalBlock;
import eu.shardline.mod.block.custom.MothLightBlock;
import eu.shardline.mod.block.custom.PurityAnchorBlock;
import eu.shardline.mod.block.custom.QueenAltarBlock;
import eu.shardline.mod.block.custom.ShardFlowerBlock;
import eu.shardline.mod.block.custom.ShardOreBlock;
import eu.shardline.mod.block.custom.ShardPlantBlock;
import eu.shardline.mod.block.custom.ShardPortalBlock;
import eu.shardline.mod.block.custom.TrophyBlock;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.world.feature.ModConfiguredFeatures;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import eu.shardline.mod.virus.VirusStrain;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
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
    public static final DeferredBlock<Block> POLISHED_SHARDSTONE = registerWithItem("polished_shardstone",
            () -> new Block(stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<StairBlock> POLISHED_SHARDSTONE_STAIRS = registerWithItem("polished_shardstone_stairs",
            () -> new StairBlock(POLISHED_SHARDSTONE.get().defaultBlockState(), stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<SlabBlock> POLISHED_SHARDSTONE_SLAB = registerWithItem("polished_shardstone_slab",
            () -> new SlabBlock(stone(MapColor.COLOR_PURPLE, 2.0F)));
    public static final DeferredBlock<Block> CHISELED_SHARDSTONE_BRICKS = registerWithItem("chiseled_shardstone_bricks",
            () -> new Block(stone(MapColor.COLOR_PURPLE, 2.0F).lightLevel(state -> 4)));

    // --- Erze & Rohstoffbloecke ---
    public static final DeferredBlock<ShardOreBlock> SHARD_ORE = registerWithItem("shard_ore",
            () -> new ShardOreBlock(UniformInt.of(2, 5), stone(MapColor.COLOR_PURPLE, 3.0F).lightLevel(state -> 3).randomTicks()));
    public static final DeferredBlock<ShardOreBlock> DEEPSLATE_SHARD_ORE = registerWithItem("deepslate_shard_ore",
            () -> new ShardOreBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).lightLevel(state -> 3).randomTicks()));
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
    public static final DeferredBlock<StairBlock> CRYSTAL_STAIRS = registerWithItem("crystal_stairs",
            () -> new StairBlock(CRYSTAL_PLANKS.get().defaultBlockState(), wood()));
    public static final DeferredBlock<SlabBlock> CRYSTAL_SLAB = registerWithItem("crystal_slab", () -> new SlabBlock(wood()));
    public static final DeferredBlock<FenceBlock> CRYSTAL_FENCE = registerWithItem("crystal_fence", () -> new FenceBlock(wood()));
    public static final DeferredBlock<FenceGateBlock> CRYSTAL_FENCE_GATE = registerWithItem("crystal_fence_gate",
            () -> new FenceGateBlock(WoodType.CHERRY, wood().forceSolidOn()));
    public static final DeferredBlock<DoorBlock> CRYSTAL_DOOR = registerDoor("crystal_door",
            () -> new DoorBlock(BlockSetType.CHERRY, wood().strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<TrapDoorBlock> CRYSTAL_TRAPDOOR = registerWithItem("crystal_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.CHERRY, wood().strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)));
    public static final DeferredBlock<ButtonBlock> CRYSTAL_BUTTON = registerWithItem("crystal_button",
            () -> new ButtonBlock(BlockSetType.CHERRY, 30, BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<PressurePlateBlock> CRYSTAL_PRESSURE_PLATE = registerWithItem("crystal_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.CHERRY, wood().forceSolidOn().noCollission().strength(0.5F)
                    .pushReaction(PushReaction.DESTROY)));
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

    // --- Licht & Glas ---
    public static final DeferredBlock<LanternBlock> SHARD_LANTERN = registerWithItem("shard_lantern",
            () -> new LanternBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn()
                    .requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.LANTERN).lightLevel(state -> 15)
                    .noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<TransparentBlock> CRYSTAL_GLASS = registerWithItem("crystal_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .lightLevel(state -> 3)));
    public static final DeferredBlock<IronBarsBlock> CRYSTAL_GLASS_PANE = registerWithItem("crystal_glass_pane",
            () -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .lightLevel(state -> 2)));

    // --- Portal ---
    public static final DeferredBlock<Block> SHARD_PORTAL_FRAME = registerWithItem("shard_portal_frame",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(25.0F, 1200.0F).sound(SoundType.AMETHYST).lightLevel(state -> 4)));
    public static final DeferredBlock<ShardPortalBlock> SHARD_PORTAL = BLOCKS.register("shard_portal",
            () -> new ShardPortalBlock(BlockBehaviour.Properties.of().noCollission().strength(-1.0F).sound(SoundType.GLASS)
                    .lightLevel(state -> 11).pushReaction(PushReaction.BLOCK).noLootTable().randomTicks()));

    // --- Boss-Altaere & Trophaeen ---
    public static final DeferredBlock<ColossusAltarBlock> COLOSSUS_ALTAR = registerWithItem("colossus_altar",
            () -> new ColossusAltarBlock(altar()), new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredBlock<QueenAltarBlock> QUEEN_ALTAR = registerWithItem("queen_altar",
            () -> new QueenAltarBlock(altar()), new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredBlock<TrophyBlock> COLOSSUS_TROPHY = registerWithItem("colossus_trophy",
            () -> new TrophyBlock(trophy()), new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredBlock<TrophyBlock> QUEEN_TROPHY = registerWithItem("queen_trophy",
            () -> new TrophyBlock(trophy()), new Item.Properties().rarity(Rarity.EPIC).fireResistant());


    // --- Story ---
    public static final DeferredBlock<MemoryCrystalBlock> MEMORY_CRYSTAL = registerWithItem("memory_crystal",
            () -> new MemoryCrystalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0F, 3600000.0F)
                    .sound(SoundType.AMETHYST_CLUSTER).lightLevel(state -> 10).noOcclusion().noLootTable()),
            new Item.Properties().rarity(Rarity.RARE));

    // --- Shardvirus: infizierte Bloecke (mit leuchtender Overlay-Schicht) ---
    public static final DeferredBlock<InfectedBlock> INFECTED_STONE = registerWithItem("infected_stone",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.8F, 6.0F).sound(SoundType.CALCITE).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_DIRT = registerWithItem("infected_dirt",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.6F)
                    .sound(SoundType.ROOTED_DIRT).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_GRASS = registerWithItem("infected_grass",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.7F)
                    .sound(SoundType.NYLIUM).randomTicks()));
    public static final DeferredBlock<InfectedPillarBlock> INFECTED_LOG = registerWithItem("infected_log",
            () -> new InfectedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F).sound(SoundType.STEM).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_LEAVES = registerWithItem("infected_leaves",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.2F)
                    .randomTicks().sound(SoundType.AZALEA_LEAVES).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)));
    public static final DeferredBlock<InfectedBlock> INFECTED_SAND = registerWithItem("infected_sand",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.6F)
                    .sound(SoundType.SAND).randomTicks()));
    // Nach dem Ausbruch: tiefer im Boden, in Haeusern, im Nether (Glutbrand) und im End (Leerenfaeule)
    public static final DeferredBlock<InfectedBlock> INFECTED_DEEPSLATE = registerWithItem("infected_deepslate",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_PLANKS = registerWithItem("infected_planks",
            () -> new InfectedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_NETHERRACK = registerWithItem("infected_netherrack",
            () -> new InfectedBlock(VirusStrain.EMBER, BlockBehaviour.Properties.of().mapColor(MapColor.NETHER)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.6F).sound(SoundType.NETHERRACK)
                    .lightLevel(state -> 3).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_BLACKSTONE = registerWithItem("infected_blackstone",
            () -> new InfectedBlock(VirusStrain.EMBER, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)
                    .lightLevel(state -> 3).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_SOUL_SOIL = registerWithItem("infected_soul_soil",
            () -> new InfectedBlock(VirusStrain.EMBER, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.6F)
                    .sound(SoundType.SOUL_SOIL).lightLevel(state -> 2).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_NYLIUM = registerWithItem("infected_nylium",
            () -> new InfectedBlock(VirusStrain.EMBER, BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_NYLIUM)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.5F).sound(SoundType.NYLIUM)
                    .lightLevel(state -> 3).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_WART_BLOCK = registerWithItem("infected_wart_block",
            () -> new InfectedBlock(VirusStrain.EMBER, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F)
                    .sound(SoundType.WART_BLOCK).lightLevel(state -> 3).randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_END_STONE = registerWithItem("infected_end_stone",
            () -> new InfectedBlock(VirusStrain.VOID, BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F).sound(SoundType.STONE)
                    .randomTicks()));
    public static final DeferredBlock<InfectedBlock> INFECTED_PURPUR = registerWithItem("infected_purpur",
            () -> new InfectedBlock(VirusStrain.VOID, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)
                    .randomTicks()));

    // --- Gegenmittel ---
    // --- Hueter-Ruinen ---
    public static final DeferredBlock<Block> KEEPER_RUIN_BRICKS = registerWithItem("keeper_ruin_bricks",
            () -> new Block(stone(MapColor.QUARTZ, 2.0F)));
    public static final DeferredBlock<RotatedPillarBlock> KEEPER_RUIN_PILLAR = registerWithItem("keeper_ruin_pillar",
            () -> new RotatedPillarBlock(stone(MapColor.QUARTZ, 2.0F)));
    public static final DeferredBlock<SlabBlock> KEEPER_RUNE_SLAB = registerWithItem("keeper_rune_slab",
            () -> new SlabBlock(stone(MapColor.QUARTZ, 2.0F).lightLevel(state -> 3)));

    public static final DeferredBlock<DecisionAltarBlock> DECISION_ALTAR = registerWithItem("decision_altar",
            () -> new DecisionAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(-1.0F, 3600000.0F)
                    .sound(SoundType.AMETHYST).lightLevel(state -> state.getValue(DecisionAltarBlock.ACTIVE) ? 12 : 4)
                    .noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK)),
            new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredBlock<PurityAnchorBlock> PURITY_ANCHOR = registerWithItem("purity_anchor",
            () -> new PurityAnchorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops()
                    .strength(5.0F, 1200.0F).sound(SoundType.AMETHYST).lightLevel(state -> 15).noOcclusion()),
            new Item.Properties().rarity(Rarity.RARE));

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

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.CHERRY_WOOD).ignitedByLava();
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

    /** Tueren brauchen ein Doppelblock-Item (setzt beide Haelften und raeumt Wasser korrekt). */
    private static <B extends Block> DeferredBlock<B> registerDoor(String name, Supplier<B> block) {
        DeferredBlock<B> holder = BLOCKS.register(name, block);
        BLOCK_ITEMS.add(ModItems.ITEMS.<BlockItem>register(name, () -> new DoubleHighBlockItem(holder.get(), new Item.Properties())));
        return holder;
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
