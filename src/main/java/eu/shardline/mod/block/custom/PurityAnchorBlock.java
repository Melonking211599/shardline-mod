package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.effect.ShardFeverEffect;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.virus.InfectionMap;
import eu.shardline.mod.virus.VirusSpreadManager;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Reinheitsanker: verhindert jede Ausbreitung des Virus im Schutzradius, reinigt dort nach und
 * nach infizierte Bloecke und heilt das Splitterfieber von Lebewesen in der Naehe.
 */
public class PurityAnchorBlock extends Block {
    public static final MapCodec<PurityAnchorBlock> CODEC = simpleCodec(PurityAnchorBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0),
            Block.box(4.0, 3.0, 4.0, 12.0, 12.0, 12.0),
            Block.box(5.0, 12.0, 5.0, 11.0, 16.0, 11.0));
    private static final int PULSE_TICKS = 40;

    public PurityAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            StoryManager.data(serverLevel.getServer()).addAnchor(serverLevel.dimension(), pos);
            serverLevel.scheduleTick(pos, this, PULSE_TICKS);
            serverLevel.playSound(null, pos, ModSounds.PURITY_ANCHOR_ACTIVATE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !newState.is(this)) {
            StoryManager.data(serverLevel.getServer()).removeAnchor(serverLevel.dimension(), pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof ServerPlayer player) {
            StoryManager.award(player, "story/purity_anchor");
            StoryEvents.onContainmentAction(player);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.shardline.purity_anchor_info", ShardlineConfig.ANCHOR_RADIUS.get()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Alle zwei Sekunden: Fieber heilen, einige zufaellige Bloecke im Radius reinigen. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, PULSE_TICKS);
        StoryManager.data(level.getServer()).addAnchor(level.dimension(), pos);
        int radius = ShardlineConfig.ANCHOR_RADIUS.get();

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(radius))) {
            if (entity.distanceToSqr(pos.getCenter()) <= (double) radius * radius) {
                entity.removeEffectsCuredBy(ShardFeverEffect.PURIFICATION);
            }
        }

        int attempts = ShardlineConfig.ANCHOR_PURIFY_PER_PULSE.get();
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        for (int i = 0; i < attempts; i++) {
            target.set(pos.getX() + random.nextIntBetweenInclusive(-radius, radius),
                    pos.getY() + random.nextIntBetweenInclusive(-radius / 2, radius / 2),
                    pos.getZ() + random.nextIntBetweenInclusive(-radius, radius));
            if (target.distSqr(pos) > (double) radius * radius) {
                continue;
            }
            BlockState found = InfectionMap.stateIfLoaded(level, target);
            if (found != null && InfectionMap.isInfected(found) && VirusSpreadManager.purify(level, target.immutable(), found)) {
                level.sendParticles(ParticleTypes.END_ROD, target.getX() + 0.5, target.getY() + 1.1, target.getZ() + 0.5, 3, 0.3, 0.1, 0.3, 0.01);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double angle = (level.getGameTime() % 80) / 80.0 * Math.PI * 2.0;
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + Math.cos(angle) * 0.6, pos.getY() + 1.1,
                pos.getZ() + 0.5 + Math.sin(angle) * 0.6, 0.0, 0.03, 0.0);
        if (random.nextInt(3) == 0) {
            float a = random.nextFloat() * Mth.TWO_PI;
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5 + Mth.cos(a) * 2.0, pos.getY() + 0.3,
                    pos.getZ() + 0.5 + Mth.sin(a) * 2.0, 0.0, 0.02, 0.0);
        }
    }
}
