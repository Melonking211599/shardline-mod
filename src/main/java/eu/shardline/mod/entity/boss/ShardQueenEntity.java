package eu.shardline.mod.entity.boss;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.entity.ai.HoveringMoveControl;
import eu.shardline.mod.entity.misc.VoidZoneEntity;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Boss 2 - Die Splitterkoenigin (End-Game), fliegend.
 * <ul>
 *     <li>Phase 1 (&gt; 66 %): Projektilhagel - gezielte Salven und Splitterregen von oben.</li>
 *     <li>Phase 2 (&gt; 33 %): Teleportation und Klone; solange Klone leben, halbiert sich ihr Schaden.</li>
 *     <li>Phase 3: Void-Zonen unter den Spielern, schnellere Salven.</li>
 * </ul>
 */
public class ShardQueenEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(ShardQueenEntity.class, EntityDataSerializers.INT);
    public static final double ARENA_RADIUS = 26.0;
    private static final double LEASH_RADIUS = 44.0;
    private static final int MAX_CLONES = 6;
    private static final int MAX_ZONES = 6;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.shardline.shard_queen"),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_6);
    @Nullable
    private BlockPos home;
    private int volleyCooldown = 60;
    private int hailCooldown = 120;
    private int teleportCooldown = 100;
    private int cloneCooldown = 40;
    private int zoneCooldown = 40;
    private int idleTicks;

    public ShardQueenEntity(EntityType<? extends ShardQueenEntity> type, Level level) {
        super(type, level);
        this.moveControl = new HoveringMoveControl(this, 0.05);
        this.setNoGravity(true);
        this.xpReward = 500;
        this.setPersistenceRequired();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ShardlineConfig.QUEEN_HEALTH.get());
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new HoverGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
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
    }

    private Vec3 arenaCenter() {
        return this.home != null ? Vec3.atBottomCenterOf(this.home) : this.position();
    }

    // --- Boss-Logik ---

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();
        this.updatePhase(level);

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.idleTicks = 0;
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.tickAttacks(level, target);
        } else if (++this.idleTicks > 600 && this.getHealth() < this.getMaxHealth()) {
            this.resetFight(level);
        }

        if (this.home != null && this.distanceToSqr(this.arenaCenter()) > LEASH_RADIUS * LEASH_RADIUS) {
            this.teleportTo(this.home.getX() + 0.5, this.home.getY() + 10.0, this.home.getZ() + 0.5);
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void updatePhase(ServerLevel level) {
        float ratio = this.getHealth() / this.getMaxHealth();
        int phase = ratio > 0.66F ? 1 : ratio > 0.33F ? 2 : 3;
        if (phase <= this.getPhase()) {
            return;
        }
        this.entityData.set(DATA_PHASE, phase);
        this.applyPhaseVisuals(phase);
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "scream");
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.QUEEN_SCREAM.get(), SoundSource.HOSTILE, 4.0F, phase == 3 ? 0.7F : 1.0F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 150, 1.5, 2.0, 1.5, 0.3);
        String key = phase == 2 ? "message.shardline.queen_phase_two" : "message.shardline.queen_phase_three";
        for (ServerPlayer player : this.bossEvent.getPlayers()) {
            player.displayClientMessage(Component.translatable(key), true);
        }
        this.cloneCooldown = 20;
        this.zoneCooldown = 20;
    }

    private void applyPhaseVisuals(int phase) {
        switch (phase) {
            case 2 -> this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            case 3 -> this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            default -> this.bossEvent.setColor(BossEvent.BossBarColor.PINK);
        }
    }

    /** Niemand kaempft mehr: Arena aufraeumen und vollstaendig heilen. */
    private void resetFight(ServerLevel level) {
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_PHASE, 1);
        this.applyPhaseVisuals(1);
        this.discardClones();
        VoidZoneEntity.clearAround(this, LEASH_RADIUS);
        level.sendParticles(ModParticles.SHARD_SPARKLE.get(), this.getX(), this.getY(0.5), this.getZ(), 60, 1.0, 1.5, 1.0, 0.1);
    }

    private void tickAttacks(ServerLevel level, LivingEntity target) {
        int phase = this.getPhase();
        this.volleyCooldown--;
        this.hailCooldown--;
        this.teleportCooldown--;
        this.cloneCooldown--;
        this.zoneCooldown--;

        if (this.volleyCooldown <= 0 && this.hasLineOfSight(target)) {
            this.fireVolley(level, target, phase == 3 ? 7 : 5);
            this.volleyCooldown = switch (phase) {
                case 1 -> 50;
                case 2 -> 60;
                default -> 36;
            };
        }
        if ((phase == 1 || phase == 3) && this.hailCooldown <= 0) {
            this.shardHail(level);
            this.hailCooldown = phase == 1 ? 140 : 200;
        }
        if (phase >= 2 && this.teleportCooldown <= 0) {
            this.teleportInArena(level);
            this.teleportCooldown = phase == 2 ? 100 : 160;
        }
        if (phase == 2 && this.cloneCooldown <= 0) {
            this.summonClones(level, target);
            this.cloneCooldown = 300;
        }
        if (phase == 3 && this.zoneCooldown <= 0) {
            this.spawnVoidZones(level);
            this.zoneCooldown = 160;
        }
    }

    private float projectileDamage() {
        return ShardlineConfig.QUEEN_PROJECTILE_DAMAGE.get().floatValue();
    }

    private void fireVolley(ServerLevel level, LivingEntity target, int count) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "cast");
        Vec3 origin = new Vec3(this.getX(), this.getY(0.6), this.getZ());
        Vec3 aim = target.getEyePosition().subtract(origin);
        for (int i = 0; i < count; i++) {
            float spread = (i - (count - 1) / 2.0F) * 6.0F;
            Vec3 direction = aim.yRot(spread * Mth.DEG_TO_RAD);
            ShardProjectileEntity.shoot(level, this, origin.x, origin.y, origin.z, direction, 1.3F, 1.0F, this.projectileDamage());
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.QUEEN_CAST.get(), SoundSource.HOSTILE, 2.0F, 1.2F);
    }

    /** Splitterregen: ueber jedem Spieler in der Arena fallen Kristallsplitter herab. */
    private void shardHail(ServerLevel level) {
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "cast");
        for (Player player : this.playersInArena(level)) {
            for (int i = 0; i < 8; i++) {
                double x = player.getX() + (this.random.nextDouble() - 0.5) * 6.0;
                double z = player.getZ() + (this.random.nextDouble() - 0.5) * 6.0;
                double y = player.getY() + 12.0 + this.random.nextDouble() * 4.0;
                ShardProjectileEntity.shoot(level, this, x, y, z, new Vec3(0.0, -1.0, 0.0), 0.8F, 6.0F, this.projectileDamage() * 0.75F);
            }
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.QUEEN_CAST.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
    }

    private void teleportInArena(ServerLevel level) {
        Vec3 center = this.arenaCenter();
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double distance = 6.0 + this.random.nextDouble() * (ARENA_RADIUS - 10.0);
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = center.y + 6.0 + this.random.nextDouble() * 6.0;
            AABB box = this.getDimensions(this.getPose()).makeBoundingBox(x, y, z);
            if (level.noCollision(this, box)) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 60, 0.5, 1.0, 0.5, 0.2);
                this.teleportTo(x, y, z);
                this.getMoveControl().setWantedPosition(x, y, z, 1.0);
                level.sendParticles(ModParticles.SHARD_SPARKLE.get(), x, y + 1.5, z, 60, 0.5, 1.0, 0.5, 0.2);
                level.playSound(null, x, y, z, ModSounds.QUEEN_TELEPORT.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "teleport");
                return;
            }
        }
    }

    private void summonClones(ServerLevel level, LivingEntity target) {
        int alive = this.livingClones().size();
        int count = Math.min(ShardlineConfig.QUEEN_CLONE_COUNT.get(), MAX_CLONES - alive);
        if (count <= 0) {
            return;
        }
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "summon");
        for (int i = 0; i < count; i++) {
            QueenCloneEntity clone = ModEntities.QUEEN_CLONE.get().create(level);
            if (clone == null) {
                continue;
            }
            double angle = Mth.TWO_PI * i / count + this.random.nextDouble();
            double x = this.getX() + Math.cos(angle) * 5.0;
            double z = this.getZ() + Math.sin(angle) * 5.0;
            clone.moveTo(x, this.getY(), z, this.getYRot(), 0.0F);
            clone.finalizeSpawn(level, level.getCurrentDifficultyAt(clone.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            clone.setTarget(target);
            level.addFreshEntity(clone);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, this.getY() + 1.0, z, 30, 0.4, 0.8, 0.4, 0.1);
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.QUEEN_CAST.get(), SoundSource.HOSTILE, 2.0F, 0.6F);
    }

    private void spawnVoidZones(ServerLevel level) {
        int existing = level.getEntitiesOfClass(VoidZoneEntity.class, this.arenaBox()).size();
        int budget = MAX_ZONES - existing;
        if (budget <= 0) {
            return;
        }
        this.triggerAnim(ShardlineAnimations.ACTION_CONTROLLER, "cast");
        float damage = ShardlineConfig.QUEEN_VOID_ZONE_DAMAGE.get().floatValue();
        for (Player player : this.playersInArena(level)) {
            if (budget-- <= 0) {
                break;
            }
            Vec3 ground = this.findGround(level, player.position());
            level.addFreshEntity(VoidZoneEntity.create(level, ground, damage, this));
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.QUEEN_SCREAM.get(), SoundSource.HOSTILE, 1.5F, 1.4F);
    }

    private Vec3 findGround(ServerLevel level, Vec3 start) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(start).mutable();
        for (int i = 0; i < 12; i++) {
            BlockPos below = cursor.below();
            BlockState state = level.getBlockState(below);
            if (state.isFaceSturdy(level, below, Direction.UP)) {
                return new Vec3(start.x, cursor.getY(), start.z);
            }
            cursor.move(Direction.DOWN);
        }
        return start;
    }

    private AABB arenaBox() {
        return new AABB(BlockPos.containing(this.arenaCenter())).inflate(ARENA_RADIUS + 6.0, 32.0, ARENA_RADIUS + 6.0);
    }

    private List<Player> playersInArena(ServerLevel level) {
        return level.getEntitiesOfClass(Player.class, this.arenaBox(), player -> player.isAlive() && !player.isSpectator() && !player.isCreative());
    }

    private List<QueenCloneEntity> livingClones() {
        return this.level().getEntitiesOfClass(QueenCloneEntity.class, this.getBoundingBox().inflate(LEASH_RADIUS), LivingEntity::isAlive);
    }

    private void discardClones() {
        this.livingClones().forEach(Entity::discard);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean fromPlayer = source.getEntity() instanceof Player;
        if (!fromPlayer && (source.is(ModDamageTypes.SHARD) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(ModDamageTypes.VOID_ZONE))) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        if (this.getPhase() >= 2 && !this.livingClones().isEmpty()) {
            amount *= 0.5F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.discardClones();
            VoidZoneEntity.clearAround(this, LEASH_RADIUS);
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

    // --- Flug & Diverses ---

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
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
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(this.getPhase() == 3 ? ModParticles.VOID_MOTE.get() : ModParticles.SHARD_SPARKLE.get(),
                        this.getRandomX(0.8), this.getRandomY(), this.getRandomZ(0.8), 0.0, -0.05, 0.0);
            }
        }
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
        this.entityData.set(DATA_PHASE, Mth.clamp(tag.getInt("Phase"), 1, 3));
        NbtUtils.readBlockPos(tag, "Home").ifPresent(this::setHome);
        this.applyPhaseVisuals(this.getPhase());
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.QUEEN_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.QUEEN_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.QUEEN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ShardlineAnimations.flying(this));
        controllers.add(ShardlineAnimations.actions(this, "cast", "summon", "teleport", "scream"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Schwebt um das Ziel herum (oder ueber der Arena), bleibt aber innerhalb der Arena.
     */
    static class HoverGoal extends Goal {
        private final ShardQueenEntity queen;

        HoverGoal(ShardQueenEntity queen) {
            this.queen = queen;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.queen.getMoveControl().hasWanted() && this.queen.random.nextInt(reducedTickDelay(10)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Vec3 center = this.queen.arenaCenter();
            LivingEntity target = this.queen.getTarget();
            Vec3 anchor = target != null ? target.position() : center;
            double angle = this.queen.random.nextDouble() * Mth.TWO_PI;
            double distance = target != null ? 6.0 + this.queen.random.nextDouble() * 5.0 : this.queen.random.nextDouble() * 10.0;
            double x = anchor.x + Math.cos(angle) * distance;
            double z = anchor.z + Math.sin(angle) * distance;
            double y = (target != null ? anchor.y : center.y) + 5.0 + this.queen.random.nextDouble() * 5.0;
            // In der Arena bleiben.
            Vec3 offset = new Vec3(x - center.x, 0.0, z - center.z);
            if (offset.length() > ARENA_RADIUS - 4.0) {
                offset = offset.normalize().scale(ARENA_RADIUS - 4.0);
                x = center.x + offset.x;
                z = center.z + offset.z;
            }
            y = Mth.clamp(y, center.y + 4.0, center.y + 18.0);
            this.queen.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }
}
