package eu.shardline.mod.entity.mob;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Schattenfuchs (neutral): greift nur an, wenn er provoziert wird. Wird dann fuer einige Sekunden
 * unsichtbar und schneller; der erste Biss aus der Unsichtbarkeit ("Schattenbiss") macht 50 % mehr Schaden.
 */
public class ShadowFoxEntity extends PathfinderMob implements NeutralMob, GeoEntity {
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final ResourceLocation SHADOW_SPEED_ID = Shardline.id("shadow_fox_speed");
    private static final int VANISH_COOLDOWN = 400;
    private static final double BASE_DAMAGE = 4.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private int vanishCooldown;
    private int vanishTicks;

    public ShadowFoxEntity(EntityType<? extends ShadowFoxEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BASE_DAMAGE * ShardlineConfig.MOB_DAMAGE_MULTIPLIER.get());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, false));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null && !this.level().isClientSide) {
            this.vanish(ShardlineConfig.FOX_INVISIBILITY_SECONDS.get() * 20);
        }
    }

    /** Verschwindet in den Schatten: Unsichtbarkeit plus Tempo. */
    private void vanish(int ticks) {
        if (this.vanishCooldown > 0 || this.vanishTicks > 0) {
            return;
        }
        this.vanishTicks = ticks;
        this.vanishCooldown = VANISH_COOLDOWN;
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks, 0, false, false));
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(SHADOW_SPEED_ID)) {
            speed.addTransientModifier(new AttributeModifier(SHADOW_SPEED_ID, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.playSound(ModSounds.SHADOW_FOX_VANISH.get(), 1.0F, 1.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 20, 0.3, 0.3, 0.3, 0.02);
        }
    }

    private void reveal() {
        this.vanishTicks = 0;
        this.removeEffect(MobEffects.INVISIBILITY);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SHADOW_SPEED_ID);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean ambush = this.vanishTicks > 0;
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "attack");
        boolean hurt;
        if (ambush) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
            hurt = target.hurt(this.damageSources().mobAttack(this), damage);
            this.reveal();
        } else {
            hurt = super.doHurtTarget(target);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.vanishCooldown > 0) {
                this.vanishCooldown--;
            }
            if (this.vanishTicks > 0 && --this.vanishTicks == 0) {
                this.reveal();
            }
            // Gelegentliches Verschwinden im Dunkeln, auch ohne Ziel.
            if (this.getTarget() == null && this.vanishCooldown == 0 && this.random.nextInt(800) == 0
                    && serverLevel.getMaxLocalRawBrightness(this.blockPosition()) < 7) {
                this.vanish(80);
            }
            this.updatePersistentAnger(serverLevel, true);
        } else if (this.random.nextInt(6) == 0) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4), 0.0, 0.01, 0.0);
        }
    }

    // --- NeutralMob ---

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
        tag.putInt("VanishCooldown", this.vanishCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
        this.vanishCooldown = tag.getInt("VanishCooldown");
    }

    // --- Sounds ---

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHADOW_FOX_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHADOW_FOX_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHADOW_FOX_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.walkIdle(this));
        controllers.add(ShardlineAnimations.actions(this, "attack"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
