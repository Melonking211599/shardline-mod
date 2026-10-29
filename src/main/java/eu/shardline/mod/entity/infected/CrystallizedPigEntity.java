package eu.shardline.mod.entity.infected;

import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Kristallisiertes Schwein: Ruecken voller Viruskristalle, frisst Amethyst. Laesst sich wie ein
 * Schwein satteln und reiten. Traeger wie die Kristall-Kuh, Blitze verwandeln es nicht.
 */
public class CrystallizedPigEntity extends Pig implements InfectedMob {
    private static final EntityDataAccessor<Boolean> DATA_COLLAR = SynchedEntityData.defineId(CrystallizedPigEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID owner;
    private int summonTicks;

    public CrystallizedPigEntity(EntityType<? extends CrystallizedPigEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pig.createAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new InfectedMobs.FollowOwnerGoal(this, 1.2));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack -> stack.is(ModItems.SHARD_FRAGMENT.get()), false));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.AMETHYST_SHARD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult tame = InfectedMobs.tryTame(this, player, hand);
        return tame != null ? tame : super.mobInteract(player, hand);
    }

    /** Der Kristall leitet den Blitz ab: kein Zombie-Piglin. */
    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            InfectedMobs.splinterAttacker(this, source, 0.25F);
        }
        return hurt;
    }

    @Nullable
    @Override
    public Pig getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CrystallizedPigEntity piglet = ModEntities.CRYSTALLIZED_PIG.get().create(level);
        if (piglet != null) {
            piglet.setOwnerUUID(this.owner);
        }
        return piglet;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            InfectedMobs.serverTick(this, 1.5);
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
        return ModSounds.CRYSTALLIZED_PIG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTALLIZED_PIG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTALLIZED_PIG_DEATH.get();
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
