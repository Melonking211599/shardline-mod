package eu.shardline.mod.entity.mob;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.ShardlineAnimations;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Leuchtmotte (friedlich): fliegende Lichtquelle. Mit Leuchtblumen zaehmbar, folgt dann ihrem
 * Besitzer und erhellt die Umgebung. Rechtsklick des Besitzers laesst sie sitzen.
 */
public class GlowMothEntity extends TamableAnimal implements FlyingAnimal, GeoEntity {
    private static final int LIGHT_INTERVAL = 10;
    /** Mindestabstand (quadriert) zwischen zwei Lichtpunkten, damit keine dichte Lichtspur entsteht. */
    private static final double LIGHT_SPACING_SQR = 9.0;
    /** Licht nur setzen, wenn ein Spieler es sehen kann - jeder Lichtwechsel kostet Licht- und Render-Updates. */
    private static final double LIGHT_PLAYER_RANGE = 40.0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private BlockPos lastLightPos;

    public GlowMothEntity(EntityType<? extends GlowMothEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 5.0F, 1.0F));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModBlocks.GLOWBLOOM.get().asItem());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame() && this.isFood(stack)) {
            stack.consume(1, player);
            if (!this.level().isClientSide) {
                if (this.random.nextInt(3) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isTame() && this.isOwnedBy(player) && !this.isFood(stack)) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.navigation.stop();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount % LIGHT_INTERVAL == 0 && this.isAlive()) {
                this.placeLight(serverLevel);
            }
        } else if (this.random.nextInt(4) == 0) {
            this.level().addParticle(ModParticles.SHARD_SPARKLE.get(), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, -0.02, 0.0);
        }
    }

    /**
     * Setzt einen unsichtbaren Lichtblock (entfernt sich selbst, sobald die Motte weiterzieht).
     * Neue Lichtpunkte entstehen nur alle paar Bloecke und nur in Spielernaehe, um Licht-Updates gering zu halten.
     */
    private void placeLight(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        if (this.lastLightPos != null && this.lastLightPos.distSqr(pos) < LIGHT_SPACING_SQR
                && level.getBlockState(this.lastLightPos).is(ModBlocks.MOTH_LIGHT.get())) {
            return;
        }
        if (!level.hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), LIGHT_PLAYER_RANGE)) {
            return;
        }
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, ModBlocks.MOTH_LIGHT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            this.lastLightPos = pos;
        }
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame() && !this.hasCustomName();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.GLOW_MOTH.get().create(level);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GLOW_MOTH_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GLOW_MOTH_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLOW_MOTH_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.isInSittingPose() || this.onGround()) {
                return state.setAndContinue(ShardlineAnimations.SIT);
            }
            return state.setAndContinue(ShardlineAnimations.FLY);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
