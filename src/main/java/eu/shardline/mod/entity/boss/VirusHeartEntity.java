package eu.shardline.mod.entity.boss;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusSpreadManager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Virusherz (Mini-Boss): pulsierender Kristallknoten, der in stark infizierten Gebieten entsteht.
 * Bewegt sich nicht. Sein Tod reinigt ein grosses Gebiet. Wer drei Herzen besiegt, erhaelt den Kern des Bruchs.
 *
 * <p>Drei Phasen nach Lebenspunkten (Grenzen 2/3 und 1/3, passend zu den Marken der Bossleiste):</p>
 * <ol>
 *   <li>Fieberwellen und Splittersalven.</li>
 *   <li>Zusaetzlich Tentakelschlaege ({@code attack_tendrils}): Ein Ring aus Sporen markiert eine Sekunde vorher die
 *       Einschlagstelle unter dem Ziel.</li>
 *   <li>Zusaetzlich Beschwoerung ({@code summon_infected}): Sporenwirbel zeigen, wo Infizierte erscheinen; die Wellen
 *       kommen schneller.</li>
 * </ol>
 * Jeder Phasenwechsel wird angekuendigt: Das Herz verhaertet sich kurz (unverwundbar, Clip {@code spawn}), die
 * Bossleiste wechselt die Farbe. Treffer zeigen den Clip {@code hurt}, der Tod {@code death} (bleibt stehen).
 * Schwierigkeit: {@code remedies.virusHeartDifficulty}.
 */
public class VirusHeartEntity extends Monster implements GeoEntity {
    private static final double PULSE_RADIUS = 8.0;
    private static final double TENDRIL_RADIUS = 2.5;
    /** Lebensanteil, ab dem Phase 2 bzw. 3 beginnt. */
    public static final float PHASE_2 = 2.0F / 3.0F;
    public static final float PHASE_3 = 1.0F / 3.0F;
    private static final int TRANSITION_TICKS = 32;
    private static final int TENDRIL_WINDUP = 20;
    private static final int SUMMON_WINDUP = 34;
    private static final int DEATH_TICKS = 44;
    /** Entity-Event "Fieberwelle": die Bossleiste laesst ihre Adern im Takt des Herzschlags aufleuchten. */
    private static final byte EVENT_PULSE = 100;
    private static long lastPulseMillis;
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(VirusHeartEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.shardline.virus_heart"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    private int pulseCooldown = 100;
    private int volleyCooldown = 50;
    private int tendrilCooldown = 60;
    private int summonCooldown = 60;
    private int transitionTicks;
    private int tendrilTicks;
    @Nullable
    private Vec3 tendrilTarget;
    private int summonTicks;
    private final List<BlockPos> summonSpots = new ArrayList<>();
    private int hurtAnimCooldown;
    private boolean announced;

    public VirusHeartEntity(EntityType<? extends VirusHeartEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ShardlineConfig.VIRUS_HEART_HEALTH.get());
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
    }

    /** Aktuelle Phase 1-3 (auch auf dem Client). */
    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    /** Phase, die zu einem Lebensanteil gehoert. */
    public static int phaseFor(float healthFraction) {
        return healthFraction <= PHASE_3 ? 3 : healthFraction <= PHASE_2 ? 2 : 1;
    }

    private static ShardlineConfig.HeartDifficulty difficulty() {
        return ShardlineConfig.VIRUS_HEART_DIFFICULTY.get();
    }

