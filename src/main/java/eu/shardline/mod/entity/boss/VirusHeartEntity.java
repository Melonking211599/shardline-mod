package eu.shardline.mod.entity.boss;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.entity.infected.ShardZombieEntity;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusSpreadManager;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
 * Bewegt sich nicht, verschiesst Splitter, sendet Fieberwellen aus und ruft Shard-Zombies.
 * Sein Tod reinigt ein grosses Gebiet. Wer drei Herzen besiegt, erhaelt den Kern des Bruchs.
 */
public class VirusHeartEntity extends Monster implements GeoEntity {
    private static final double PULSE_RADIUS = 8.0;
    private static final int MAX_MINIONS = 4;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.shardline.virus_heart"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    private int pulseCooldown = 100;
    private int volleyCooldown = 50;
    private int summonCooldown = 80;

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
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> !VirusEffects.isImmune(target)));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        LivingEntity target = this.getTarget();
        if (target != null && (!target.isAlive() || this.distanceToSqr(target) > 32.0 * 32.0)) {
            this.setTarget(null);
            target = null;
        }

        if (--this.pulseCooldown <= 0) {
            this.pulseCooldown = 100 + this.random.nextInt(40);
            this.pulse(level);
        }
        if (target != null && --this.volleyCooldown <= 0 && this.hasLineOfSight(target)) {
            this.volleyCooldown = 40 + this.random.nextInt(30);
            this.volley(level, target);
        }
        if (target != null && --this.summonCooldown <= 0) {
            this.summonCooldown = 200 + this.random.nextInt(100);
            this.summonMinions(level);
        }
    }

    /** Fieberwelle: Schaden, Rueckstoss und Splitterfieber im Umkreis, streut Infektion. */
    private void pulse(ServerLevel level) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "pulse");
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
            entity.hurt(ModDamageTypes.source(level, ModDamageTypes.VIRUS_PULSE, this), 5.0F);
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

    private void summonMinions(ServerLevel level) {
        List<ShardZombieEntity> minions = level.getEntitiesOfClass(ShardZombieEntity.class, this.getBoundingBox().inflate(16.0),
                zombie -> zombie.getOwnerUUID() == null);
        int count = Math.min(2, MAX_MINIONS - minions.size());
        for (int i = 0; i < count; i++) {
            ShardZombieEntity zombie = ModEntities.SHARD_ZOMBIE.get().create(level);
            if (zombie == null) {
                continue;
            }
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            BlockPos pos = BlockPos.containing(this.getX() + Math.cos(angle) * 4.0, this.getY(), this.getZ() + Math.sin(angle) * 4.0);
            zombie.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.random.nextFloat() * 360.0F, 0.0F);
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(zombie);
            level.sendParticles(ModParticles.VIRUS_SPORE.get(), zombie.getX(), zombie.getY() + 1.0, zombie.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
        }
        if (count > 0) {
            this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "summon");
        }
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
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            StoryEvents.onVirusHeartDefeated(level, this, source);
        }
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
        controllers.add(ShardlineAnimations.actions(this, "pulse", "summon"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
