package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.ShardPortalBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
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
        purityAnchor();
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
