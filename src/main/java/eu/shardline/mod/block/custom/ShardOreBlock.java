package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.shardline.mod.virus.VirusSpreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shard-Erz. Nach dem Bruch ist jedes freiliegende Erz eine Quelle des Virus: bei Random Ticks
 * infiziert es benachbarte Bloecke und wird als Infektionsherd vermerkt.
 */
public class ShardOreBlock extends DropExperienceBlock {
    public static final MapCodec<ShardOreBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntProvider.codec(0, 10).fieldOf("experience").forGetter(block -> block.xpRange),
            propertiesCodec()).apply(instance, ShardOreBlock::new));

    private final IntProvider xpRange;

    public ShardOreBlock(IntProvider xpRange, Properties properties) {
        super(xpRange, properties);
        this.xpRange = xpRange;
    }

    @Override
    public MapCodec<? extends DropExperienceBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        VirusSpreadManager.seedFromOre(level, pos, random);
    }
}
