package eu.shardline.mod.entity.infected;

import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.virus.VirusEffects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Kristallisierte Kuh: friedlicher Traeger des Virus. Hinterlaesst doppelt so oft neue
 * Infektionsstellen wie ein Shard-Zombie und gibt keine Milch mehr. Wer sie im Nahkampf
 * trifft, riskiert Splitterfieber durch absplitternde Kristalle.
 */
public class CrystallizedCowEntity extends Cow implements InfectedMob {
    private static final EntityDataAccessor<Boolean> DATA_COLLAR = SynchedEntityData.defineId(CrystallizedCowEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID owner;
    private int summonTicks;

    public CrystallizedCowEntity(EntityType<? extends CrystallizedCowEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.19);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new InfectedMobs.FollowOwnerGoal(this, 1.2));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack -> stack.is(ModItems.SHARD_FRAGMENT.get()), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.AMETHYST_SHARD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult tame = InfectedMobs.tryTame(this, player, hand);
        if (tame != null) {
            return tame;
        }
        if (player.getItemInHand(hand).is(Items.BUCKET)) {
            // Kristallisierte Kuehe geben keine Milch.
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getDirectEntity() instanceof LivingEntity attacker
                && attacker.distanceToSqr(this) < 9.0 && this.random.nextFloat() < 0.3F) {
            VirusEffects.giveFever(attacker, 5 * 20);
        }
        return hurt;
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CrystallizedCowEntity calf = ModEntities.CRYSTALLIZED_COW.get().create(level);
        if (calf != null) {
            calf.setOwnerUUID(this.owner);
        }
        return calf;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            InfectedMobs.serverTick(this, 2.0);
        } else if (this.random.nextInt(5) == 0) {
            this.level().addParticle(ModParticles.VIRUS_SPORE.get(), this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.02, 0.0);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTALLIZED_COW_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTALLIZED_COW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTALLIZED_COW_DEATH.get();
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
