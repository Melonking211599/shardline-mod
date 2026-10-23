package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.shardline.mod.core.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kristallsetzling. Waechst wie ein normaler Setzling, aber auf dem Boden der Splitterweite.
 */
public class CrystalSaplingBlock extends SaplingBlock {
    public static final MapCodec<CrystalSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(TreeGrower.CODEC.fieldOf("tree").forGetter(block -> block.treeGrower), propertiesCodec())
            .apply(instance, CrystalSaplingBlock::new));

    public CrystalSaplingBlock(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public MapCodec<? extends SaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.SHARD_PLANT_SOIL);
    }
}
