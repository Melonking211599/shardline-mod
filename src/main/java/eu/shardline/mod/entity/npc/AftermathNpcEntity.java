package eu.shardline.mod.entity.npc;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.story.dialogue.DialogueNpc;
import eu.shardline.mod.story.dialogue.DialogueSessions;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Gemeinsame Basis der NPCs von "Nach dem Ausbruch" (Rhea, Veyl, Ilsa, Kesh).
 *
 * <ul>
 *   <li>Rechtsklick startet den Dialog {@code shardline:<npc>} ({@link DialogueSessions}); der NPC bleibt stehen und schaut
 *   den Spieler an, solange jemand mit ihm spricht (Animation "talk").</li>
 *   <li>Heimatpunkt (Spawnort bzw. Lagerplatz): Im Umkreis von {@code aftermath.campRadius} ist der NPC unverwundbar,
 *   ausserhalb flieht er vor Monstern und bei Schaden.</li>
 *   <li>Toedlicher Schaden ausserhalb des Lagers laesst ihn zusammenbrechen statt sterben (Leben 1, 10 s unverwundbar,
 *   Regeneration), damit die Geschichte nicht abreisst. Nur Schaden, der Unverwundbarkeit umgeht (/kill, Leere), toetet.</li>
 *   <li>Nachts geht er zu seinem Heimatpunkt und schlaeft (Animation "sleep"); ansprechbar ist er dann nur schleichend.</li>
 * </ul>
 * Die KI nutzt nur Vanilla-Ziele mit kleinem Suchradius; der Gespraechszustand wird alle 10 Ticks abgefragt.
 */
public abstract class AftermathNpcEntity extends PathfinderMob implements GeoEntity, DialogueNpc {
    private static final EntityDataAccessor<Boolean> TALKING = SynchedEntityData.defineId(AftermathNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(AftermathNpcEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final RawAnimation TALK = RawAnimation.begin().thenLoop("talk");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("sleep");
    protected static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    protected static final RawAnimation HURT = RawAnimation.begin().thenPlay("hurt");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private BlockPos home;
    private int protectedUntil;
    private int lastHurtTick = -1000;

    protected AftermathNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** Dialog dieses NPCs: data/shardline/dialogues/&lt;npc&gt;.json */
    public ResourceLocation dialogueId() {
        return Shardline.id(this.npcId());
    }

    // ------------------------------------------------------------------ Zustand

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TALKING, false);
        builder.define(SLEEPING, false);
    }

    public boolean isTalking() {
        return this.entityData.get(TALKING);
    }

    public boolean isResting() {
        return this.entityData.get(SLEEPING);
    }

    protected void setResting(boolean resting) {
        this.entityData.set(SLEEPING, resting);
    }

    @Nullable
    public BlockPos home() {
        return this.home;
    }

    public void setHome(BlockPos pos) {
        this.home = pos.immutable();
        this.restrictTo(this.home, 14);
    }

    /** Im Lager (Umkreis um den Heimatpunkt). */
    public boolean inCamp() {
        int radius = ShardlineConfig.NPC_CAMP_RADIUS.get();
        return this.home != null && this.home.distSqr(this.blockPosition()) <= (double) radius * radius;
    }

