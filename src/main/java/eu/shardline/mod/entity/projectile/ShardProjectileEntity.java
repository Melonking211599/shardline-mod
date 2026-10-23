package eu.shardline.mod.entity.projectile;

import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Splitter-Projektil. Wird vom Shard-Spaeher, vom Splitterkriecher, den Bossen und der
 * Splitterklinge verschossen. Verursacht Schaden vom Typ {@code shardline:shard}.
 */
public class ShardProjectileEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME = 100;
    private float damage = 4.0F;

    public ShardProjectileEntity(EntityType<? extends ShardProjectileEntity> type, Level level) {
        super(type, level);
    }

    public ShardProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SHARD_PROJECTILE.get(), shooter, level);
    }

    /**
     * Erzeugt und verschiesst ein Projektil.
     */
    public static ShardProjectileEntity shoot(Level level, LivingEntity shooter, double x, double y, double z, Vec3 direction,
                                              float speed, float inaccuracy, float damage) {
        ShardProjectileEntity projectile = new ShardProjectileEntity(level, shooter);
        projectile.setPos(x, y, z);
        projectile.setDamage(damage);
        projectile.shoot(direction.x, direction.y, direction.z, speed, inaccuracy);
        level.addFreshEntity(projectile);
        return projectile;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHARD_FRAGMENT.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.tickCount > MAX_LIFETIME) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target)) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner == null) {
            return true;
        }
        if (target == owner || target.isPassengerOfSameVehicle(owner)) {
            return false;
        }
        // Kein Eigenbeschuss unter Monstern.
        if (owner instanceof Enemy && target instanceof Enemy) {
            return false;
        }
        // Spieler treffen ihre eigenen Haustiere nicht.
        return !(owner instanceof Player && target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID()));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        target.hurt(ModDamageTypes.source(this.level(), ModDamageTypes.SHARD, this, owner), this.damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.SHARD_FRAGMENT.get())),
                    this.getX(), this.getY(), this.getZ(), 6, 0.1, 0.1, 0.1, 0.08);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SHARD_SHATTER.get(), SoundSource.HOSTILE,
                    0.6F, 1.2F + this.random.nextFloat() * 0.4F);
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ShardDamage", this.damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ShardDamage")) {
            this.damage = tag.getFloat("ShardDamage");
        }
    }
}
