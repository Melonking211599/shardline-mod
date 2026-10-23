package eu.shardline.mod.entity.misc;

import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.boss.QueenCloneEntity;
import eu.shardline.mod.entity.boss.ShardQueenEntity;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

/**
 * Void-Zone der Splitterkoenigin (Phase 3): ein wachsender Kreis aus Leere am Boden. Zieht Ziele zur
 * Mitte, verursacht jede Sekunde Schaden und Verdorrung.
 */
public class VoidZoneEntity extends Entity implements TraceableEntity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(VoidZoneEntity.class, EntityDataSerializers.FLOAT);
    private static final float START_RADIUS = 1.5F;
    private static final float MAX_RADIUS = 3.5F;
    private static final int GROW_TICKS = 60;
    private static final int LIFETIME = 240;

    private float damage = 4.0F;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public VoidZoneEntity(EntityType<? extends VoidZoneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static VoidZoneEntity create(Level level, Vec3 pos, float damage, LivingEntity owner) {
        VoidZoneEntity zone = new VoidZoneEntity(ModEntities.VOID_ZONE.get(), level);
        zone.setPos(pos.x, pos.y, pos.z);
        zone.damage = damage;
        zone.owner = owner;
        zone.ownerUUID = owner.getUUID();
        return zone;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, START_RADIUS);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_RADIUS.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel
                && serverLevel.getEntity(this.ownerUUID) instanceof LivingEntity living) {
            this.owner = living;
        }
        return this.owner;
    }

    @Override
    public void tick() {
        super.tick();
        float radius = this.getRadius();
        if (this.level().isClientSide) {
            this.spawnParticles(radius);
            return;
        }
        if (this.tickCount <= GROW_TICKS) {
            this.entityData.set(DATA_RADIUS, Mth.lerp(this.tickCount / (float) GROW_TICKS, START_RADIUS, MAX_RADIUS));
        }
        if (this.tickCount > LIFETIME) {
            this.discard();
            return;
        }
        LivingEntity source = this.getOwner();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.0, 2.0, 0.0))) {
            if (target instanceof ShardQueenEntity || target instanceof QueenCloneEntity || !target.isAlive()) {
                continue;
            }
            Vec3 toCenter = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            double distance = toCenter.length();
            if (distance > radius) {
                continue;
            }
            if (distance > 0.3) {
                target.setDeltaMovement(target.getDeltaMovement().add(toCenter.normalize().scale(0.04)));
                target.hurtMarked = true;
            }
            if (this.tickCount % 20 == 0) {
                target.hurt(ModDamageTypes.source(this.level(), ModDamageTypes.VOID_ZONE, this, source), this.damage);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), source);
            }
        }
    }

    private void spawnParticles(float radius) {
        int ring = Mth.ceil(radius * 6.0F);
        for (int i = 0; i < ring; i++) {
            if (this.random.nextInt(3) != 0) {
                continue;
            }
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            this.level().addParticle(ModParticles.VOID_MOTE.get(), x, this.getY() + 0.1, z, 0.0, 0.03, 0.0);
        }
        for (int i = 0; i < 3; i++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double r = Math.sqrt(this.random.nextDouble()) * radius;
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getX() + Math.cos(angle) * r, this.getY() + 0.1,
                    this.getZ() + Math.sin(angle) * r, 0.0, 0.05, 0.0);
        }
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.damage = tag.getFloat("Damage");
        this.tickCount = tag.getInt("Age");
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.damage);
        tag.putInt("Age", this.tickCount);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    /** Wird von der Koenigin beim Tod aufgerufen. */
    public static void clearAround(Entity center, double radius) {
        center.level().getEntitiesOfClass(VoidZoneEntity.class, center.getBoundingBox().inflate(radius)).forEach(Entity::discard);
    }
}
