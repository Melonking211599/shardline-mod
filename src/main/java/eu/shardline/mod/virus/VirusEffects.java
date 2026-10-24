package eu.shardline.mod.virus;

import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.effect.ModEffects;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.infected.CrystallizedCowEntity;
import eu.shardline.mod.entity.infected.CrystallizedEndermanEntity;
import eu.shardline.mod.entity.infected.CrystallizedPiglinEntity;
import eu.shardline.mod.entity.infected.CrystallizedPigEntity;
import eu.shardline.mod.entity.infected.CrystallizedSheepEntity;
import eu.shardline.mod.entity.infected.CrystallizedVillagerEntity;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.entity.infected.ShardZombieEntity;
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
        onStandingOnInfected(level, entity, VirusStrain.SHARD);
    }

    /** Wie oben, mit der Wirkung des Stamms (Glutbrand: kurz brennen, Leerenfaeule: Dunkelheit). */
    public static void onStandingOnInfected(Level level, Entity entity, VirusStrain strain) {
        if (entity.tickCount % 20 != 0 || !(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        if (!StoryManager.isVirusActive(server)) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        if (strain != VirusStrain.SHARD && ShardlineConfig.VIRUS_STRAIN_EFFECTS.get() && !isImmune(living)
                && !(living instanceof ServerPlayer player && (player.isCreative() || player.isSpectator()))) {
            applyStrain(living, strain);
        }
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
        if (living instanceof InfectedMob || !(living instanceof Mob mob) || living.hasEffect(ModEffects.IMMUNITY)
                || random.nextDouble() >= ShardlineConfig.MOB_CONVERSION_CHANCE.get()) {
            return;
        }
        EntityType<?> type = mob.getType();
        boolean animals = ShardlineConfig.ANIMALS_TRANSFORM.get();
        if (type == EntityType.ZOMBIE || type == EntityType.HUSK) {
            convert(serverLevel, mob, ModEntities.SHARD_ZOMBIE.get());
        } else if (type == EntityType.COW && animals) {
            convert(serverLevel, mob, ModEntities.CRYSTALLIZED_COW.get());
        } else if (type == EntityType.SHEEP && animals) {
            convert(serverLevel, mob, ModEntities.CRYSTALLIZED_SHEEP.get());
        } else if (type == EntityType.PIG && animals) {
            convert(serverLevel, mob, ModEntities.CRYSTALLIZED_PIG.get());
        } else if (mob instanceof Villager villager && type == EntityType.VILLAGER && ShardlineConfig.VILLAGERS_TRANSFORM.get()) {
            CrystallizedVillagerEntity.infect(serverLevel, villager);
        } else if (ShardlineConfig.NETHER_END_MOBS_TRANSFORM.get()
                && (type == EntityType.PIGLIN || type == EntityType.ZOMBIFIED_PIGLIN || type == EntityType.PIGLIN_BRUTE)) {
            convert(serverLevel, mob, ModEntities.CRYSTALLIZED_PIGLIN.get());
        } else if (ShardlineConfig.NETHER_END_MOBS_TRANSFORM.get() && type == EntityType.ENDERMAN) {
            convert(serverLevel, mob, ModEntities.CRYSTALLIZED_ENDERMAN.get());
        }
    }

    /** Glutbrand: 2 Sekunden Feuer (feuerfeste Wesen ausgenommen). Leerenfaeule: 3 Sekunden Dunkelheit. */
    public static void applyStrain(LivingEntity living, VirusStrain strain) {
        switch (strain) {
            case EMBER -> {
                if (!living.fireImmune() && living.getRemainingFireTicks() < 20) {
                    living.igniteForSeconds(2.0F);
                }
            }
            case VOID -> living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, true));
            default -> {
            }
        }
    }

    /**
     * Reinigung eines infizierten Wesens (Reinigungsessenz): zurueck zur Vanilla-Form, ein kristallisierter
     * Dorfbewohner bekommt seinen alten Beruf zurueck.
     *
     * @return das gereinigte Wesen oder {@code null}, wenn es sich nicht reinigen laesst
     */
    @Nullable
    public static Mob purifyMob(ServerLevel level, LivingEntity target) {
        Mob cured;
        if (target instanceof CrystallizedVillagerEntity villager) {
            cured = villager.restore(level);
        } else if (target instanceof CrystallizedPiglinEntity piglin) {
            cured = level.dimension() == Level.NETHER ? convert(level, piglin, EntityType.PIGLIN) : convert(level, piglin, EntityType.ZOMBIFIED_PIGLIN);
        } else if (target instanceof CrystallizedEndermanEntity enderman) {
            cured = convert(level, enderman, EntityType.ENDERMAN);
        } else if (target instanceof CrystallizedCowEntity cow) {
            cured = convert(level, cow, EntityType.COW);
        } else if (target instanceof CrystallizedSheepEntity sheep) {
            cured = convert(level, sheep, EntityType.SHEEP);
        } else if (target instanceof CrystallizedPigEntity pig) {
            cured = convert(level, pig, EntityType.PIG);
        } else if (target instanceof ShardZombieEntity zombie) {
            cured = convert(level, zombie, EntityType.ZOMBIE);
        } else {
            return null;
        }
        if (cured != null) {
            level.playSound(null, cured.blockPosition(), ModSounds.INFECTED_CURED.get(), SoundSource.NEUTRAL, 1.0F, 1.2F);
        }
        return cured;
    }

    /** Ob die Reinigungsessenz das Wesen zurueckverwandeln kann. */
    public static boolean isCurable(LivingEntity target) {
        return target instanceof CrystallizedVillagerEntity || target instanceof CrystallizedCowEntity || target instanceof CrystallizedSheepEntity
                || target instanceof CrystallizedPigEntity || target instanceof ShardZombieEntity || target instanceof CrystallizedEndermanEntity;
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

    /** Immunitaet verleihen (Reinheitsanker, Reinigungsessenz); verlaengert, kuerzt aber nie. */
    public static void giveImmunity(LivingEntity entity, int ticks, boolean ambient) {
        if (ticks <= 0 || entity instanceof InfectedMob) {
            return;
        }
        entity.addEffect(new MobEffectInstance(ModEffects.IMMUNITY, ticks, 0, ambient, !ambient, true));
    }

    /** Splitterfieber verleihen; Splitterherrscher und infizierte Wesen sind immun, die Immunitaet blockt es ebenfalls. */
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
        spawnSpores(level, pos, random, VirusStrain.SHARD);
    }

    /** Wie oben; Glutbrand sprueht Funken und Asche, Leerenfaeule laesst Leerenstaub aufsteigen. */
    public static void spawnSpores(Level level, BlockPos pos, RandomSource random, VirusStrain strain) {
        if (random.nextInt(8) != 0 || !ShardlineClientConfig.INFECTION_FOG.get()) {
            return;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir()) {
            return;
        }
        if (strain != VirusStrain.SHARD) {
            double x = pos.getX() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            if (strain == VirusStrain.EMBER) {
                level.addParticle(random.nextInt(3) == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.WHITE_ASH, x, pos.getY() + 1.05, z,
                        0.0, 0.02 + random.nextDouble() * 0.02, 0.0);
            } else {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x, pos.getY() + 1.05, z, 0.0, 0.03, 0.0);
            }
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
