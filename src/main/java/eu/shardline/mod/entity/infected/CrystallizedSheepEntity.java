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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Kristallisiertes Schaf: Statt Wolle traegt es ein Vlies aus Viruskristallen. Scheren liefert
 * Amethyst und manchmal einen Shard-Splitter; das Vlies waechst wie Wolle nach. Ein Traeger wie
 * die Kristall-Kuh: streut Infektionsstellen, Nahkampftreffer koennen Fieber ausloesen.
 */
public class CrystallizedSheepEntity extends Sheep implements InfectedMob {
    private static final EntityDataAccessor<Boolean> DATA_COLLAR = SynchedEntityData.defineId(CrystallizedSheepEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID owner;
    private int summonTicks;

    public CrystallizedSheepEntity(EntityType<? extends CrystallizedSheepEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Sheep.createAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    /** Vanilla-Ziele bleiben (das Grasfressen laesst das Vlies nachwachsen); dazu Besitzer und Splitter-Lockung. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new InfectedMobs.FollowOwnerGoal(this, 1.2));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, stack -> stack.is(ModItems.SHARD_FRAGMENT.get()), false));
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

    /** Scheren: Kristallvlies statt Wolle. */
    @Override
    public void shear(SoundSource category) {
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, category, 1.0F, 0.8F);
        this.level().playSound(null, this, SoundEvents.AMETHYST_CLUSTER_BREAK, category, 0.8F, 1.2F);
        this.setSheared(true);
        int shards = 1 + this.random.nextInt(2);
        for (int i = 0; i < shards; i++) {
            this.dropShorn(new ItemStack(Items.AMETHYST_SHARD));
        }
        if (this.random.nextFloat() < 0.35F) {
            this.dropShorn(new ItemStack(ModItems.SHARD_FRAGMENT.get()));
        }
    }

    private void dropShorn(ItemStack stack) {
        ItemEntity item = this.spawnAtLocation(stack, 1);
        if (item != null) {
            item.setDeltaMovement(item.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                    this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
        }
    }

    /** Eine Beute-Tabelle fuer alle Farben (Vanilla waehlt sie nach der Wollfarbe). */
    @Override
    public ResourceKey<LootTable> getDefaultLootTable() {
        return this.getType().getDefaultLootTable();
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
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CrystallizedSheepEntity lamb = ModEntities.CRYSTALLIZED_SHEEP.get().create(level);
        if (lamb != null) {
            lamb.setOwnerUUID(this.owner);
        }
        return lamb;
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
        return ModSounds.CRYSTALLIZED_SHEEP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTALLIZED_SHEEP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTALLIZED_SHEEP_DEATH.get();
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