    private int scaled(int ticks) {
        return Math.max(10, (int) Math.round(ticks * difficulty().cooldown()));
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> !VirusEffects.isImmune(target)));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();
        float fraction = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(fraction);
        if (!this.announced) {
            this.announced = true;
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "spawn");
        }
        if (this.hurtAnimCooldown > 0) {
            this.hurtAnimCooldown--;
        }

        // Phasenwechsel: kurz verhaerten (unverwundbar), dann weiter mit neuen Angriffen
        int phase = phaseFor(fraction);
        if (phase > this.getPhase()) {
            this.enterPhase(level, phase);
        }
        if (this.transitionTicks > 0) {
            this.transitionTicks--;
            if (this.transitionTicks % 4 == 0) {
                ringParticles(level, this.position(), 2.5 + (TRANSITION_TICKS - this.transitionTicks) * 0.15, 24, ModParticles.VIRUS_SPORE.get());
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && (!target.isAlive() || this.distanceToSqr(target) > 32.0 * 32.0)) {
            this.setTarget(null);
            target = null;
        }

        this.tickTendrils(level);
        this.tickSummon(level);

        if (--this.pulseCooldown <= 0) {
            this.pulseCooldown = scaled(this.getPhase() == 3 ? 70 : 100) + this.random.nextInt(40);
            this.pulse(level);
        }
        if (target != null && --this.volleyCooldown <= 0 && this.hasLineOfSight(target)) {
            this.volleyCooldown = scaled(40) + this.random.nextInt(30);
            this.volley(level, target);
        }
        if (this.getPhase() >= 2 && target != null && this.tendrilTicks == 0 && --this.tendrilCooldown <= 0) {
            this.tendrilCooldown = scaled(this.getPhase() == 3 ? 70 : 100) + this.random.nextInt(30);
            this.startTendrils(level, target);
        }
        if (this.getPhase() >= 3 && target != null && this.summonTicks == 0 && --this.summonCooldown <= 0) {
            this.summonCooldown = scaled(260) + this.random.nextInt(80);
            this.startSummon(level);
        }
    }

    private void enterPhase(ServerLevel level, int phase) {
        this.entityData.set(DATA_PHASE, phase);
        this.transitionTicks = TRANSITION_TICKS;
        this.tendrilTicks = 0;
        this.summonTicks = 0;
        this.bossEvent.setColor(phase == 3 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PINK);
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "spawn");
        level.playSound(null, this.blockPosition(), ModSounds.VIRUS_HEART_PHASE.get(), SoundSource.HOSTILE, 3.0F, phase == 3 ? 0.7F : 0.9F);
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.2, this.getZ(), 2, 0.4, 0.4, 0.4, 0.0);
        Component text = Component.translatable("message.shardline.virus_heart.phase_" + phase).withStyle(ChatFormatting.LIGHT_PURPLE);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(32.0))) {
            player.displayClientMessage(text, true);
        }
    }

    // --- Phase 2: Tentakel ---

    private void startTendrils(ServerLevel level, LivingEntity target) {
        this.tendrilTarget = target.position();
        this.tendrilTicks = TENDRIL_WINDUP;
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "attack_tendrils");
        level.playSound(null, BlockPos.containing(this.tendrilTarget), ModSounds.VIRUS_HEART_TENDRILS.get(), SoundSource.HOSTILE, 1.6F, 1.0F);
    }

    private void tickTendrils(ServerLevel level) {
        if (this.tendrilTicks <= 0 || this.tendrilTarget == null) {
            return;
        }
        this.tendrilTicks--;
        if (this.tendrilTicks % 3 == 0) {
            ringParticles(level, this.tendrilTarget, TENDRIL_RADIUS, 16, ModParticles.VIRUS_SPORE.get());
        }
        if (this.tendrilTicks > 0) {
            return;
        }
        // Einschlag
        Vec3 at = this.tendrilTarget;
        this.tendrilTarget = null;
        level.sendParticles(ParticleTypes.CRIT, at.x, at.y + 0.5, at.z, 30, TENDRIL_RADIUS * 0.5, 0.6, TENDRIL_RADIUS * 0.5, 0.2);
        level.sendParticles(ModParticles.VIRUS_SPORE.get(), at.x, at.y + 0.6, at.z, 40, TENDRIL_RADIUS * 0.5, 0.8, TENDRIL_RADIUS * 0.5, 0.05);
        level.playSound(null, BlockPos.containing(at), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.5F, 0.6F);
        float damage = (float) (7.0 * difficulty().damage());
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(TENDRIL_RADIUS, 2.0, TENDRIL_RADIUS),
                e -> e != this && !(e instanceof InfectedMob))) {
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            if (entity.hurt(ModDamageTypes.source(level, ModDamageTypes.VIRUS_PULSE, this), damage)) {
                entity.push(0.0, 0.7, 0.0);
                entity.hurtMarked = true;
                VirusEffects.giveFever(entity, ShardlineConfig.FEVER_DURATION_SECONDS.get() * 10);
            }
        }
    }

    // --- Phase 3: Beschwoerung ---

    private void startSummon(ServerLevel level) {
        List<Mob> wild = level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16.0),
                mob -> mob instanceof InfectedMob infected && !infected.isTamed());
        int count = Math.min(2 + this.random.nextInt(2), difficulty().minions() - wild.size());
        if (count <= 0) {
            return;
        }
        this.summonSpots.clear();
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            this.summonSpots.add(BlockPos.containing(this.getX() + Math.cos(angle) * 4.5, this.getY(), this.getZ() + Math.sin(angle) * 4.5));
        }
        this.summonTicks = SUMMON_WINDUP;
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "summon_infected");
        level.playSound(null, this.blockPosition(), ModSounds.VIRUS_HEART_SUMMON.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
    }

    private void tickSummon(ServerLevel level) {
        if (this.summonTicks <= 0) {
            return;
        }
        this.summonTicks--;
        if (this.summonTicks % 3 == 0) {
            for (BlockPos spot : this.summonSpots) {
                ringParticles(level, Vec3.atBottomCenterOf(spot), 0.7, 6, ModParticles.VIRUS_SPORE.get());
            }
        }
        if (this.summonTicks > 0) {
            return;
        }
        for (BlockPos spot : this.summonSpots) {
            // Meist Shard-Zombies, manchmal kristallisierte Tiere
            int roll = this.random.nextInt(10);
            EntityType<? extends Mob> type = roll < 6 ? ModEntities.SHARD_ZOMBIE.get()
                    : roll < 8 ? ModEntities.CRYSTALLIZED_PIG.get() : ModEntities.CRYSTALLIZED_SHEEP.get();
            Mob mob = type.create(level);
            if (mob == null) {
                continue;
            }
            mob.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, this.random.nextFloat() * 360.0F, 0.0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(mob);
            level.sendParticles(ModParticles.VIRUS_SPORE.get(), mob.getX(), mob.getY() + 1.0, mob.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
        }
        this.summonSpots.clear();
    }

    private static void ringParticles(ServerLevel level, Vec3 center, double radius, int count, ParticleOptions particle) {
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2.0 / count;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y + 0.15, center.z + Math.sin(angle) * radius,
                    1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    /** Fieberwelle: Schaden, Rueckstoss und Splitterfieber im Umkreis, streut Infektion. */
    private void pulse(ServerLevel level) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "pulse");
        level.broadcastEntityEvent(this, EVENT_PULSE);
        level.playSound(null, this.blockPosition(), ModSounds.VIRUS_HEART_PULSE.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
        for (int i = 0; i < 40; i++) {
            float angle = i * Mth.TWO_PI / 40.0F;
            level.sendParticles(ModParticles.VIRUS_SPORE.get(), this.getX() + Mth.cos(angle) * PULSE_RADIUS * 0.6, this.getY() + 0.5,
                    this.getZ() + Mth.sin(angle) * PULSE_RADIUS * 0.6, 2, 0.6, 0.2, 0.6, 0.02);
        }
        AABB area = this.getBoundingBox().inflate(PULSE_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this && !(e instanceof InfectedMob))) {
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            if (VirusEffects.isImmune(entity)) {
                continue;
            }
            entity.hurt(ModDamageTypes.source(level, ModDamageTypes.VIRUS_PULSE, this), (float) (5.0 * difficulty().damage()));
            Vec3 push = entity.position().subtract(this.position()).normalize().scale(0.9);
            entity.push(push.x, 0.35, push.z);
            entity.hurtMarked = true;
            VirusEffects.giveFever(entity, ShardlineConfig.FEVER_DURATION_SECONDS.get() * 10);
        }
        VirusSpreadManager.seedAround(level, this.blockPosition(), (int) PULSE_RADIUS, 12, this.random);
    }

    private void volley(ServerLevel level, LivingEntity target) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "pulse");
        Vec3 origin = this.position().add(0.0, this.getBbHeight() * 0.6, 0.0);
        for (int i = 0; i < 3; i++) {
            Vec3 direction = target.getEyePosition().subtract(origin);
            ShardProjectileEntity.shoot(level, this, origin.x, origin.y, origin.z, direction, 1.1F, 4.0F + i * 2.0F, 6.0F);
        }
        level.playSound(null, this.blockPosition(), ModSounds.SHARD_SCOUT_SHOOT.get(), SoundSource.HOSTILE, 1.5F, 0.6F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING) || source.is(ModDamageTypes.SHARD_FEVER)
                || source.is(ModDamageTypes.VIRUS_PULSE)) {
            return false;
        }
        if (source.getEntity() instanceof InfectedMob infected && !infected.isTamed()) {
            return false;
        }
        if (this.transitionTicks > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!this.level().isClientSide) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 1.2F, 0.5F);
            }
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive() && this.hurtAnimCooldown == 0 && this.tendrilTicks == 0 && this.summonTicks == 0) {
            this.hurtAnimCooldown = 12;
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "hurt");
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "death");
            this.onDefeated(level, source);
        }
    }

    /** Folgen des Siegs (Reinigung, Fortschritt zum Kern des Bruchs); Unterklassen (Schwaerenherz) ersetzen sie. */
    protected void onDefeated(ServerLevel level, DamageSource source) {
        StoryEvents.onVirusHeartDefeated(level, this, source);
    }

    /** Fuer Unterklassen (Schwaerenherz): Name und Farbe der Bossleiste. */
    protected ServerBossEvent bossEvent() {
        return this.bossEvent;
    }

    /** Zeitpunkt (Util.getMillis) der letzten Fieberwelle, die dieser Client gesehen hat; fuer die Bossleiste. */
    public static long lastPulseMillis() {
        return lastPulseMillis;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_PULSE) {
            lastPulseMillis = Util.getMillis();
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Der Koerper bleibt fuer den Clip {@code death} stehen (2,2 s statt der ueblichen 1 s). */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", this.getPhase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int phase = Math.max(1, Math.min(3, tag.getInt("Phase")));
        this.entityData.set(DATA_PHASE, tag.contains("Phase") ? phase : phaseFor(this.getHealth() / this.getMaxHealth()));
        this.bossEvent.setColor(phase == 3 ? BossEvent.BossBarColor.RED : phase == 2 ? BossEvent.BossBarColor.PINK : BossEvent.BossBarColor.PURPLE);
        this.announced = true;
    }

    // --- Unbeweglich ---

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ModParticles.VIRUS_SPORE.get(), this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0),
                        0.0, 0.04, 0.0);
            }
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.WITCH, this.getRandomX(1.2), this.getRandomY(), this.getRandomZ(1.2), 0.0, 0.0, 0.0);
            }
        }
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

    // --- Sounds & Animation ---

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VIRUS_HEART_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 40;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VIRUS_HEART_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VIRUS_HEART_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.walkIdle(this));
        controllers.add(ShardlineAnimations.actions(this, "pulse", "summon", "spawn", "attack_tendrils", "summon_infected", "hurt", "death"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
