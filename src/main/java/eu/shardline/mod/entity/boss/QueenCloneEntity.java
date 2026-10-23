package eu.shardline.mod.entity.boss;

import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.entity.ai.HoveringMoveControl;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Spiegelbild der Splitterkoenigin (Phase 2). Zerspringt bei jedem Treffer eines Spielers,
 * verschiesst schwache Splitter und loest sich nach 20 Sekunden auf.
 */
public class QueenCloneEntity extends Monster implements GeoEntity {
    private static final int LIFETIME = 400;
    private static final float PROJECTILE_DAMAGE = 3.0F;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int shootCooldown = 40;

    public QueenCloneEntity(EntityType<? extends QueenCloneEntity> type, Level level) {
        super(type, level);
        this.moveControl = new HoveringMoveControl(this, 0.06);
        this.setNoGravity(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new CircleTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount > LIFETIME) {
            this.shatter();
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && --this.shootCooldown <= 0 && this.hasLineOfSight(target)) {
            Vec3 origin = new Vec3(this.getX(), this.getY(0.6), this.getZ());
            Vec3 aim = target.getEyePosition().subtract(origin);
            ShardProjectileEntity.shoot(this.level(), this, origin.x, origin.y, origin.z, aim, 1.2F, 3.0F, PROJECTILE_DAMAGE);
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "cast");
            this.shootCooldown = 50 + this.random.nextInt(30);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player && !this.isInvulnerableTo(source)) {
            // Spiegelbilder zerspringen beim ersten Treffer.
            amount = Math.max(amount, this.getHealth() + 1.0F);
        } else if (!(source.getEntity() instanceof Player) && source.getEntity() != null) {
            return false;
        }
        return super.hurt(source, amount);
    }

    private void shatter() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY(0.5), this.getZ(), 40, 0.5, 1.0, 0.5, 0.2);
        }
        this.playSound(ModSounds.SHARD_SHATTER.get(), 1.0F, 0.8F);
        this.discard();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY(0.5), this.getZ(), 40, 0.5, 1.0, 0.5, 0.2);
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHARD_SHATTER.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHARD_SHATTER.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.flying(this));
        controllers.add(ShardlineAnimations.actions(this, "cast"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Kreist in mittlerer Entfernung um das Ziel. */
    static class CircleTargetGoal extends Goal {
        private final QueenCloneEntity clone;

        CircleTargetGoal(QueenCloneEntity clone) {
            this.clone = clone;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.clone.getMoveControl().hasWanted() && this.clone.random.nextInt(reducedTickDelay(8)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = this.clone.getTarget();
            Vec3 anchor = target != null ? target.position() : this.clone.position();
            double angle = this.clone.random.nextDouble() * Mth.TWO_PI;
            double distance = 5.0 + this.clone.random.nextDouble() * 6.0;
            this.clone.getMoveControl().setWantedPosition(anchor.x + Math.cos(angle) * distance,
                    anchor.y + 3.0 + this.clone.random.nextDouble() * 4.0, anchor.z + Math.sin(angle) * distance, 1.0);
        }
    }
}
