package eu.shardline.mod.entity.mob;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Kristallspinne (feindlich): klettert wie eine Spinne und hinterlaesst bei Treffern
 * verlangsamende Kristallnetze, die nach einigen Sekunden zerfallen.
 */
public class CrystalSpiderEntity extends Spider implements GeoEntity {
    private static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("climb");
    private static final double BASE_DAMAGE = 4.0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CrystalSpiderEntity(EntityType<? extends CrystalSpiderEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BASE_DAMAGE * ShardlineConfig.MOB_DAMAGE_MULTIPLIER.get());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "attack");
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && this.random.nextDouble() < ShardlineConfig.SPIDER_WEB_CHANCE.get()) {
            BlockPos feet = living.blockPosition();
            BlockState state = this.level().getBlockState(feet);
            if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                this.level().setBlock(feet, ModBlocks.CRYSTAL_WEB.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
        }
        return hurt;
    }

    @Override
    public void makeStuckInBlock(BlockState state, net.minecraft.world.phys.Vec3 motionMultiplier) {
        if (!state.is(ModBlocks.CRYSTAL_WEB.get())) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTAL_SPIDER_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTAL_SPIDER_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTAL_SPIDER_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.isClimbing()) {
                return state.setAndContinue(CLIMB);
            }
            return state.setAndContinue(state.isMoving() ? ShardlineAnimations.WALK : ShardlineAnimations.IDLE);
        }));
        controllers.add(ShardlineAnimations.actions(this, "attack"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
