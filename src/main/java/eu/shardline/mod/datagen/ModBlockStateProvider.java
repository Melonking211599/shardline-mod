package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.DecisionAltarBlock;
import eu.shardline.mod.block.custom.ShardPortalBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Blockstates und Blockmodelle. Texturen: assets/shardline/textures/block/&lt;name&gt;.png
 */
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, Shardline.MODID, fileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cube(ModBlocks.SHARDSTONE.get());
        cube(ModBlocks.SHARDSTONE_BRICKS.get());
        cube(ModBlocks.CRACKED_SHARDSTONE_BRICKS.get());
        cube(ModBlocks.SHARD_ORE.get());
        cube(ModBlocks.DEEPSLATE_SHARD_ORE.get());
        cube(ModBlocks.SHARD_BLOCK.get());
        cube(ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        cube(ModBlocks.ASH_BLOCK.get());
        cube(ModBlocks.CRYSTAL_PLANKS.get());
        cube(ModBlocks.SHARD_PORTAL_FRAME.get());
        simpleBlockWithItem(ModBlocks.CRYSTAL_MOSS.get(), models().cubeBottomTop("crystal_moss",
                modLoc("block/crystal_moss_side"), modLoc("block/shardstone"), modLoc("block/crystal_moss")));

        ResourceLocation bricks = blockTexture(ModBlocks.SHARDSTONE_BRICKS.get());
        stairsBlock(ModBlocks.SHARDSTONE_BRICK_STAIRS.get(), bricks);
        slabBlock(ModBlocks.SHARDSTONE_BRICK_SLAB.get(), bricks, bricks);
        wallBlock(ModBlocks.SHARDSTONE_BRICK_WALL.get(), bricks);
        simpleBlockItem(ModBlocks.SHARDSTONE_BRICK_STAIRS.get(), models().getExistingFile(modLoc("block/shardstone_brick_stairs")));
        simpleBlockItem(ModBlocks.SHARDSTONE_BRICK_SLAB.get(), models().getExistingFile(modLoc("block/shardstone_brick_slab")));

        logBlock(ModBlocks.CRYSTAL_LOG.get());
        simpleBlockItem(ModBlocks.CRYSTAL_LOG.get(), models().getExistingFile(modLoc("block/crystal_log")));
        simpleBlockWithItem(ModBlocks.CRYSTAL_LEAVES.get(), models().cubeAll("crystal_leaves",
                blockTexture(ModBlocks.CRYSTAL_LEAVES.get())).renderType("cutout_mipped"));

        cross(ModBlocks.CRYSTAL_SAPLING.get());
        cross(ModBlocks.GLOWBLOOM.get());
        cross(ModBlocks.SHARD_FERN.get());
        cross(ModBlocks.GLOWCAP.get());
        cross(ModBlocks.LUMINOUS_BERRY_BUSH.get());
        cross(ModBlocks.CRYSTAL_WEB.get());

        directionalBlock(ModBlocks.GLOW_CRYSTAL_CLUSTER.get(), models().cross("glow_crystal_cluster",
                blockTexture(ModBlocks.GLOW_CRYSTAL_CLUSTER.get())).renderType("cutout"));

        portal();
        altar(ModBlocks.COLOSSUS_ALTAR.get(), "colossus_altar");
        altar(ModBlocks.QUEEN_ALTAR.get(), "queen_altar");
        trophy(ModBlocks.COLOSSUS_TROPHY.get(), "colossus_trophy");
        trophy(ModBlocks.QUEEN_TROPHY.get(), "queen_trophy");

        simpleBlock(ModBlocks.MOTH_LIGHT.get(), models().getBuilder("moth_light")
                .texture("particle", modLoc("block/glow_crystal_block")));

        // --- Shardvirus: Grundtextur + leuchtende Overlay-Schicht (<name>_glow.png) ---
        infected(ModBlocks.INFECTED_STONE.get(), "infected_stone", "solid");
        infected(ModBlocks.INFECTED_DIRT.get(), "infected_dirt", "solid");
        infected(ModBlocks.INFECTED_SAND.get(), "infected_sand", "solid");
        infected(ModBlocks.INFECTED_LEAVES.get(), "infected_leaves", "cutout_mipped");
        simpleBlockWithItem(ModBlocks.INFECTED_GRASS.get(), overlayModel("infected_grass",
                modLoc("block/infected_grass_side"), modLoc("block/infected_grass_top"), modLoc("block/infected_dirt"),
                modLoc("block/infected_grass_side_glow"), modLoc("block/infected_grass_top_glow"), "cutout"));
        ModelFile log = overlayModel("infected_log", modLoc("block/infected_log"), modLoc("block/infected_log_top"),
                modLoc("block/infected_log_top"), modLoc("block/infected_log_glow"), modLoc("block/infected_log_top_glow"), "cutout");
        axisBlock(ModBlocks.INFECTED_LOG.get(), log, log);
        simpleBlockItem(ModBlocks.INFECTED_LOG.get(), log);

        memoryCrystal();
        keeperRuin();
        decisionAltar();
        purityAnchor();

        // --- Splitterhandwerk ---
        cube(ModBlocks.POLISHED_SHARDSTONE.get());
        cube(ModBlocks.CHISELED_SHARDSTONE_BRICKS.get());
        ResourceLocation polished = blockTexture(ModBlocks.POLISHED_SHARDSTONE.get());
        stairsBlock(ModBlocks.POLISHED_SHARDSTONE_STAIRS.get(), polished);
        slabBlock(ModBlocks.POLISHED_SHARDSTONE_SLAB.get(), polished, polished);
        simpleBlockItem(ModBlocks.POLISHED_SHARDSTONE_STAIRS.get(), models().getExistingFile(modLoc("block/polished_shardstone_stairs")));
        simpleBlockItem(ModBlocks.POLISHED_SHARDSTONE_SLAB.get(), models().getExistingFile(modLoc("block/polished_shardstone_slab")));

        ResourceLocation planks = blockTexture(ModBlocks.CRYSTAL_PLANKS.get());
        stairsBlock(ModBlocks.CRYSTAL_STAIRS.get(), planks);
        slabBlock(ModBlocks.CRYSTAL_SLAB.get(), planks, planks);
        fenceBlock(ModBlocks.CRYSTAL_FENCE.get(), planks);
        fenceGateBlock(ModBlocks.CRYSTAL_FENCE_GATE.get(), planks);
        doorBlockWithRenderType(ModBlocks.CRYSTAL_DOOR.get(), modLoc("block/crystal_door_bottom"), modLoc("block/crystal_door_top"), "cutout");
        trapdoorBlockWithRenderType(ModBlocks.CRYSTAL_TRAPDOOR.get(), modLoc("block/crystal_trapdoor"), true, "cutout");
        buttonBlock(ModBlocks.CRYSTAL_BUTTON.get(), planks);
        pressurePlateBlock(ModBlocks.CRYSTAL_PRESSURE_PLATE.get(), planks);
        simpleBlockItem(ModBlocks.CRYSTAL_STAIRS.get(), models().getExistingFile(modLoc("block/crystal_stairs")));
        simpleBlockItem(ModBlocks.CRYSTAL_SLAB.get(), models().getExistingFile(modLoc("block/crystal_slab")));
        simpleBlockItem(ModBlocks.CRYSTAL_FENCE_GATE.get(), models().getExistingFile(modLoc("block/crystal_fence_gate")));
        simpleBlockItem(ModBlocks.CRYSTAL_TRAPDOOR.get(), models().getExistingFile(modLoc("block/crystal_trapdoor_bottom")));
        simpleBlockItem(ModBlocks.CRYSTAL_PRESSURE_PLATE.get(), models().getExistingFile(modLoc("block/crystal_pressure_plate")));

        ResourceLocation lanternTexture = modLoc("block/shard_lantern");
        ModelFile lantern = models().withExistingParent("shard_lantern", mcLoc("block/template_lantern"))
                .texture("lantern", lanternTexture).renderType("cutout");
        ModelFile hanging = models().withExistingParent("shard_lantern_hanging", mcLoc("block/template_hanging_lantern"))
                .texture("lantern", lanternTexture).renderType("cutout");
        getVariantBuilder(ModBlocks.SHARD_LANTERN.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : lantern).build(), LanternBlock.WATERLOGGED);

        simpleBlockWithItem(ModBlocks.CRYSTAL_GLASS.get(), models().cubeAll("crystal_glass",
                blockTexture(ModBlocks.CRYSTAL_GLASS.get())).renderType("cutout"));
        paneBlockWithRenderType(ModBlocks.CRYSTAL_GLASS_PANE.get(), modLoc("block/crystal_glass"), modLoc("block/crystal_glass_pane_top"), "cutout");
    }

    private void infected(Block block, String name, String renderType) {
        ResourceLocation base = modLoc("block/" + name);
        ResourceLocation glow = modLoc("block/" + name + "_glow");
        BlockModelBuilder model = overlayModel(name, base, base, base, glow, glow, renderType);
        simpleBlockWithItem(block, model);
    }

    /**
     * Wuerfel aus zwei deckungsgleichen Elementen: unten die Grundtextur, darueber die
     * Leuchtschicht mit voller Eigenhelligkeit (leuchtet auch im Dunkeln, ohne Licht abzugeben).
     */
    private BlockModelBuilder overlayModel(String name, ResourceLocation side, ResourceLocation top, ResourceLocation bottom,
                                           ResourceLocation glowSide, ResourceLocation glowTop, String renderType) {
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block"))
                .texture("particle", side).texture("side", side).texture("top", top).texture("bottom", bottom)
                .texture("glow_side", glowSide).texture("glow_top", glowTop)
                .renderType(renderType);
        model.element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.DOWN ? "#bottom" : "#side").cullface(dir))
                .end();
        model.element().from(0, 0, 0).to(16, 16, 16).shade(false).emissivity(15, 15)
                .allFaces((dir, face) -> face.texture(dir.getAxis() == Direction.Axis.Y ? "#glow_top" : "#glow_side").cullface(dir))
                .end();
        return model;
    }

    /** Erinnerungskristall: Sockel und drei leuchtende Kristallspitzen. */
    private void memoryCrystal() {
        ResourceLocation texture = modLoc("block/memory_crystal");
        ResourceLocation base = modLoc("block/shardstone_bricks");
        BlockModelBuilder model = models().getBuilder("memory_crystal").texture("particle", texture).texture("crystal", texture)
                .texture("base", base).renderType("cutout");
        model.element().from(3, 0, 3).to(13, 2, 13).allFaces((dir, face) -> face.texture("#base")).end();
        model.element().from(6, 2, 6).to(10, 15, 10).emissivity(12, 12).allFaces((dir, face) -> face.texture("#crystal")).end();
        model.element().from(4, 2, 7).to(6, 10, 9).emissivity(12, 12)
                .rotation().angle(22.5F).axis(Direction.Axis.Z).origin(5, 2, 8).end()
                .allFaces((dir, face) -> face.texture("#crystal")).end();
        model.element().from(10, 2, 7).to(12, 9, 9).emissivity(12, 12)
                .rotation().angle(-22.5F).axis(Direction.Axis.Z).origin(11, 2, 8).end()
                .allFaces((dir, face) -> face.texture("#crystal")).end();
        simpleBlockWithItem(ModBlocks.MEMORY_CRYSTAL.get(), model);
    }

    /**
     * Entscheidungsaltar: Sockel, Saeule, Schale mit Kristall. Erwacht (lit=true) liegt ueber allem eine
     * Leuchtschicht mit voller Eigenhelligkeit; ruhend leuchtet nur der Kristall schwach.
     */
    private void decisionAltar() {
        ResourceLocation side = modLoc("block/decision_altar_side");
        ResourceLocation top = modLoc("block/decision_altar_top");
        ResourceLocation bottom = modLoc("block/decision_altar_bottom");
        ResourceLocation sideGlow = modLoc("block/decision_altar_side_glow");
        ResourceLocation topGlow = modLoc("block/decision_altar_top_glow");
        BlockModelBuilder dormant = altarModel("decision_altar", side, top, bottom, sideGlow, topGlow, false);
        BlockModelBuilder awake = altarModel("decision_altar_active", side, top, bottom, sideGlow, topGlow, true);
        getVariantBuilder(ModBlocks.DECISION_ALTAR.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(DecisionAltarBlock.ACTIVE) ? awake : dormant).build());
        simpleBlockItem(ModBlocks.DECISION_ALTAR.get(), awake);
    }

    private BlockModelBuilder altarModel(String name, ResourceLocation side, ResourceLocation top, ResourceLocation bottom,
                                         ResourceLocation sideGlow, ResourceLocation topGlow, boolean awake) {
        BlockModelBuilder model = models().getBuilder(name).texture("particle", side).texture("side", side).texture("top", top)
                .texture("bottom", bottom).texture("glow_side", sideGlow).texture("glow_top", topGlow).renderType("cutout");
        float[][] parts = {{1, 0, 1, 15, 3, 15}, {4, 3, 4, 12, 10, 12}, {2, 10, 2, 14, 13, 14}};
        for (float[] p : parts) {
            model.element().from(p[0], p[1], p[2]).to(p[3], p[4], p[5])
                    .allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.DOWN ? "#bottom" : "#side")).end();
        }
        // Kristall in der Schale
        model.element().from(6, 13, 6).to(10, 16, 10).emissivity(awake ? 15 : 7, awake ? 15 : 7)
                .allFaces((dir, face) -> face.texture("#glow_top")).end();
        if (awake) {
            for (float[] p : parts) {
                model.element().from(p[0], p[1], p[2]).to(p[3], p[4], p[5]).shade(false).emissivity(15, 15)
                        .allFaces((dir, face) -> face.texture(dir.getAxis() == Direction.Axis.Y ? "#glow_top" : "#glow_side")).end();
            }
        }
        return model;
    }

    /** Hueter-Ruinen: Ziegel, Saeule (drehbar) und Runenstufe mit leuchtender Rune auf der Oberseite. */
    private void keeperRuin() {
        cube(ModBlocks.KEEPER_RUIN_BRICKS.get());
        axisBlock(ModBlocks.KEEPER_RUIN_PILLAR.get(), modLoc("block/keeper_ruin_pillar"), modLoc("block/keeper_ruin_pillar_top"));
        simpleBlockItem(ModBlocks.KEEPER_RUIN_PILLAR.get(), models().getExistingFile(modLoc("block/keeper_ruin_pillar")));
        BlockModelBuilder bottom = runeSlabModel("keeper_rune_slab", 0, 8);
        BlockModelBuilder top = runeSlabModel("keeper_rune_slab_top", 8, 16);
        BlockModelBuilder full = runeSlabModel("keeper_rune_slab_double", 0, 16);
        slabBlock(ModBlocks.KEEPER_RUNE_SLAB.get(), bottom, top, full);
        simpleBlockItem(ModBlocks.KEEPER_RUNE_SLAB.get(), bottom);
    }

    private BlockModelBuilder runeSlabModel(String name, float y0, float y1) {
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block"))
                .texture("particle", modLoc("block/keeper_rune_slab_side")).texture("side", modLoc("block/keeper_rune_slab_side"))
                .texture("top", modLoc("block/keeper_rune_slab")).texture("bottom", modLoc("block/keeper_ruin_bricks"))
                .texture("glow", modLoc("block/keeper_rune_slab_glow")).renderType("cutout");
        model.element().from(0, y0, 0).to(16, y1, 16)
                .allFaces((dir, face) -> {
                    face.texture(dir == Direction.UP ? "#top" : dir == Direction.DOWN ? "#bottom" : "#side");
                    if (dir != Direction.UP || y1 == 16) {
                        face.cullface(dir);
                    }
                }).end();
        model.element().from(0, y0, 0).to(16, y1, 16).shade(false).emissivity(15, 15)
                .face(Direction.UP).texture("#glow").cullface(y1 == 16 ? Direction.UP : null).end().end();
        return model;
    }

    /** Reinheitsanker: Sockel, Saeule und schwebender Kristall. */
    private void purityAnchor() {
        ResourceLocation side = modLoc("block/purity_anchor");
        ResourceLocation crystal = modLoc("block/purity_anchor_crystal");
        BlockModelBuilder model = models().getBuilder("purity_anchor").texture("particle", side).texture("side", side)
                .texture("crystal", crystal).renderType("cutout");
        model.element().from(1, 0, 1).to(15, 3, 15).allFaces((dir, face) -> face.texture("#side")).end();
        model.element().from(4, 3, 4).to(12, 12, 12).allFaces((dir, face) -> face.texture("#side")).end();
        model.element().from(5, 12, 5).to(11, 16, 11).emissivity(15, 15).allFaces((dir, face) -> face.texture("#crystal")).end();
        simpleBlockWithItem(ModBlocks.PURITY_ANCHOR.get(), model);
    }

    private void cube(Block block) {
        simpleBlockWithItem(block, cubeAll(block));
    }

    private void cross(Block block) {
        String name = name(block);
        simpleBlock(block, models().cross(name, blockTexture(block)).renderType("cutout"));
    }

    private void portal() {
        ResourceLocation texture = modLoc("block/shard_portal");
        ModelFile ns = models().withExistingParent("shard_portal_ns", mcLoc("block/nether_portal_ns"))
                .texture("particle", texture).texture("portal", texture).renderType("translucent");
        ModelFile ew = models().withExistingParent("shard_portal_ew", mcLoc("block/nether_portal_ew"))
                .texture("particle", texture).texture("portal", texture).renderType("translucent");
        getVariantBuilder(ModBlocks.SHARD_PORTAL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ShardPortalBlock.AXIS) == Direction.Axis.Z ? ew : ns)
                .build());
    }

    /** Altar: Sockel, Mittelstueck und Kristall. */
    private void altar(Block block, String name) {
        ResourceLocation texture = modLoc("block/" + name);
        ResourceLocation top = modLoc("block/" + name + "_top");
        BlockModelBuilder model = models().getBuilder(name).texture("particle", texture).texture("side", texture).texture("top", top);
        model.element().from(0, 0, 0).to(16, 4, 16)
                .allFaces((dir, face) -> face.texture(dir.getAxis() == Direction.Axis.Y ? "#top" : "#side")).end();
        model.element().from(2, 4, 2).to(14, 10, 14)
                .allFaces((dir, face) -> face.texture(dir.getAxis() == Direction.Axis.Y ? "#top" : "#side")).end();
        model.element().from(5, 10, 5).to(11, 16, 11)
                .allFaces((dir, face) -> face.texture("#top")).end();
        simpleBlockWithItem(block, model);
    }

    /** Trophaee: Sockel und schwebender Kristall, horizontal drehbar. */
    private void trophy(Block block, String name) {
        ResourceLocation texture = modLoc("block/" + name);
        ResourceLocation base = modLoc("block/shardstone_bricks");
        BlockModelBuilder model = models().getBuilder(name).texture("particle", texture).texture("base", base).texture("crystal", texture);
        model.element().from(2, 0, 2).to(14, 3, 14).allFaces((dir, face) -> face.texture("#base")).end();
        model.element().from(4, 3, 4).to(12, 5, 12).allFaces((dir, face) -> face.texture("#base")).end();
        model.element().from(5, 5, 5).to(11, 15, 11).allFaces((dir, face) -> face.texture("#crystal")).end();
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }

    private static String name(Block block) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
