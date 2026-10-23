package eu.shardline.mod.virus;

import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.effect.ModEffects;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryManager;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Wirkung des Virus auf Lebewesen: Splitterfieber, Kristallisierung von Mobs, Virus-Lauf der
 * Splitterherrscher und die Sporen-Partikel infizierter Bloecke.
 */
public final class VirusEffects {
    private VirusEffects() {
    }

    /** Wird jeden Tick fuer Lebewesen auf einem infizierten Block aufgerufen (nur Server). */
    public static void onStandingOnInfected(Level level, Entity entity) {
        if (entity.tickCount % 20 != 0 || !(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        if (!StoryManager.isVirusActive(server)) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        if (living instanceof ServerPlayer player) {
            if (StoryManager.isRuler(player)) {
                if (ShardlineConfig.VIRUS_RUN.get()) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
                }
                return;
            }
            if (player.isCreative() || player.isSpectator()) {
                return;
            }
            StoryManager.reach(player, StoryChapter.THE_BREACH);
            if (random.nextDouble() < ShardlineConfig.FEVER_CHANCE_ON_INFECTED_BLOCK.get()) {
                giveFever(player, ShardlineConfig.FEVER_DURATION_SECONDS.get() * 20);
            }
            return;
        }
        if (living instanceof InfectedMob || !(living instanceof Mob mob) || random.nextDouble() >= ShardlineConfig.MOB_CONVERSION_CHANCE.get()) {
            return;
        }
        EntityType<?> type = mob.getType();
        if (type == EntityType.ZOMBIE || type == EntityType.HUSK) {
            convert(serverLevel, mob, ModEntities.SHARD_ZOMBIE.get());
        } else if (type == EntityType.COW) {
            convert(serverLevel, mob, ModEntities.CRYSTALLIZED_COW.get());
        } else if (mob instanceof Villager && ShardlineConfig.VILLAGERS_TRANSFORM.get() && serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
            convert(serverLevel, mob, ModEntities.SHARD_ZOMBIE.get());
        }
    }

    /** Verwandelt einen Mob in eine infizierte Variante (behaelt Name, Ausruestung, Position). */
    @Nullable
    public static <T extends Mob> T convert(ServerLevel level, Mob mob, EntityType<T> type) {
        if (!EventHooks.canLivingConvert(mob, type, timer -> {
        })) {
            return null;
        }
        T converted = mob.convertTo(type, true);
        if (converted == null) {
            return null;
        }
        converted.finalizeSpawn(level, level.getCurrentDifficultyAt(converted.blockPosition()), MobSpawnType.CONVERSION, null);
        EventHooks.onLivingConvert(mob, converted);
        level.sendParticles(ModParticles.VIRUS_SPORE.get(), converted.getX(), converted.getY() + 1.0, converted.getZ(),
                30, 0.4, 0.6, 0.4, 0.02);
        level.playSound(null, converted.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
        return converted;
    }

    /** Splitterfieber verleihen; Splitterherrscher und infizierte Wesen sind immun. */
    public static void giveFever(LivingEntity entity, int ticks) {
        if (ticks <= 0 || isImmune(entity)) {
            return;
        }
        entity.addEffect(new MobEffectInstance(ModEffects.SHARD_FEVER, ticks, 0));
    }

    public static boolean isImmune(LivingEntity entity) {
        if (entity instanceof InfectedMob || entity.getType() == ModEntities.VIRUS_HEART.get()) {
            return true;
        }
        return entity instanceof ServerPlayer player && StoryManager.isRuler(player);
    }

    /** Sporen ueber infizierten Bloecken (nur Client, rein optisch). */
    public static void spawnSpores(Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0 || !ShardlineClientConfig.INFECTION_FOG.get()) {
            return;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir()) {
            return;
        }
        level.addParticle(ModParticles.VIRUS_SPORE.get(), pos.getX() + random.nextDouble(), pos.getY() + 1.05,
                pos.getZ() + random.nextDouble(), (random.nextDouble() - 0.5) * 0.01, 0.015 + random.nextDouble() * 0.02,
                (random.nextDouble() - 0.5) * 0.01);
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                    0.0, 0.0, 0.0);
        }
    }
}
