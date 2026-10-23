package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.virus.VirusSpreadManager;
import eu.shardline.mod.world.dimension.ModDimensions;
import eu.shardline.mod.world.portal.ShardPortalForcer;
import eu.shardline.mod.world.portal.ShardPortalShape;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Das Splitterportal. Verbindet Oberwelt und Splitterweite (Massstab 4:1).
 * Eigener Effekt: Kristallfunkeln, eigener Reisesound und Sanfter Fall nach der Ankunft.
 */
public class ShardPortalBlock extends Block implements Portal {
    public static final MapCodec<ShardPortalBlock> CODEC = simpleCodec(ShardPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public ShardPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis portalAxis = state.getValue(AXIS);
        boolean sideways = portalAxis != facingAxis && facingAxis.isHorizontal();
        if (!sideways && !facingState.is(this) && !new ShardPortalShape(level, currentPos, portalAxis).isComplete()) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    /** Nach dem Bruch tragen Portale ausserhalb der Splitterweite den Virus in ihre Umgebung. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.dimension() != ModDimensions.SPLITTERWEITE_LEVEL) {
            VirusSpreadManager.seedFromPortal(level, pos, random);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof Player player) {
            return Math.max(1, level.getGameRules().getInt(player.getAbilities().invulnerable
                    ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                    : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        }
        return 0;
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> targetKey = level.dimension() == ModDimensions.SPLITTERWEITE_LEVEL ? Level.OVERWORLD : ModDimensions.SPLITTERWEITE_LEVEL;
        ServerLevel target = level.getServer().getLevel(targetKey);
        if (target == null) {
            return null;
        }
        WorldBorder border = target.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(level.dimensionType(), target.dimensionType());
        BlockPos exitPos = border.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);

        Optional<BlockPos> existing = ShardPortalForcer.findClosestPortalPosition(target, exitPos, border);
        BlockUtil.FoundRectangle rectangle;
        DimensionTransition.PostDimensionTransition post;
        if (existing.isPresent()) {
            BlockPos portalPos = existing.get();
            BlockState portalState = target.getBlockState(portalPos);
            rectangle = BlockUtil.getLargestRectangleAround(portalPos, portalState.getValue(AXIS), 21, Direction.Axis.Y, 21,
                    p -> target.getBlockState(p) == portalState);
            post = arrivalEffects().then(e -> e.placePortalTicket(portalPos));
        } else {
            Direction.Axis axis = entity.level().getBlockState(pos).getOptionalValue(AXIS).orElse(Direction.Axis.X);
            Optional<BlockUtil.FoundRectangle> created = ShardPortalForcer.createPortal(target, exitPos, axis);
            if (created.isEmpty()) {
                Shardline.LOGGER.error("Splitterportal konnte nicht erzeugt werden (vermutlich ausserhalb der Weltgrenze).");
                return null;
            }
            rectangle = created.get();
            post = arrivalEffects().then(DimensionTransition.PLACE_PORTAL_TICKET);
        }
        return createTransition(entity, pos, rectangle, target, post);
    }

    private static DimensionTransition createTransition(Entity entity, BlockPos pos, BlockUtil.FoundRectangle rectangle, ServerLevel target,
                                                        DimensionTransition.PostDimensionTransition post) {
        BlockState entryState = entity.level().getBlockState(pos);
        Direction.Axis entryAxis;
        Vec3 offset;
        if (entryState.hasProperty(AXIS)) {
            entryAxis = entryState.getValue(AXIS);
            BlockUtil.FoundRectangle entryRect = BlockUtil.getLargestRectangleAround(pos, entryAxis, 21, Direction.Axis.Y, 21,
                    p -> entity.level().getBlockState(p) == entryState);
            offset = entity.getRelativePortalPosition(entryAxis, entryRect);
        } else {
            entryAxis = Direction.Axis.X;
            offset = new Vec3(0.5, 0.0, 0.0);
        }

        BlockPos corner = rectangle.minCorner;
        Direction.Axis exitAxis = target.getBlockState(corner).getOptionalValue(AXIS).orElse(Direction.Axis.X);
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        int rotation = entryAxis == exitAxis ? 0 : 90;
        Vec3 speed = entity.getDeltaMovement();
        Vec3 exitSpeed = entryAxis == exitAxis ? speed : new Vec3(speed.z, speed.y, -speed.x);
        double along = dimensions.width() / 2.0 + (rectangle.axis1Size - dimensions.width()) * offset.x();
        double up = (rectangle.axis2Size - dimensions.height()) * offset.y();
        double across = 0.5 + offset.z();
        boolean xAxis = exitAxis == Direction.Axis.X;
        Vec3 exit = new Vec3(corner.getX() + (xAxis ? along : across), corner.getY() + up, corner.getZ() + (xAxis ? across : along));
        Vec3 safeExit = PortalShape.findCollisionFreePosition(exit, target, entity, dimensions);
        return new DimensionTransition(target, safeExit, exitSpeed, entity.getYRot() + rotation, entity.getXRot(), post);
    }

    /** Eigener Portaleffekt bei der Ankunft. */
    private static DimensionTransition.PostDimensionTransition arrivalEffects() {
        return entity -> {
            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.SHARD_PORTAL_TRAVEL.get(),
                        SoundSource.BLOCKS, 1.0F, 0.8F + serverLevel.random.nextFloat() * 0.4F);
                serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), entity.getX(), entity.getY() + 1.0, entity.getZ(),
                        60, 0.8, 1.2, 0.8, 0.08);
                serverLevel.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.0, entity.getZ(),
                        20, 0.5, 1.0, 0.5, 0.05);
            }
            int seconds = ShardlineConfig.PORTAL_SLOW_FALLING_SECONDS.get();
            if (seconds > 0 && entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, seconds * 20, 0, false, true, true));
            }
        };
    }

    @Override
    public Transition getLocalTransition() {
        return Transition.NONE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.SHARD_PORTAL_AMBIENT.get(),
                    SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double dx = (random.nextFloat() - 0.5) * 0.3;
            double dy = random.nextFloat() * 0.15;
            double dz = (random.nextFloat() - 0.5) * 0.3;
            int side = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                x = pos.getX() + 0.5 + 0.25 * side;
                dx = random.nextFloat() * 0.4F * side;
            } else {
                z = pos.getZ() + 0.5 + 0.25 * side;
                dz = random.nextFloat() * 0.4F * side;
            }
            level.addParticle(i == 0 ? ParticleTypes.REVERSE_PORTAL : ModParticles.SHARD_SPARKLE.get(), x, y, z, dx, dy, dz);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z);
            default -> state;
        };
    }
}