    // ------------------------------------------------------------------ KI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ConversationGoal());
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.75));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 10.0F, 0.6, 0.8, this::fleesFrom));
        this.goalSelector.addGoal(3, new RestGoal());
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** Vor wem der NPC flieht (Standard: jedes Monster). */
    protected boolean fleesFrom(net.minecraft.world.entity.LivingEntity entity) {
        return !this.inCamp() || this.tickCount - this.lastHurtTick < 200;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.tickCount % 10 == 0) {
            this.entityData.set(TALKING, DialogueSessions.isTalking(this));
        }
    }

    /** Bleibt stehen und schaut den naechsten Gespraechspartner an. */
    private final class ConversationGoal extends Goal {
        ConversationGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return AftermathNpcEntity.this.isTalking();
        }

        @Override
        public void start() {
            AftermathNpcEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            AftermathNpcEntity npc = AftermathNpcEntity.this;
            for (UUID uuid : DialogueSessions.listeners(npc)) {
                Player player = npc.level().getPlayerByUUID(uuid);
                if (player != null) {
                    npc.getLookControl().setLookAt(player, 30.0F, 30.0F);
                    break;
                }
            }
        }
    }

    /** Nachts zum Heimatpunkt gehen und dort schlafen. */
    private final class RestGoal extends Goal {
        RestGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        private boolean wantsRest() {
            AftermathNpcEntity npc = AftermathNpcEntity.this;
            return ShardlineConfig.NPC_SLEEP.get() && npc.home != null && npc.level().isNight() && !npc.isTalking()
                    && npc.tickCount - npc.lastHurtTick > 400 && npc.canRest();
        }

        @Override
        public boolean canUse() {
            return this.wantsRest();
        }

        @Override
        public boolean canContinueToUse() {
            return this.wantsRest();
        }

        @Override
        public void tick() {
            AftermathNpcEntity npc = AftermathNpcEntity.this;
            if (npc.home.distToCenterSqr(npc.position()) <= 2.5) {
                npc.getNavigation().stop();
                npc.setResting(true);
            } else if (npc.tickCount % 20 == 0) {
                npc.setResting(false);
                npc.getNavigation().moveTo(npc.home.getX() + 0.5, npc.home.getY(), npc.home.getZ() + 0.5, 0.45);
            }
        }

        @Override
        public void stop() {
            AftermathNpcEntity.this.setResting(false);
        }
    }

    /** Ob dieser NPC nachts schlaeft (Geister nicht). */
    protected boolean canRest() {
        return true;
    }

    // ------------------------------------------------------------------ Interaktion

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (this.isResting()) {
                if (!player.isShiftKeyDown()) {
                    player.displayClientMessage(Component.translatable("npc.shardline.sleeping", this.getDisplayName()).withStyle(ChatFormatting.GRAY), true);
                    return InteractionResult.CONSUME;
                }
                this.setResting(false);
                this.lastHurtTick = this.tickCount - 300;     // wach fuer eine Weile
            }
            if (!DialogueSessions.start(serverPlayer, this, this.dialogueId())) {
                player.displayClientMessage(Component.translatable("npc.shardline.silent", this.getDisplayName()).withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ------------------------------------------------------------------ Schaden

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean bypass = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (!this.level().isClientSide && !bypass && (this.inCamp() || this.tickCount < this.protectedUntil)) {
            return false;
        }
        if (!this.level().isClientSide && !bypass && amount >= this.getHealth()) {
            amount = Math.max(0.0F, this.getHealth() - 1.0F);       // zusammenbrechen statt sterben
            this.protectedUntil = this.tickCount + 200;
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            if (this.home != null) {
                this.getNavigation().moveTo(this.home.getX() + 0.5, this.home.getY(), this.home.getZ() + 0.5, 0.8);
            }
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.lastHurtTick = this.tickCount;
            this.setResting(false);
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "hurt");
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Spawn und Speichern

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnData) {
        if (this.home == null) {
            this.setHome(this.blockPosition());
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(this.home));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NbtUtils.readBlockPos(tag, "Home").ifPresent(this::setHome);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 480;
    }

    // ------------------------------------------------------------------ Animationen

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            if (this.isResting()) {
                return state.setAndContinue(SLEEP);
            }
            if (this.isTalking()) {
                return state.setAndContinue(TALK);
            }
            return state.setAndContinue(state.isMoving() ? ShardlineAnimations.WALK : ShardlineAnimations.IDLE);
        }));
        controllers.add(new AnimationController<>(this, ShardlineAnimations.ACTION_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim("hurt", HURT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Fuer Tests und Strukturen: Heimatpunkt setzen und sofort am Platz bleiben. */
    public void placeAtHome(ServerLevel level, BlockPos pos) {
        this.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.getYRot(), 0.0F);
        this.setHome(pos);
    }
}
