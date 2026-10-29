package eu.shardline.mod.entity.infected;

import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.virus.VirusEffects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Kristallisierter Dorfbewohner: entsteht, wenn ein Dorfbewohner auf infiziertem Boden steht. Er
 * handelt nicht mehr, wandert ziellos und murmelt Bruchstuecke des falschen Tons. Seine alten Daten
 * (Beruf, Stufe, Angebote, Ruf) bleiben gespeichert: Eine Reinigungsessenz macht ihn wieder zum
 * Dorfbewohner, der er war. Splitterherrscher koennen ihn zaehmen wie die Kristall-Kuh.
 */
public class CrystallizedVillagerEntity extends AgeableMob implements InfectedMob {
    private static final EntityDataAccessor<Boolean> DATA_COLLAR = SynchedEntityData.defineId(CrystallizedVillagerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int MURMUR_LINES = 4;

    @Nullable
    private UUID owner;
    private int summonTicks;
    /** Speicherdaten des urspruenglichen Dorfbewohners (null bei Spawn-Ei oder Befehl). */
    @Nullable
    private CompoundTag originalVillager;
    private int murmurCooldown;

    public CrystallizedVillagerEntity(EntityType<? extends CrystallizedVillagerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    /** Verwandelt einen Dorfbewohner und merkt sich alles, was die Reinigung spaeter zurueckgeben soll. */
    @Nullable
    public static CrystallizedVillagerEntity infect(ServerLevel level, Villager villager) {
        CompoundTag original = new CompoundTag();
        villager.addAdditionalSaveData(original);
        CrystallizedVillagerEntity infected = VirusEffects.convert(level, villager, ModEntities.CRYSTALLIZED_VILLAGER.get());
        if (infected != null) {
            infected.originalVillager = original;
        }
        return infected;
    }

    /** Reinigung: wieder ein Dorfbewohner, mit Beruf, Angeboten und Ruf von vorher. */
    @Nullable
    public Villager restore(ServerLevel level) {
        CompoundTag original = this.originalVillager;
        Villager villager = VirusEffects.convert(level, this, EntityType.VILLAGER);
        if (villager != null && original != null) {
            villager.readAdditionalSaveData(original);
            villager.setHealth(villager.getMaxHealth());
        }
        return villager;
    }

    public boolean remembersVillager() {
        return this.originalVillager != null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(3, new InfectedMobs.FollowOwnerGoal(this, 1.3));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack -> stack.is(ModItems.SHARD_FRAGMENT.get()), false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult tame = InfectedMobs.tryTame(this, player, hand);
        if (tame != null) {
            return tame;
        }
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
            if (!this.level().isClientSide && this.murmurCooldown <= 0) {
                this.murmurCooldown = 60;
                int line = 1 + this.random.nextInt(MURMUR_LINES);
                player.displayClientMessage(Component.translatable("message.shardline.crystallized_villager.murmur." + line)
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), true);
                this.playAmbientSound();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            InfectedMobs.splinterAttacker(this, source, 0.3F);
        }
        return hurt;
    }

    /** Kristallisierte Dorfbewohner vermehren sich nicht. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            if (this.murmurCooldown > 0) {
                this.murmurCooldown--;
            }
            InfectedMobs.serverTick(this, 1.5);
        } else if (this.random.nextInt(5) == 0) {
            this.level().addParticle(ModParticles.VIRUS_SPORE.get(), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.02, 0.0);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTALLIZED_VILLAGER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTALLIZED_VILLAGER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTALLIZED_VILLAGER_DEATH.get();
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
        if (this.originalVillager != null) {
            tag.put("OriginalVillager", this.originalVillager);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        InfectedMobs.load(this, tag);
        this.originalVillager = tag.contains("OriginalVillager") ? tag.getCompound("OriginalVillager") : null;
    }
}
