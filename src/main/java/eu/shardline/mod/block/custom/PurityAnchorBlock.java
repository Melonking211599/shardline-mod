package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.block.entity.PurityAnchorBlockEntity;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.effect.ShardFeverEffect;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.virus.InfectionMap;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.PurityAnchors;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusSpreadManager;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Reinheitsanker: verhindert jede Ausbreitung des Virus im Schutzradius, reinigt dort nach und
 * nach infizierte Bloecke und heilt das Splitterfieber von Lebewesen in der Naehe.
 *
 * <p>Die Zone waechst: Sie beginnt beim Startradius und erreicht nach {@code anchorGrowSeconds} den vollen Radius.
 * Jeder neu hinzugekommene Ring wird ueber {@link Purification#purifyShell} gereinigt (mit Budget pro Tick);
 * innerhalb der Zone reinigt der Anker zusaetzlich zufaellige Positionen. Der Rand wird mit goldenen Funken
 * markiert, der Client zeichnet dort eine goldene Linie am Boden ({@link PurityAnchorBlockEntity}).</p>
 */
public class PurityAnchorBlock extends Block implements EntityBlock {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PurityAnchorBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            ShardlineWorldData data = StoryManager.data(serverLevel.getServer());
            data.addAnchor(serverLevel.dimension(), pos);
            data.setAnchorSince(serverLevel.dimension(), pos, serverLevel.getGameTime());
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.shardline.purity_anchor.tooltip", ShardlineConfig.ANCHOR_RADIUS.get())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("block.shardline.purity_anchor.tooltip2").withStyle(ChatFormatting.GOLD));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            int radius = Mth.floor(PurityAnchors.radius(serverLevel, pos));
            int max = Math.max(ShardlineConfig.ANCHOR_RADIUS.get(), ShardlineConfig.ANCHOR_START_RADIUS.get());
            player.displayClientMessage(radius < max
                    ? Component.translatable("message.shardline.purity_anchor_growing", radius, max)
                    : Component.translatable("message.shardline.purity_anchor_info", radius), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Alle zwei Sekunden: Zone wachsen lassen, Fieber heilen, Immunitaet geben, Bloecke im Radius reinigen. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, PULSE_TICKS);
        ShardlineWorldData data = StoryManager.data(level.getServer());
        data.addAnchor(level.dimension(), pos);
        long since = data.anchorSince(level.dimension(), pos.asLong());
        long now = level.getGameTime();
        double current = PurityAnchors.radius(since, now);
        int radius = Mth.floor(current);
        if (level.getBlockEntity(pos) instanceof PurityAnchorBlockEntity anchor) {
            anchor.sync(since, ShardlineConfig.ANCHOR_START_RADIUS.get(), ShardlineConfig.ANCHOR_RADIUS.get(),
                    ShardlineConfig.ANCHOR_GROW_SECONDS.get() * 20L);
        }

        // Wachstum: neu hinzugekommene Ringe gruendlich reinigen (beim ersten Puls die ganze Startzone)
        int previous = since >= 0L && now - since > PULSE_TICKS ? Mth.floor(PurityAnchors.radius(since, now - PULSE_TICKS)) : -1;
        if (since >= 0L && radius > previous) {
            Purification.purifyShell(level, pos, previous, radius, null);
            if (previous >= 0) {
                level.playSound(null, pos, ModSounds.PURITY_ANCHOR_GROW.get(), SoundSource.BLOCKS, 0.6F, 0.8F + radius * 0.02F);
            }
        }
        edgeParticles(level, pos, current);

        int immunity = ShardlineConfig.IMMUNITY_ANCHOR_SECONDS.get() * 20;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(radius))) {
            if (entity.distanceToSqr(pos.getCenter()) <= (double) radius * radius) {
                entity.removeEffectsCuredBy(ShardFeverEffect.PURIFICATION);
                VirusEffects.giveImmunity(entity, immunity, true);
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

    /** Goldene Funken auf dem Boden entlang des Zonenrands (nur in geladenen Chunks, Hoehe aus der Hoehenkarte). */
    private static void edgeParticles(ServerLevel level, BlockPos center, double radius) {
        int points = Mth.clamp((int) (radius * 2.0), 12, 48);
        double offset = (level.getGameTime() / 40L) * 0.37;
        for (int i = 0; i < points; i++) {
            double angle = offset + i * Math.PI * 2.0 / points;
            int x = Mth.floor(center.getX() + 0.5 + Math.cos(angle) * radius);
            int z = Mth.floor(center.getZ() + 0.5 + Math.sin(angle) * radius);
            if (!level.hasChunkAt(new BlockPos(x, center.getY(), z))) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (Math.abs(y - center.getY()) > 12) {
                continue;
            }
            level.sendParticles(ModParticles.PURITY_MOTE.get(), x + 0.5, y + 0.15, z + 0.5, 1, 0.2, 0.05, 0.2, 0.01);
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
