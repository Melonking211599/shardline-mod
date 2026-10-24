package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.shardline.mod.virus.Infected;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusSpreadManager;
import eu.shardline.mod.virus.VirusStrain;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vom Virus befallener Block (Stein, Erde, Gras, Sand, Blaetter; im Nether und End die Bloecke der dortigen Staemme).
 * Breitet sich bei Random Ticks aus, setzt Sporen frei und wirkt auf Lebewesen, die darauf stehen.
 */
public class InfectedBlock extends Block implements Infected {
    public static final MapCodec<InfectedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            VirusStrain.CODEC.fieldOf("strain").forGetter(InfectedBlock::strain), propertiesCodec()).apply(instance, InfectedBlock::new));

    private final VirusStrain strain;

    public InfectedBlock(Properties properties) {
        this(VirusStrain.SHARD, properties);
    }

    public InfectedBlock(VirusStrain strain, Properties properties) {
        super(properties);
        this.strain = strain;
    }

    @Override
    public VirusStrain strain() {
        return this.strain;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        VirusSpreadManager.spreadFrom(level, pos, random, this.strain);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide) {
            VirusEffects.onStandingOnInfected(level, entity, this.strain);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        VirusEffects.spawnSpores(level, pos, random, this.strain);
    }
}
