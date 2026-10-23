package eu.shardline.mod.entity.infected;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.virus.VirusEffects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

/**
 * Shard-Zombie: ein vom Virus kristallisierter Zombie. Brennt nicht in der Sonne, steckt mit
 * Splitterfieber an, verwandelt getoetete Dorfbewohner und hinterlaesst neue Infektionsstellen.
 * Splitterherrscher koennen ihn zaehmen.
 */
public class ShardZombieEntity extends Zombie implements InfectedMob {
    @Nullable
    private UUID owner;
    private int summonTicks;

    public ShardZombieEntity(EntityType<? extends ShardZombieEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new InfectedMobs.FollowOwnerGoal(this, 1.1));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new InfectedMobs.DefendOwnerGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> InfectedMobs.canHunt(this, target)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, 10, false, false,
                target -> InfectedMobs.canHunt(this, target)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, 10, true, false,
                target -> InfectedMobs.canHunt(this, target)));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !InfectedMobs.isFriendly(this, target) && super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && InfectedMobs.isFriendly(this, target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            VirusEffects.giveFever(living, 8 * 20);
        }
        return hit;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        if (entity instanceof Villager villager) {
            if (ShardlineConfig.VILLAGERS_TRANSFORM.get() && this.random.nextFloat() < conversionChance(level.getDifficulty())) {
                ShardZombieEntity converted = (ShardZombieEntity) VirusEffects.convert(level, villager, ModEntities.SHARD_ZOMBIE.get());
                if (converted != null) {
                    converted.setOwnerUUID(this.owner);
                    return false;
                }
            }
            return true;
        }
        return super.killedEntity(level, entity);
    }

    private static float conversionChance(Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> 1.0F;
            case NORMAL -> 0.5F;
            case EASY -> 0.25F;
            default -> 0.0F;
        };
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult tame = InfectedMobs.tryTame(this, player, hand);
        return tame != null ? tame : super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            InfectedMobs.serverTick(this, 1.0);
        } else if (this.random.nextInt(6) == 0) {
            this.level().addParticle(ModParticles.VIRUS_SPORE.get(), this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.owner == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHARD_ZOMBIE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHARD_ZOMBIE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHARD_ZOMBIE_DEATH.get();
    }

    // --- InfectedMob ---

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.owner;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID owner) {
        this.owner = owner;
        if (owner != null) {
            this.setPersistenceRequired();
        }
    }

    @Override
    public int getSummonTicks() {
        return this.summonTicks;
    }

    @Override
    public void setSummonTicks(int ticks) {
        this.summonTicks = ticks;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        InfectedMobs.save(this, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        InfectedMobs.load(this, tag);
    }
}
