package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.virus.Infected;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusSpreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vom Shardvirus befallener Block (Stein, Erde, Gras, Sand, Blaetter). Breitet sich bei Random
 * Ticks aus, setzt Sporen frei und wirkt auf Lebewesen, die darauf stehen.
 */
public class InfectedBlock extends Block implements Infected {
    public static final MapCodec<InfectedBlock> CODEC = simpleCodec(InfectedBlock::new);

    public InfectedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        VirusSpreadManager.spreadFrom(level, pos, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide) {
            VirusEffects.onStandingOnInfected(level, entity);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        VirusEffects.spawnSpores(level, pos, random);
    }
}
