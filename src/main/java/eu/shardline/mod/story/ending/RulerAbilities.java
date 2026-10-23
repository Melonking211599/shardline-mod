package eu.shardline.mod.story.ending;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModAttachments;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.entity.infected.ShardZombieEntity;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.virus.VirusEffects;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * Faehigkeiten des Splitterherrschers (Ende "Beherrschen"):
 * <ul>
 *   <li>Splitterruf (Taste): holt bis zu drei gezaehmte infizierte Diener heran, fehlende werden
 *       als kurzlebige Shard-Zombies beschworen; dazu Staerke.</li>
 *   <li>Kristallwelle (Taste): Splitterwelle um den Spieler mit Schaden, Rueckstoss und kurzem Splitterfieber.</li>
 *   <li>Virus-Lauf (passiv): Tempo und Regeneration auf infiziertem Boden (siehe {@code VirusEffects}).</li>
 * </ul>
 */
public final class RulerAbilities {
    private static final double CALL_SEARCH_RADIUS = 128.0;
    private static final int SUMMON_LIFETIME_TICKS = 60 * 20;

    private RulerAbilities() {
    }

    public static void shardCall(ServerPlayer player) {
        if (!checkReady(player, ModAttachments.SHARD_CALL_READY_AT, ShardlineConfig.SHARD_CALL_COOLDOWN_SECONDS.get(), "shard_call")) {
            return;
        }
        ServerLevel level = player.serverLevel();
        int max = ShardlineConfig.SHARD_CALL_MAX_MOBS.get();
        List<Mob> servants = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(CALL_SEARCH_RADIUS),
                mob -> mob instanceof InfectedMob infected && player.getUUID().equals(infected.getOwnerUUID()) && mob.isAlive());
        servants.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(player)));

        int called = 0;
        for (Mob servant : servants) {
            if (called >= max) {
                break;
            }
            Vec3 spot = spotNear(player, called);
            servant.teleportTo(spot.x, spot.y, spot.z);
            servant.getNavigation().stop();
            level.sendParticles(ModParticles.VIRUS_SPORE.get(), spot.x, spot.y + 1.0, spot.z, 20, 0.3, 0.6, 0.3, 0.02);
            called++;
        }
        for (; called < max; called++) {
            ShardZombieEntity zombie = ModEntities.SHARD_ZOMBIE.get().create(level);
            if (zombie == null) {
                break;
            }
            Vec3 spot = spotNear(player, called);
            zombie.moveTo(spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(spot)), MobSpawnType.MOB_SUMMONED, null);
            zombie.setBaby(false);
            zombie.setOwnerUUID(player.getUUID());
            zombie.setSummonTicks(SUMMON_LIFETIME_TICKS);
            level.addFreshEntity(zombie);
            level.sendParticles(ModParticles.VIRUS_SPORE.get(), spot.x, spot.y + 1.0, spot.z, 30, 0.3, 0.6, 0.3, 0.04);
        }

        int strength = ShardlineConfig.SHARD_CALL_STRENGTH_SECONDS.get();
        if (strength > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, strength * 20, 0));
        }
        level.playSound(null, player.blockPosition(), ModSounds.ABILITY_SHARD_CALL.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
        player.displayClientMessage(Component.translatable("message.shardline.ability.shard_call"), true);
    }

    private static Vec3 spotNear(Player player, int index) {
        float angle = player.getYRot() * Mth.DEG_TO_RAD + Mth.PI + (index - 1) * 0.9F;
        Vec3 candidate = player.position().add(-Mth.sin(angle) * 2.5, 0.0, Mth.cos(angle) * 2.5);
        BlockPos pos = BlockPos.containing(candidate);
        for (int dy = 0; dy < 3; dy++) {
            BlockPos test = pos.above(dy);
            if (player.level().getBlockState(test).isAir() && player.level().getBlockState(test.above()).isAir()) {
                return new Vec3(candidate.x, test.getY(), candidate.z);
            }
        }
        return player.position();
    }

    public static void crystalWave(ServerPlayer player) {
        if (!checkReady(player, ModAttachments.CRYSTAL_WAVE_READY_AT, ShardlineConfig.CRYSTAL_WAVE_COOLDOWN_SECONDS.get(), "crystal_wave")) {
            return;
        }
        ServerLevel level = player.serverLevel();
        double radius = ShardlineConfig.CRYSTAL_WAVE_RADIUS.get();
        float damage = ShardlineConfig.CRYSTAL_WAVE_DAMAGE.get().floatValue();
        int fever = ShardlineConfig.CRYSTAL_WAVE_FEVER_SECONDS.get() * 20;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive() && !(entity instanceof Player)
                        && !(entity instanceof InfectedMob infected && player.getUUID().equals(infected.getOwnerUUID()))
                        && entity.distanceToSqr(player) <= radius * radius);
        for (LivingEntity target : targets) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.SHARD, player), damage);
            Vec3 push = target.position().subtract(player.position()).normalize().scale(1.1);
            target.push(push.x, 0.4, push.z);
            target.hurtMarked = true;
            VirusEffects.giveFever(target, fever);
        }
        for (int ring = 1; ring <= 3; ring++) {
            double r = radius * ring / 3.0;
            int points = 12 * ring;
            for (int i = 0; i < points; i++) {
                double angle = i * Math.PI * 2.0 / points;
                level.sendParticles(ModParticles.SHARD_SPARKLE.get(), player.getX() + Math.cos(angle) * r, player.getY() + 0.3,
                        player.getZ() + Math.sin(angle) * r, 1, 0.0, 0.05, 0.0, 0.0);
            }
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, player.blockPosition(), ModSounds.ABILITY_CRYSTAL_WAVE.get(), SoundSource.PLAYERS, 1.0F, 1.3F);
        player.displayClientMessage(Component.translatable("message.shardline.ability.crystal_wave", targets.size()), true);
    }

    /** Prueft Herrscher-Status und Abklingzeit und setzt diese bei Erfolg neu. */
    private static boolean checkReady(ServerPlayer player, Supplier<AttachmentType<Long>> attachment, int cooldownSeconds, String ability) {
        if (!StoryManager.isRuler(player)) {
            player.displayClientMessage(Component.translatable("message.shardline.ability.ruler_only"), true);
            return false;
        }
        long now = player.level().getGameTime();
        long readyAt = player.getData(attachment);
        if (now < readyAt && readyAt - now <= cooldownSeconds * 20L) {
            player.displayClientMessage(Component.translatable("message.shardline.ability.cooldown",
                    Component.translatable("key.shardline." + ability), (readyAt - now + 19) / 20), true);
            return false;
        }
        player.setData(attachment, now + cooldownSeconds * 20L);
        return true;
    }
}
