package eu.shardline.mod.entity.infected;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.virus.VirusEffects;
import eu.shardline.mod.virus.VirusSpreadManager;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Gemeinsame Logik der infizierten Mobs: Besitzer speichern, zaehmen, folgen, verteidigen,
 * neue Infektionsstellen setzen.
 */
public final class InfectedMobs {
    private InfectedMobs() {
    }

    public static void save(InfectedMob mob, CompoundTag tag) {
        if (mob.getOwnerUUID() != null) {
            tag.putUUID("ShardOwner", mob.getOwnerUUID());
        }
        if (mob.getSummonTicks() > 0) {
            tag.putInt("SummonTicks", mob.getSummonTicks());
        }
    }

    public static void load(InfectedMob mob, CompoundTag tag) {
        mob.setOwnerUUID(tag.hasUUID("ShardOwner") ? tag.getUUID("ShardOwner") : null);
        mob.setSummonTicks(tag.getInt("SummonTicks"));
    }

    /** Persistente Markierung der Befallenen einer Schwaere in der beherrschten Welt ("Gegengesang"). */
    private static final String COUNTER_SONG = "ShardlineCounterSong";

    public static void markCounterSong(Mob mob) {
        mob.getPersistentData().putBoolean(COUNTER_SONG, true);
    }

    /** Befallene des Gegengesangs greifen auch Splitterherrscher an und gehorchen ihnen nicht. */
    public static boolean isCounterSong(Mob mob) {
        return mob.getPersistentData().getBoolean(COUNTER_SONG);
    }

    /** Ein Lebewesen, das ein infizierter Mob nicht angreifen soll. */
    public static boolean isFriendly(Mob self, LivingEntity other) {
        UUID owner = ((InfectedMob) self).getOwnerUUID();
        if (owner == null && isCounterSong(self) && other instanceof ServerPlayer) {
            return false;
        }
        if (other instanceof InfectedMob them) {
            // Gleiche Herde: beide wild oder beide vom selben Spieler gezaehmt.
            return Objects.equals(owner, them.getOwnerUUID());
        }
        if (other.getType() == ModEntities.VIRUS_HEART.get()) {
            return owner == null;
        }
        if (other instanceof ServerPlayer player) {
            return player.getUUID().equals(owner) || (owner == null && StoryManager.isRuler(player));
        }
        return false;
    }

    /** Zielauswahl fuer wilde infizierte Mobs: keine Splitterherrscher, nichts wenn gezaehmt. */
    public static boolean canHunt(Mob self, LivingEntity target) {
        return !((InfectedMob) self).isTamed() && !isFriendly(self, target);
    }

    /**
     * Rechtsklick eines Splitterherrschers mit einem Shard-Splitter zaehmt den Mob.
     *
     * @return {@code null}, wenn die Interaktion nicht behandelt wurde
     */
    @Nullable
    public static InteractionResult tryTame(Mob mob, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InfectedMob infected = (InfectedMob) mob;
        if (!stack.is(ModItems.SHARD_FRAGMENT.get()) || infected.isTamed()) {
            return null;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (!StoryManager.isRuler(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.translatable("message.shardline.tame_requires_ruler"), true);
            return InteractionResult.CONSUME;
        }
        if (isCounterSong(mob)) {
            serverPlayer.displayClientMessage(Component.translatable("message.shardline.tame_counter_song"), true);
            return InteractionResult.CONSUME;
        }
        stack.consume(1, player);
        infected.setOwnerUUID(player.getUUID());
        mob.setTarget(null);
        mob.setPersistenceRequired();
        mob.getNavigation().stop();
        ((ServerLevel) mob.level()).sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(),
                6, 0.4, 0.3, 0.4, 0.0);
        serverPlayer.displayClientMessage(Component.translatable("message.shardline.tamed_infected", mob.getDisplayName()), true);
        StoryManager.award(serverPlayer, "story/tame_infected");
        recordHerd(serverPlayer, mob.getType());
        eu.shardline.mod.story.quest.Quests.trigger(serverPlayer, "tame",
                target -> target.equals("any") || eu.shardline.mod.story.quest.Quests.matchesEntity(mob.getType(), target), 1);
        return InteractionResult.CONSUME;
    }

    /** Merkt sich gezaehmte Tierarten; wer alle vier hat, erhaelt "Kristallherde". */
    private static void recordHerd(ServerPlayer player, EntityType<?> type) {
        List<EntityType<?>> herd = List.of(ModEntities.CRYSTALLIZED_COW.get(), ModEntities.CRYSTALLIZED_SHEEP.get(),
                ModEntities.CRYSTALLIZED_PIG.get(), ModEntities.CRYSTALLIZED_VILLAGER.get());
        if (!herd.contains(type)) {
            return;
        }
        var data = StoryManager.data(player.server);
        data.addFlag(player.getUUID(), "tamed:" + EntityType.getKey(type).getPath());
        if (herd.stream().allMatch(member -> data.hasFlag(player.getUUID(), "tamed:" + EntityType.getKey(member).getPath()))) {
            StoryManager.award(player, "story/infected_herd");
        }
    }

