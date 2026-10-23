package eu.shardline.mod.entity.mob;

import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.item.ModItems;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Void-Rochen (friedlich, fliegend, reitbar): gleitet in weiten Bahnen ueber dem Void.
 * Mit Leuchtbeeren zaehmbar; gezaehmt und gesattelt kann man auf ihm fliegen (Blickrichtung lenkt).
 */
public class VoidRayEntity extends Animal implements Saddleable, FlyingAnimal, OwnableEntity, GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(VoidRayEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_TAMED = SynchedEntityData.defineId(VoidRayEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private UUID ownerUUID;

    public VoidRayEntity(EntityType<? extends VoidRayEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 6, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(false);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(4, new GlideGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_TAMED, false);
    }

    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    private void setTamedBy(Player player) {
        this.entityData.set(DATA_TAMED, true);
        this.ownerUUID = player.getUUID();
        this.setPersistenceRequired();
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.LUMINOUS_BERRY.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;

        if (!this.isTamed() && this.isFood(stack) && !this.isBaby()) {
            stack.consume(1, player);
            if (!client) {
                if (this.random.nextInt(4) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                    this.setTamedBy(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.isTamed() && stack.is(Items.SADDLE) && this.isSaddleable() && !this.isSaddled()) {
            if (!client) {
                this.equipSaddle(stack.split(1), SoundSource.NEUTRAL);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.isTamed() && this.isSaddled() && !this.isVehicle() && !player.isSecondaryUseActive() && !this.isFood(stack)) {
            if (!client) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return super.mobInteract(player, hand);
    }

    // --- Saddleable ---

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby() && this.isTamed();
    }

    @Override
    public void equipSaddle(ItemStack stack, @Nullable SoundSource source) {
        this.entityData.set(DATA_SADDLED, true);
        if (source != null) {
            this.level().playSound(null, this, this.getSaddleSoundEvent(), source, 0.5F, 1.0F);
        }
    }

    @Override
    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    // --- Reiten ---

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isSaddled() && this.getFirstPassenger() instanceof Player player) {
            return player;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float strafe = player.xxa * 0.5F;
        float forward = player.zza;
        if (forward <= 0.0F) {
            forward *= 0.3F;
        }
        // Blick nach oben/unten steuert die Hoehe.
        float vertical = -Mth.sin(player.getXRot() * Mth.DEG_TO_RAD) * forward;
        return new Vec3(strafe, vertical, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.FLYING_SPEED);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isVehicle()) {
            this.getNavigation().stop();
        }
        if (this.level().isClientSide && this.random.nextInt(3) == 0) {
            this.level().addParticle(ModParticles.VOID_MOTE.get(), this.getRandomX(1.0), this.getY() + 0.2, this.getRandomZ(1.0), 0.0, -0.03, 0.0);
        }
        if (!this.level().isClientSide && this.tickCount % 60 == 0 && this.getDeltaMovement().lengthSqr() > 0.01) {
            this.playSound(ModSounds.VOID_RAY_FLAP.get(), 0.6F, 0.8F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    @Override
    public boolean isFlying() {
        return true;
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
        return !this.isTamed() && !this.hasCustomName();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.VOID_RAY.get().create(level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Saddled", this.isSaddled());
        tag.putBoolean("Tamed", this.isTamed());
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_SADDLED, tag.getBoolean("Saddled"));
        this.entityData.set(DATA_TAMED, tag.getBoolean("Tamed"));
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VOID_RAY_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VOID_RAY_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VOID_RAY_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.flying(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Gleitet zu weit entfernten Wegpunkten, bevorzugt in der mittleren Hoehe ueber dem Void.
     */
    static class GlideGoal extends Goal {
        private final VoidRayEntity ray;

        GlideGoal(VoidRayEntity ray) {
            this.ray = ray;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.ray.isVehicle()) {
                return false;
            }
            if (!this.ray.getMoveControl().hasWanted()) {
                return true;
            }
            double dx = this.ray.getMoveControl().getWantedX() - this.ray.getX();
            double dy = this.ray.getMoveControl().getWantedY() - this.ray.getY();
            double dz = this.ray.getMoveControl().getWantedZ() - this.ray.getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            return distance < 4.0 || distance > 3600.0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Entity owner = this.ray;
            double angle = this.ray.random.nextDouble() * Mth.TWO_PI;
            double distance = 16.0 + this.ray.random.nextDouble() * 24.0;
            double x = owner.getX() + Math.cos(angle) * distance;
            double z = owner.getZ() + Math.sin(angle) * distance;
            double y = Mth.clamp(owner.getY() + (this.ray.random.nextDouble() - 0.5) * 16.0, 50.0, 150.0);
            this.ray.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }
}
