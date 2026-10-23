package eu.shardline.mod.entity.mob;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Gesplitterter Ritter (Elite): bewacht die Ruinen-Tuerme. Schwer gepanzert, stuermt mit einem
 * Ansturm auf entfernte Gegner zu. Laesst immer ein Ritterwappen fallen.
 */
public class ShatteredKnightEntity extends Monster implements GeoEntity {
    private static final double BASE_DAMAGE = 12.0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShatteredKnightEntity(EntityType<? extends ShatteredKnightEntity> type, Level level) {
        super(type, level);
        this.xpReward = 25;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BASE_DAMAGE * ShardlineConfig.MOB_DAMAGE_MULTIPLIER.get());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ChargeGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (spawnType == MobSpawnType.STRUCTURE) {
            // Waechter bleiben bei ihrer Ruine.
            this.setPersistenceRequired();
            this.restrictTo(this.blockPosition(), 16);
        }
        return data;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "attack");
        return super.doHurtTarget(target);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHATTERED_KNIGHT_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHATTERED_KNIGHT_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHATTERED_KNIGHT_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.walkIdle(this));
        controllers.add(ShardlineAnimations.actions(this, "attack", "charge"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Ansturm: kurzes Ausholen, dann ein schneller Sprint auf das Ziel; trifft einmal mit Rueckstoss.
     */
    static class ChargeGoal extends Goal {
        private static final int WINDUP = 12;
        private static final int DASH = 10;
        private static final int COOLDOWN = 140;

        private final ShatteredKnightEntity knight;
        private int timer;
        private int cooldown = 60;
        private boolean hit;
        private Vec3 direction = Vec3.ZERO;

        ChargeGoal(ShatteredKnightEntity knight) {
            this.knight = knight;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            LivingEntity target = this.knight.getTarget();
            if (target == null || !target.isAlive() || !this.knight.onGround()) {
                return false;
            }
            double distance = this.knight.distanceToSqr(target);
            return distance > 25.0 && distance < 144.0 && this.knight.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.timer < WINDUP + DASH && this.knight.getTarget() != null;
        }

        @Override
        public void start() {
            this.timer = 0;
            this.hit = false;
            this.knight.getNavigation().stop();
            this.knight.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "charge");
            this.knight.playSound(ModSounds.SHATTERED_KNIGHT_CHARGE.get(), 1.5F, 1.1F);
        }

        @Override
        public void stop() {
            this.cooldown = COOLDOWN;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.knight.getTarget();
            if (target == null) {
                return;
            }
            this.timer++;
            if (this.timer <= WINDUP) {
                this.knight.getLookControl().setLookAt(target, 30.0F, 30.0F);
                Vec3 toTarget = target.position().subtract(this.knight.position()).multiply(1.0, 0.0, 1.0);
                this.direction = toTarget.lengthSqr() > 1.0E-4 ? toTarget.normalize() : Vec3.ZERO;
                return;
            }
            this.knight.setDeltaMovement(this.direction.x * 0.9, this.knight.getDeltaMovement().y, this.direction.z * 0.9);
            if (this.knight.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.knight.getX(), this.knight.getY() + 0.3, this.knight.getZ(),
                        3, 0.3, 0.1, 0.3, 0.02);
            }
            if (!this.hit && this.knight.getBoundingBox().inflate(0.6).intersects(target.getBoundingBox())) {
                this.hit = true;
                float damage = (float) this.knight.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25F;
                if (target.hurt(this.knight.damageSources().mobAttack(this.knight), damage)) {
                    target.knockback(1.5, -this.direction.x, -this.direction.z);
                    target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.3, 0.0));
                    target.hurtMarked = true;
                }
                this.timer = WINDUP + DASH;
            }
        }
    }
}
