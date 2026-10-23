package eu.shardline.mod.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * Direkte Schwebe-Steuerung (aehnlich dem Plagegeist): beschleunigt ohne Pfadsuche zum Zielpunkt
 * und schaut dabei zum aktuellen Ziel.
 */
public class HoveringMoveControl extends MoveControl {
    private final double acceleration;

    public HoveringMoveControl(Mob mob, double acceleration) {
        super(mob);
        this.acceleration = acceleration;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) {
            return;
        }
        Vec3 delta = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
        double distance = delta.length();
        if (distance < this.mob.getBoundingBox().getSize()) {
            this.operation = Operation.WAIT;
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5));
            return;
        }
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(delta.scale(this.speedModifier * this.acceleration / distance)));
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            Vec3 motion = this.mob.getDeltaMovement();
            this.mob.setYRot(-((float) Mth.atan2(motion.x, motion.z)) * Mth.RAD_TO_DEG);
        } else {
            double dx = target.getX() - this.mob.getX();
            double dz = target.getZ() - this.mob.getZ();
            this.mob.setYRot(-((float) Mth.atan2(dx, dz)) * Mth.RAD_TO_DEG);
        }
        this.mob.yBodyRot = this.mob.getYRot();
    }
}
