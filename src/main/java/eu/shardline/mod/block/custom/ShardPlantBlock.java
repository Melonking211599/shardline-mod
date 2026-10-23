package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Leuchtende Pflanze der Splitterweite (Splitterfarn, Leuchtkappe, Leuchtbeerenstrauch).
 * Waechst auf allen Bloecken im Tag {@code shardline:shard_plant_soil}.
 */
public class ShardPlantBlock extends BushBlock {
    public static final MapCodec<ShardPlantBlock> CODEC = simpleCodec(ShardPlantBlock::new);
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public ShardPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.SHARD_PLANT_SOIL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.addParticle(ModParticles.SHARD_SPARKLE.get(),
                    pos.getX() + random.nextDouble(), pos.getY() + 0.2 + random.nextDouble() * 0.6, pos.getZ() + random.nextDouble(),
                    0.0, 0.01, 0.0);
        }
    }
}
