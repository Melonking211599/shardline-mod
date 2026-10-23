package eu.shardline.mod.event;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModAttachments;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.item.custom.ShardlineArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Faehigkeit "Kristallschild" des Koloss-Sets: Absorptionsherzen fuer einige Sekunden plus eine
 * Rueckstosswelle. Ausgeloest per Tastendruck auf dem Client, ausgefuehrt auf dem Server.
 */
public final class CrystalShieldHandler {
    private CrystalShieldHandler() {
    }

    public static void tryActivate(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }
        if (!SetBonusEvents.wearsFullSet(player, ShardlineArmorItem.SetType.COLOSSUS)) {
            player.displayClientMessage(Component.translatable("message.shardline.crystal_shield.no_set").withStyle(ChatFormatting.RED), true);
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        long readyAt = player.getData(ModAttachments.CRYSTAL_SHIELD_READY_AT);
        if (now < readyAt) {
            int seconds = Mth.ceil((readyAt - now) / 20.0);
            player.displayClientMessage(Component.translatable("message.shardline.crystal_shield.cooldown", seconds)
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        int durationTicks = ShardlineConfig.SHIELD_DURATION_SECONDS.get() * 20;
        int hearts = ShardlineConfig.SHIELD_ABSORPTION_HEARTS.get();
        // Absorption Stufe n gibt 4*(n+1) Lebenspunkte = 2*(n+1) Herzen.
        int amplifier = Math.max(0, Mth.ceil(hearts / 2.0) - 1);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, durationTicks, amplifier, false, true, true));

        double radius = ShardlineConfig.SHIELD_PUSH_RADIUS.get();
        if (radius > 0.0) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                    target -> target != player && target.isAlive() && !isFriendly(player, target))) {
                Vec3 push = target.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
                if (push.lengthSqr() > 1.0E-4) {
                    push = push.normalize();
                    target.knockback(1.4, -push.x, -push.z);
                    target.hurtMarked = true;
                }
            }
        }
        for (int i = 0; i < 40; i++) {
            double angle = i * Mth.TWO_PI / 40.0;
            level.sendParticles(ModParticles.SHARD_SPARKLE.get(), player.getX() + Math.cos(angle) * 1.2, player.getY() + 1.0,
                    player.getZ() + Math.sin(angle) * 1.2, 2, 0.05, 0.6, 0.05, 0.02);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CRYSTAL_SHIELD_ACTIVATE.get(), SoundSource.PLAYERS, 1.2F, 1.0F);

        player.setData(ModAttachments.CRYSTAL_SHIELD_READY_AT, now + ShardlineConfig.SHIELD_COOLDOWN_SECONDS.get() * 20L);
        player.displayClientMessage(Component.translatable("message.shardline.crystal_shield.activated").withStyle(ChatFormatting.AQUA), true);
    }

    private static boolean isFriendly(Player player, LivingEntity target) {
        return target instanceof OwnableEntity ownable && player.getUUID().equals(ownable.getOwnerUUID()) || target.isAlliedTo(player);
    }
}
