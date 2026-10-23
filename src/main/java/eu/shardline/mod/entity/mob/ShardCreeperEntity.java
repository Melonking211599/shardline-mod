package eu.shardline.mod.entity.mob;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Splitterkriecher (feindlich): schleicht sich an, laedt sich auf und zerspringt in eine Wolke aus
 * Kristallsplittern, die in alle Richtungen fliegen.
 */
public class ShardCreeperEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData.defineId(ShardCreeperEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation SWELL = RawAnimation.begin().thenLoop("swell");
    private static final int MAX_SWELL = 30;
    private static final float EXPLOSION_POWER = 1.5F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int oldSwell;
    private int swell;

    public ShardCreeperEntity(EntityType<? extends ShardCreeperEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SwellGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int dir) {
        this.entityData.set(DATA_SWELL_DIR, dir);
    }

    /** Fortschritt des Aufladens (0-1) fuer den Renderer. */
    public float getSwelling(float partialTick) {
        return Mth.lerp(partialTick, (float) this.oldSwell, (float) this.swell) / (float) (MAX_SWELL - 2);
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.swell;
            int dir = this.getSwellDir();
            if (dir > 0 && this.swell == 0) {
                this.playSound(ModSounds.SHARD_CREEPER_FUSE.get(), 1.0F, 0.5F);
            }
            this.swell = Mth.clamp(this.swell + dir, 0, MAX_SWELL);
            if (this.swell >= MAX_SWELL) {
                this.shatter();
            }
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Kein Nahkampf - der Kriecher greift nur durch Zerspringen an.
        return true;
    }

    private void shatter() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.dead = true;
        Level.ExplosionInteraction interaction = ShardlineConfig.CREEPER_BREAKS_BLOCKS.get()
                ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
        serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER, interaction);

        int count = ShardlineConfig.CREEPER_SHARD_COUNT.get();
        float damage = ShardlineConfig.CREEPER_SHARD_DAMAGE.get().floatValue();
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < count; i++) {
            // Gleichmaessig verteilte Richtungen auf der oberen Halbkugel (Fibonacci-Spirale).
            double y = 1.0 - (i / (double) Math.max(1, count - 1)) * 0.9;
            double radius = Math.sqrt(1.0 - y * y);
            double theta = golden * i;
            Vec3 direction = new Vec3(Math.cos(theta) * radius, y * 0.6, Math.sin(theta) * radius);
            ShardProjectileEntity.shoot(serverLevel, this, this.getX(), this.getY() + 0.8, this.getZ(), direction, 1.1F, 2.0F, damage);
        }
        serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY() + 0.8, this.getZ(), 60, 0.6, 0.6, 0.6, 0.3);
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Fuse", (short) this.swell);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.swell = tag.getShort("Fuse");
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHARD_CREEPER_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHARD_CREEPER_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.getSwellDir() > 0) {
                return state.setAndContinue(SWELL);
            }
            return state.setAndContinue(state.isMoving() ? ShardlineAnimations.WALK : ShardlineAnimations.IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Wie der Vanilla-Creeper: zuenden, wenn das Ziel nah ist. */
    static class SwellGoal extends Goal {
        private final ShardCreeperEntity creeper;
        @Nullable
        private LivingEntity target;

        SwellGoal(ShardCreeperEntity creeper) {
            this.creeper = creeper;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity current = this.creeper.getTarget();
            return this.creeper.getSwellDir() > 0 || current != null && this.creeper.distanceToSqr(current) < 9.0;
        }

        @Override
        public void start() {
            this.creeper.getNavigation().stop();
            this.target = this.creeper.getTarget();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null || this.creeper.distanceToSqr(this.target) > 49.0 || !this.creeper.getSensing().hasLineOfSight(this.target)) {
                this.creeper.setSwellDir(-1);
            } else {
                this.creeper.setSwellDir(1);
            }
        }
    }
}