    /** Nahkampftreffer gegen einen kristallisierten Traeger: absplitternde Kristalle verursachen Fieber. */
    public static void splinterAttacker(LivingEntity self, DamageSource source, float chance) {
        if (!self.level().isClientSide && source.getDirectEntity() instanceof LivingEntity attacker
                && attacker.distanceToSqr(self) < 9.0 && self.getRandom().nextFloat() < chance) {
            VirusEffects.giveFever(attacker, 5 * 20);
        }
    }

    /** Jede Sekunde: Infektion streuen, beschworene Diener altern lassen. */
    public static void serverTick(Mob mob, double seedMultiplier) {
        InfectedMob infected = (InfectedMob) mob;
        ServerLevel level = (ServerLevel) mob.level();
        if (infected.getSummonTicks() > 0) {
            int left = infected.getSummonTicks() - 1;
            infected.setSummonTicks(left);
            if (left == 0) {
                level.sendParticles(ModParticles.VIRUS_SPORE.get(), mob.getX(), mob.getY() + 0.8, mob.getZ(), 25, 0.3, 0.5, 0.3, 0.02);
                mob.discard();
                return;
            }
        }
        if (mob.tickCount % 20 != 0) {
            return;
        }
        if (mob.getRandom().nextDouble() < ShardlineConfig.INFECTED_MOB_SEED_CHANCE.get() * seedMultiplier) {
            BlockPos below = mob.getBlockPosBelowThatAffectsMyMovement();
            VirusSpreadManager.queueInfection(level, below);
            VirusSpreadManager.queueInfection(level, below.offset(mob.getRandom().nextInt(3) - 1, 0, mob.getRandom().nextInt(3) - 1));
        }
    }

    /** Folgt dem Besitzer; teleportiert bei grosser Entfernung. */
    public static class FollowOwnerGoal extends Goal {
        private final PathfinderMob mob;
        private final double speed;
        @Nullable
        private LivingEntity owner;
        private int recalc;

        public FollowOwnerGoal(PathfinderMob mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = ((InfectedMob) this.mob).getOwner();
            if (owner == null || owner.isSpectator() || this.mob.getTarget() != null || this.mob.distanceToSqr(owner) < 36.0) {
                return false;
            }
            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.owner != null && this.owner.isAlive() && this.mob.getTarget() == null && this.mob.distanceToSqr(this.owner) > 9.0;
        }

        @Override
        public void start() {
            this.recalc = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }
            this.mob.getLookControl().setLookAt(this.owner, 10.0F, this.mob.getMaxHeadXRot());
            if (--this.recalc > 0) {
                return;
            }
            this.recalc = 10;
            if (this.mob.distanceToSqr(this.owner) > 576.0) {
                BlockPos target = this.owner.blockPosition().offset(this.mob.getRandom().nextInt(5) - 2, 0, this.mob.getRandom().nextInt(5) - 2);
                if (this.mob.level().getBlockState(target).isAir() && this.mob.level().getBlockState(target.below()).isSolid()) {
                    this.mob.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, this.mob.getYRot(), this.mob.getXRot());
                    this.mob.getNavigation().stop();
                    return;
                }
            }
            this.mob.getNavigation().moveTo(this.owner, this.speed);
        }
    }

    /** Greift an, was den Besitzer angreift oder vom Besitzer angegriffen wird. */
    public static class DefendOwnerGoal extends TargetGoal {
        private final Mob owned;
        @Nullable
        private LivingEntity candidate;
        private int lastHurtBy;
        private int lastHurt;

        public DefendOwnerGoal(Mob mob) {
            super(mob, false);
            this.owned = mob;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = ((InfectedMob) this.owned).getOwner();
            if (owner == null) {
                return false;
            }
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker != null && owner.getLastHurtByMobTimestamp() != this.lastHurtBy
                    && this.canAttack(attacker, TargetingConditions.DEFAULT) && !isFriendly(this.owned, attacker)) {
                this.candidate = attacker;
                this.lastHurtBy = owner.getLastHurtByMobTimestamp();
                return true;
            }
            LivingEntity victim = owner.getLastHurtMob();
            if (victim != null && owner.getLastHurtMobTimestamp() != this.lastHurt
                    && this.canAttack(victim, TargetingConditions.DEFAULT) && !isFriendly(this.owned, victim)) {
                this.candidate = victim;
                this.lastHurt = owner.getLastHurtMobTimestamp();
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.mob.setTarget(this.candidate);
            super.start();
        }
    }

    @Nullable
    public static UUID ownerOf(Mob mob) {
        return mob instanceof InfectedMob infected ? infected.getOwnerUUID() : null;
    }
}
