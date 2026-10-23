package eu.shardline.mod.entity.misc;

import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Kristallsaeule des Kristallkolosses: kuendigt sich mit Funken am Boden an, schiesst dann aus dem
 * Boden und schleudert alles darauf in die Luft.
 */
public class CrystalPillarEntity extends Entity implements TraceableEntity, GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_WARMUP = SynchedEntityData.defineId(CrystalPillarEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation WARMUP = RawAnimation.begin().thenLoop("warmup");
    private static final RawAnimation RISE = RawAnimation.begin().thenPlayAndHold("rise");
    private static final int LINGER = 30;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private float damage = 10.0F;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;
    private boolean struck;

    public CrystalPillarEntity(EntityType<? extends CrystalPillarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static CrystalPillarEntity create(Level level, double x, double y, double z, int warmup, float damage, LivingEntity owner) {
        CrystalPillarEntity pillar = new CrystalPillarEntity(ModEntities.CRYSTAL_PILLAR.get(), level);
        pillar.setPos(x, y, z);
        pillar.entityData.set(DATA_WARMUP, warmup);
        pillar.damage = damage;
        pillar.setOwner(owner);
        return pillar;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_WARMUP, 10);
    }

    public int getWarmup() {
        return this.entityData.get(DATA_WARMUP);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUUID = owner == null ? null : owner.getUUID();
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
        int warmup = this.getWarmup();
        if (this.level().isClientSide) {
            if (this.tickCount < warmup) {
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(ModParticles.SHARD_SPARKLE.get(), this.getRandomX(0.6), this.getY() + 0.1,
                            this.getRandomZ(0.6), 0.0, 0.08, 0.0);
                }
            }
            return;
        }
        if (this.tickCount == warmup && !this.struck) {
            this.strike();
        }
        if (this.tickCount > warmup + LINGER) {
            this.discard();
        }
    }

    private void strike() {
        this.struck = true;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.COLOSSUS_PILLAR.get(), SoundSource.HOSTILE, 1.2F,
                0.8F + this.random.nextFloat() * 0.4F);
        LivingEntity source = this.getOwner();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.3))) {
            if (!target.isAlive() || target == source || target instanceof Enemy) {
                continue;
            }
            if (target.hurt(ModDamageTypes.source(this.level(), ModDamageTypes.CRYSTAL_PILLAR, this, source), this.damage)) {
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.7, 0.0));
                target.hurtMarked = true;
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_WARMUP, tag.getInt("Warmup"));
        this.damage = tag.getFloat("Damage");
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Warmup", this.getWarmup());
        tag.putFloat("Damage", this.damage);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0,
                state -> state.setAndContinue(this.tickCount < this.getWarmup() ? WARMUP : RISE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
