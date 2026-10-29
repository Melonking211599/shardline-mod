package eu.shardline.mod.entity.infected;

import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusStrain;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Kristallisierter Enderman (Leerenfaeule): ein vom End-Stamm befallener Enderman. Greift Spieler ohne Blickkontakt an,
 * springt durch den Raum, wenn sein Ziel weit weg ist, und hinterlaesst bei Treffern Dunkelheit und Splitterfieber.
 * Wie die anderen Befallenen zaehmbar durch Splitterherrscher; die Reinigungsessenz macht ihn wieder zum Enderman.
 */
public class CrystallizedEndermanEntity extends EnderMan implements InfectedMob {
    private static final EntityDataAccessor<Boolean> DATA_COLLAR = SynchedEntityData.defineId(CrystallizedEndermanEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID owner;
    private int summonTicks;

    public CrystallizedEndermanEntity(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 44.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new InfectedMobs.FollowOwnerGoal(this, 1.1));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new InfectedMobs.DefendOwnerGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
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
            VirusEffects.giveFever(living, 6 * 20);
            VirusEffects.applyStrain(living, VirusStrain.VOID);
        }
        return hit;
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
            LivingEntity target = this.getTarget();
            // Weit entferntes Ziel: alle paar Sekunden ein Sprung durch den Raum (wie ein gereizter Enderman).
            if (target != null && this.tickCount % 20 == 0 && this.distanceToSqr(target) > 256.0 && this.random.nextInt(3) == 0) {
                this.teleport();
            }
        } else if (this.random.nextInt(5) == 0) {
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.02, 0.0);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.owner == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTALLIZED_ENDERMAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTALLIZED_ENDERMAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTALLIZED_ENDERMAN_DEATH.get();
    }

    // --- InfectedMob ---

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLLAR, false);
    }

    @Override
    public boolean hasCollar() {
        return this.entityData.get(DATA_COLLAR);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.owner;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID owner) {
        this.owner = owner;
        this.entityData.set(DATA_COLLAR, owner != null);
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
