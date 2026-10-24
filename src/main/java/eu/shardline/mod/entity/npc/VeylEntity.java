package eu.shardline.mod.entity.npc;

import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ShardlineAnimations;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Veyl, der letzte Archivar der Hueter. Ein Geist, an die Hueter-Bibliothek gebunden: schwebt langsam um seinen
 * Heimatpunkt, schlaeft nicht, flieht nicht und kann nicht verletzt werden (nur /kill). Bewahrt die Chronik; im
 * versiegelten Ende hilft er, das Siegel zu stimmen, im beherrschten Ende fragt er, ob die Chronik dich verurteilt.
 */
public class VeylEntity extends AftermathNpcEntity {
    /** So weit (Bloecke) treibt Veyl vom Heimatpunkt weg. */
    private static final double DRIFT_RADIUS = 4.0;

    public VeylEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    public String npcId() {
        return "veyl";
    }

    @Override
    protected void registerMovementGoals() {
        this.goalSelector.addGoal(5, new DriftGoal());
    }

    @Override
    protected boolean canRest() {
        return false;
    }

    @Override
    protected boolean fleesFrom(net.minecraft.world.entity.LivingEntity entity) {
        return false;
    }

    /** Geister nehmen keinen Schaden; nur Schaden, der Unverwundbarkeit umgeht, wirkt. Ein Treffer laesst ihn flackern. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        if (!this.level().isClientSide && source.getEntity() != null) {
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "hurt");
            this.playSound(ModSounds.VEYL_HURT.get(), 0.6F, 1.0F);
        }
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTalking() ? null : ModSounds.VEYL_VOICE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VEYL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VEYL_DEATH.get();
    }

    /** Schwebt in kleinen Bahnen um den Heimatpunkt, knapp ueber dem Boden, mit langsamem Auf und Ab. */
    private final class DriftGoal extends Goal {
        private Vec3 target;
        private int retarget;

        DriftGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !VeylEntity.this.isTalking();
        }

        @Override
        public void tick() {
            VeylEntity veyl = VeylEntity.this;
            BlockPos home = veyl.home() != null ? veyl.home() : veyl.blockPosition();
            if (this.target == null || --this.retarget <= 0 || veyl.position().distanceToSqr(this.target) < 0.25) {
                double angle = veyl.getRandom().nextDouble() * Math.PI * 2.0;
                double radius = veyl.getRandom().nextDouble() * DRIFT_RADIUS;
                this.target = new Vec3(home.getX() + 0.5 + Math.cos(angle) * radius, home.getY(), home.getZ() + 0.5 + Math.sin(angle) * radius);
                this.retarget = 120 + veyl.getRandom().nextInt(120);
            }
            double bob = 0.35 + Mth.sin(veyl.tickCount * 0.05F) * 0.15;
            Vec3 goal = new Vec3(this.target.x, this.target.y + bob, this.target.z);
            Vec3 step = goal.subtract(veyl.position());
            double length = step.length();
            if (length > 1.0E-3) {
                veyl.setDeltaMovement(step.scale(Math.min(0.04, length) / length));
                if (step.horizontalDistanceSqr() > 0.01) {
                    veyl.getLookControl().setLookAt(goal.x, veyl.getEyeY(), goal.z);
                }
            }
        }

        @Override
        public void stop() {
            VeylEntity.this.setDeltaMovement(Vec3.ZERO);
        }
    }
}
