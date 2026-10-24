package eu.shardline.mod.entity.npc;

import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.quest.Quests;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/**
 * Kesh, der Gezeichnete: ein Ueberlebender mit Kristallen in der Haut, der "den Ton" hoert. Folgt dem Spieler auf
 * Zuruf (Dialogaktion {@code npc_state: follow}) und meldet beim Eintreffen bei Rhea den Auftragsschritt "escort"
 * mit dem Ziel {@code shardline:kesh}. Nach der Heilung ({@code npc_state: healed}) traegt er die Textur kesh_healed.
 * Folgen endet, wenn der Spieler sich mehr als {@link #LEASH} Bloecke entfernt oder den Ort wechselt; Kesh wartet dann.
 */
public class KeshEntity extends AftermathNpcEntity {
    public static final String ESCORT_TARGET = "shardline:kesh";
    private static final EntityDataAccessor<Boolean> HEALED = SynchedEntityData.defineId(KeshEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double LEASH = 24.0;
    /** Abstand zu Rhea, ab dem Kesh als "im Lager angekommen" gilt. */
    private static final double ARRIVAL = 8.0;

    @Nullable
    private UUID following;

    public KeshEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public String npcId() {
        return "kesh";
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEALED, false);
    }

    public boolean isHealed() {
        return this.entityData.get(HEALED);
    }

    public void setHealed(boolean healed) {
        this.entityData.set(HEALED, healed);
    }

    @Nullable
    public UUID following() {
        return this.following;
    }

    @Override
    public void applyState(String state, ServerPlayer player) {
        switch (state) {
            case "follow" -> {
                this.following = player.getUUID();
                this.clearRestriction();
            }
            case "stay" -> this.stay();
            case "healed" -> {
                if (!this.isHealed() && this.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0, this.getZ(), 24, 0.4, 0.6, 0.4, 0.02);
                    this.playSound(ModSounds.INFECTED_CURED.get(), 1.0F, 1.1F);
                }
                this.setHealed(true);
            }
            default -> {
            }
        }
    }

    /** Hoert auf zu folgen und macht den jetzigen Ort zum Heimatpunkt. */
    public void stay() {
        this.following = null;
        this.getNavigation().stop();
        this.setHome(this.blockPosition());
    }

    @Override
    protected void registerMovementGoals() {
        this.goalSelector.addGoal(2, new FollowGoal());
        super.registerMovementGoals();
    }

    @Override
    protected boolean canRest() {
        return this.following == null;
    }

    @Nullable
    private ServerPlayer followedPlayer() {
        if (this.following == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        return level.getPlayerByUUID(this.following) instanceof ServerPlayer player && player.level() == level ? player : null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.following != null && this.tickCount % 20 == 0) {
            this.checkArrival();
        }
    }

    /** Bei Rhea angekommen: Auftragsschritt "escort" fuer den gefolgten Spieler, Kesh bleibt im Lager. */
    public void checkArrival() {
        ServerPlayer player = this.followedPlayer();
        if (player == null) {
            return;
        }
        List<RheaEntity> rhea = this.level().getEntitiesOfClass(RheaEntity.class, this.getBoundingBox().inflate(ARRIVAL));
        if (!rhea.isEmpty()) {
            this.stay();
            if (rhea.get(0).home() != null) {
                this.setHome(rhea.get(0).home().offset(2, 0, 1));
            }
            Quests.trigger(player, "escort", ESCORT_TARGET);
        }
    }

    /** Folgt dem Spieler in 3 Bloecken Abstand; zu weit weg oder in einer anderen Welt: stehen bleiben und warten. */
    private final class FollowGoal extends Goal {
        FollowGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ServerPlayer player = KeshEntity.this.followedPlayer();
            return player != null && !KeshEntity.this.isTalking() && player.distanceToSqr(KeshEntity.this) > 9.0
                    && player.distanceToSqr(KeshEntity.this) < LEASH * LEASH;
        }

        @Override
        public void tick() {
            ServerPlayer player = KeshEntity.this.followedPlayer();
            if (player == null) {
                return;
            }
            KeshEntity.this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            if (KeshEntity.this.tickCount % 10 == 0) {
                KeshEntity.this.getNavigation().moveTo(player, 0.6);
            }
        }

        @Override
        public void stop() {
            KeshEntity.this.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ Speichern und Klaenge

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Healed", this.isHealed());
        if (this.following != null) {
            tag.putUUID("Following", this.following);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setHealed(tag.getBoolean("Healed"));
        this.following = tag.hasUUID("Following") ? tag.getUUID("Following") : null;
        if (this.following != null) {
            this.clearRestriction();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isResting() || this.isTalking() ? null : ModSounds.KESH_VOICE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KESH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.KESH_DEATH.get();
    }
}
