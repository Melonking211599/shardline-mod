package eu.shardline.mod.entity.boss;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.entity.misc.CrystalPillarEntity;
import eu.shardline.mod.entity.mob.ShardCreeperEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Boss 1 - Der Kristallkoloss (Mid-Game).
 * <ul>
 *     <li>Phase 1: Nahkampf, Bodenstampfer (Flaechenschaden), Kristallsaeulen gegen entfernte Ziele.</li>
 *     <li>Phase 2 (&lt; 50 % HP): ruft Splitterkriecher, wird schneller, alle Abklingzeiten sinken.</li>
 * </ul>
 * Bleibt an seine Arena (den Altar) gebunden und regeneriert, wenn niemand kaempft.
 */
public class CrystalColossusEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(CrystalColossusEntity.class, EntityDataSerializers.INT);
    private static final ResourceLocation ENRAGE_SPEED_ID = Shardline.id("colossus_enrage_speed");
    private static final double ARENA_RADIUS = 28.0;
    private static final double LEASH_RADIUS = 40.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.shardline.crystal_colossus"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    @Nullable
    private BlockPos home;
    private int awakenTicks;
    private int idleTicks;

    public CrystalColossusEntity(EntityType<? extends CrystalColossusEntity> type, Level level) {
        super(type, level);
        this.xpReward = 200;
        this.setPersistenceRequired();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ShardlineConfig.COLOSSUS_HEALTH.get());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ShardlineConfig.COLOSSUS_ATTACK_DAMAGE.get());
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ColossusAttackGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    public void setHome(BlockPos home) {
        this.home = home.immutable();
        this.restrictTo(this.home, (int) ARENA_RADIUS);
    }

    /** Erwachen nach der Beschwoerung: kurze Pause mit Brull-Animation. */
    public void playAwakening() {
        this.awakenTicks = 40;
    }

    public boolean isAwakening() {
        return this.awakenTicks > 0;
    }

    // --- Boss-Logik ---

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();

        if (this.awakenTicks > 0) {
            if (this.awakenTicks == 38) {
                this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "roar");
            }
            this.awakenTicks--;
            this.getNavigation().stop();
        }

        if (this.getPhase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.enterPhaseTwo(level);
        }

        // An die Arena gebunden.
        if (this.home != null) {
            if (this.distanceToSqr(Vec3.atBottomCenterOf(this.home)) > LEASH_RADIUS * LEASH_RADIUS) {
                this.teleportTo(this.home.getX() + 0.5, this.home.getY() + 1.0, this.home.getZ() + 4.5);
                this.getNavigation().stop();
            }
            LivingEntity target = this.getTarget();
            if (target != null && target.distanceToSqr(Vec3.atCenterOf(this.home)) > (LEASH_RADIUS + 8.0) * (LEASH_RADIUS + 8.0)) {
                this.setTarget(null);
            }
        }

        // Regeneration ohne Kampf.
        if (this.getTarget() == null) {
            if (++this.idleTicks > 200 && this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(4.0F);
            }
        } else {
            this.idleTicks = 0;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void enterPhaseTwo(ServerLevel level) {
        this.entityData.set(DATA_PHASE, 2);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(ENRAGE_SPEED_ID)) {
            speed.addPermanentModifier(new AttributeModifier(ENRAGE_SPEED_ID, 0.35, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
        this.bossEvent.setName(Component.translatable("entity.shardline.crystal_colossus.enraged"));
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "roar");
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 3.0F, 0.7F);
        level.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY(0.5), this.getZ(), 120, 1.5, 2.0, 1.5, 0.3);
        for (ServerPlayer player : this.bossEvent.getPlayers()) {
            player.displayClientMessage(Component.translatable("message.shardline.colossus_enraged"), true);
        }
    }

    /** Bodenstampfer: Flaechenschaden mit Rueckstoss nach oben. */
    void performSlam() {
        ServerLevel level = (ServerLevel) this.level();
        float damage = ShardlineConfig.COLOSSUS_SLAM_DAMAGE.get().floatValue();
        double radius = this.getPhase() == 2 ? 7.0 : 6.0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius, 2.0, radius))) {
            if (target == this || target instanceof ShardCreeperEntity || !target.isAlive()) {
                continue;
            }
            double distance = target.distanceTo(this);
            if (distance > radius) {
                continue;
            }
            if (target.hurt(this.damageSources().mobAttack(this), damage * (float) (1.0 - distance / (radius * 2.0)))) {
                Vec3 push = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                if (push.lengthSqr() > 1.0E-4) {
                    push = push.normalize();
                    target.knockback(1.6, -push.x, -push.z);
                }
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.6, 0.0));
                target.hurtMarked = true;
            }
        }
        for (int i = 0; i < 48; i++) {
            double angle = i * Mth.TWO_PI / 48.0;
            for (double r = 1.5; r <= radius; r += 1.5) {
                level.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX() + Math.cos(angle) * r, this.getY() + 0.2,
                        this.getZ() + Math.sin(angle) * r, 1, 0.0, 0.1, 0.0, 0.02);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.COLOSSUS_SLAM.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
    }

    /** Kristallsaeulen: eine Welle Richtung Ziel, in Phase 2 zusaetzlich ein Ring um das Ziel. */
    void performPillars(LivingEntity target) {
        ServerLevel level = (ServerLevel) this.level();
        float damage = ShardlineConfig.COLOSSUS_PILLAR_DAMAGE.get().floatValue();
        Vec3 start = this.position();
        Vec3 direction = target.position().subtract(start).multiply(1.0, 0.0, 1.0);
        double length = direction.length();
        if (length < 1.0E-3) {
            return;
        }
        direction = direction.normalize();
        int count = Mth.clamp((int) (length / 1.8) + 2, 4, 14);
        for (int i = 1; i <= count; i++) {
            Vec3 pos = start.add(direction.scale(i * 1.8));
            this.spawnPillar(level, pos, 6 + i * 2, damage);
        }
        if (this.getPhase() == 2) {
            for (int i = 0; i < 6; i++) {
                double angle = i * Mth.TWO_PI / 6.0;
                this.spawnPillar(level, target.position().add(Math.cos(angle) * 2.5, 0.0, Math.sin(angle) * 2.5), 18, damage);
            }
        }
    }

    private void spawnPillar(ServerLevel level, Vec3 pos, int warmup, float damage) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(pos.x, pos.y + 2.0, pos.z).mutable();
        // Boden unter der Position suchen.
        for (int i = 0; i < 8; i++) {
            BlockState below = level.getBlockState(cursor.below());
            if (below.isFaceSturdy(level, cursor.below(), net.minecraft.core.Direction.UP) && level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                level.addFreshEntity(CrystalPillarEntity.create(level, pos.x, cursor.getY(), pos.z, warmup, damage, this));
                return;
            }
            cursor.move(net.minecraft.core.Direction.DOWN);
        }
    }

    /** Phase 2: ruft Splitterkriecher herbei. */
    void summonMinions(LivingEntity target) {
        ServerLevel level = (ServerLevel) this.level();
        int count = ShardlineConfig.COLOSSUS_MINION_COUNT.get();
        for (int i = 0; i < count; i++) {
            ShardCreeperEntity creeper = ModEntities.SHARD_CREEPER.get().create(level);
            if (creeper == null) {
                continue;
            }
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double x = this.getX() + Math.cos(angle) * 3.5;
            double z = this.getZ() + Math.sin(angle) * 3.5;
            creeper.moveTo(x, this.getY() + 0.5, z, this.random.nextFloat() * 360.0F, 0.0F);
            creeper.finalizeSpawn(level, level.getCurrentDifficultyAt(creeper.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            creeper.setTarget(target);
            level.addFreshEntity(creeper);
            level.sendParticles(ModParticles.SHARD_SPARKLE.get(), x, this.getY() + 1.0, z, 20, 0.3, 0.6, 0.3, 0.1);
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.COLOSSUS_SUMMON.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Immun gegen die eigenen Diener und gegen Splitter/Explosionen ohne Spieler als Verursacher.
        boolean fromPlayer = source.getEntity() instanceof Player;
        if (!fromPlayer && (source.is(ModDamageTypes.SHARD) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(ModDamageTypes.CRYSTAL_PILLAR))) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FALL) || this.isAwakening() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "attack");
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.35, 0.0));
        }
        return hurt;
    }

    // --- Bossleiste ---

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    // --- Diverses ---

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", this.getPhase());
        if (this.home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(this.home));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_PHASE, Math.max(1, tag.getInt("Phase")));
        NbtUtils.readBlockPos(tag, "Home").ifPresent(this::setHome);
        if (this.getPhase() == 2) {
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            this.bossEvent.setName(Component.translatable("entity.shardline.crystal_colossus.enraged"));
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.COLOSSUS_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.COLOSSUS_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.COLOSSUS_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.walkIdle(this));
        controllers.add(ShardlineAnimations.actions(this, "attack", "slam", "pillar", "summon", "roar"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Kampf-KI des Kolosses. Waehlt je nach Abstand und Phase zwischen Nahkampf, Bodenstampfer,
     * Kristallsaeulen und Beschwoerung. Faehigkeiten haben eine Ausholzeit fuer die Animation.
     */
    static class ColossusAttackGoal extends Goal {
        private enum Action { NONE, SLAM, PILLARS, SUMMON }

        private final CrystalColossusEntity colossus;
        private Action action = Action.NONE;
        private int actionTicks;
        private int attackCooldown;
        private int slamCooldown = 60;
        private int pillarCooldown = 80;
        private int summonCooldown = 40;

        ColossusAttackGoal(CrystalColossusEntity colossus) {
            this.colossus = colossus;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.colossus.getTarget();
            return target != null && target.isAlive() && !this.colossus.isAwakening();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || this.action != Action.NONE;
        }

        @Override
        public void stop() {
            this.action = Action.NONE;
            this.colossus.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.colossus.getTarget();
            boolean enraged = this.colossus.getPhase() == 2;
            this.attackCooldown = Math.max(0, this.attackCooldown - 1);
            this.slamCooldown = Math.max(0, this.slamCooldown - 1);
            this.pillarCooldown = Math.max(0, this.pillarCooldown - 1);
            this.summonCooldown = Math.max(0, this.summonCooldown - 1);

            if (this.action != Action.NONE) {
                this.tickAction(target, enraged);
                return;
            }
            if (target == null) {
                return;
            }

            this.colossus.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceSqr = this.colossus.distanceToSqr(target);

            if (enraged && this.summonCooldown == 0) {
                this.begin(Action.SUMMON, "summon");
            } else if (this.slamCooldown == 0 && distanceSqr < 30.0) {
                this.begin(Action.SLAM, "slam");
            } else if (this.pillarCooldown == 0 && distanceSqr > 36.0 && distanceSqr < 576.0) {
                this.begin(Action.PILLARS, "pillar");
            } else {
                this.colossus.getNavigation().moveTo(target, 1.0);
                double reach = this.colossus.getBbWidth() * 1.4 + target.getBbWidth();
                if (distanceSqr <= reach * reach && this.attackCooldown == 0) {
                    this.colossus.doHurtTarget(target);
                    this.attackCooldown = enraged ? 16 : 24;
                }
            }
        }

        private void begin(Action action, String animation) {
            this.action = action;
            this.actionTicks = 0;
            this.colossus.getNavigation().stop();
            this.colossus.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, animation);
        }

        private void tickAction(@Nullable LivingEntity target, boolean enraged) {
            this.actionTicks++;
            if (target != null) {
                this.colossus.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            switch (this.action) {
                case SLAM -> {
                    if (this.actionTicks == 14) {
                        this.colossus.performSlam();
                    }
                    if (this.actionTicks >= 24) {
                        this.finish();
                        this.slamCooldown = enraged ? 90 : 160;
                    }
                }
                case PILLARS -> {
                    if (this.actionTicks == 12 && target != null) {
                        this.colossus.performPillars(target);
                    }
                    if (this.actionTicks >= 24) {
                        this.finish();
                        this.pillarCooldown = enraged ? 120 : 200;
                    }
                }
                case SUMMON -> {
                    if (this.actionTicks == 20 && target != null) {
                        this.colossus.summonMinions(target);
                    }
                    if (this.actionTicks >= 32) {
                        this.finish();
                        this.summonCooldown = 400;
                    }
                }
                default -> this.finish();
            }
        }

        private void finish() {
            this.action = Action.NONE;
            this.actionTicks = 0;
        }
    }
}
