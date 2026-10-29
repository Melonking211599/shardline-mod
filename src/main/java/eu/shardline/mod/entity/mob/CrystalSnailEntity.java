package eu.shardline.mod.entity.mob;

import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.item.ModItems;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Kristallschnecke (friedlich): kriecht langsam durch Kristallwald und Leuchtende Hoehlen und folgt Leuchtbeeren.
 * Wird sie getroffen, zieht sie sich fuer einige Sekunden in ihr Kristallhaus zurueck und nimmt dann nur noch ein
 * Fuenftel des Schadens. Hinterlaesst alle paar Minuten einen Kristallschleim (wie ein Huhn sein Ei).
 */
public class CrystalSnailEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Boolean> HIDING = SynchedEntityData.defineId(CrystalSnailEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation HIDE = RawAnimation.begin().thenPlayAndHold("hide");
    private static final int HIDE_TICKS = 100;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int hideTicks;
    private int slimeTime;

    public CrystalSnailEntity(EntityType<? extends CrystalSnailEntity> type, Level level) {
        super(type, level);
        this.xpReward = 2;
        this.slimeTime = this.nextSlimeTime();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.12)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2, Ingredient.of(ModItems.LUMINOUS_BERRY.get()), false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HIDING, false);
    }

    public boolean isHiding() {
        return this.entityData.get(HIDING);
    }

    private int nextSlimeTime() {
        return 6000 + this.random.nextInt(6000);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        float taken = this.isHiding() ? amount * 0.2F : amount;
        boolean hurt = super.hurt(source, taken);
        if (hurt && this.isAlive()) {
            if (!this.isHiding()) {
                this.playSound(ModSounds.CRYSTAL_SNAIL_HIDE.get(), 1.0F, 1.0F);
            }
            this.hideTicks = HIDE_TICKS;
            this.entityData.set(HIDING, true);
            this.getNavigation().stop();
        }
        return hurt;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isHiding();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.hideTicks > 0 && --this.hideTicks == 0) {
                this.entityData.set(HIDING, false);
            }
            if (--this.slimeTime <= 0) {
                this.spawnAtLocation(ModItems.CRYSTAL_SLIME.get());
                this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.8F, 1.4F);
                this.slimeTime = this.nextSlimeTime();
            }
            // Leuchtende Schleimspur beim Kriechen
            if (this.walkDist != this.walkDistO && this.random.nextInt(6) == 0) {
                serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY() + 0.05, this.getZ(), 1, 0.1, 0.0, 0.1, 0.0);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HideTicks", this.hideTicks);
        tag.putInt("SlimeTime", this.slimeTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hideTicks = tag.getInt("HideTicks");
        this.entityData.set(HIDING, this.hideTicks > 0);
        if (tag.contains("SlimeTime")) {
            this.slimeTime = tag.getInt("SlimeTime");
        }
    }

    // --- Sounds ---

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isHiding() ? null : ModSounds.CRYSTAL_SNAIL_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTAL_SNAIL_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTAL_SNAIL_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    // --- Animationen ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.isHiding()) {
                return state.setAndContinue(HIDE);
            }
            return state.setAndContinue(state.isMoving() ? ShardlineAnimations.WALK : ShardlineAnimations.IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
