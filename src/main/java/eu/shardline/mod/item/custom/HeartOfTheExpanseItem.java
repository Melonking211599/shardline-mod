package eu.shardline.mod.item.custom;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Herz der Splitterweite (Spezialitem der Splitterkoenigin): Rechtsklick teleportiert den Spieler
 * ein Stueck in Blickrichtung ("Void-Sprung") und schuetzt kurz vor Fallschaden.
 */
public class HeartOfTheExpanseItem extends Item {
    public HeartOfTheExpanseItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 target = findBlinkTarget(serverLevel, player);
            if (target == null) {
                return InteractionResultHolder.fail(stack);
            }
            Vec3 from = player.position();
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 40, 0.3, 0.8, 0.3, 0.1);
            player.teleportTo(target.x, target.y, target.z);
            player.resetFallDistance();
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, true));
            serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), target.x, target.y + 1.0, target.z, 40, 0.4, 0.8, 0.4, 0.1);
            serverLevel.playSound(null, target.x, target.y, target.z, ModSounds.HEART_BLINK.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
            player.getCooldowns().addCooldown(this, ShardlineConfig.HEART_COOLDOWN_SECONDS.get() * 20);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static Vec3 findBlinkTarget(ServerLevel level, Player player) {
        double distance = ShardlineConfig.HEART_BLINK_DISTANCE.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(distance));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 destination = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation().subtract(look.scale(0.6));
        // Fuesse auf Hoehe des Ziels, eventuell nach oben ausweichen, bis Platz fuer den Spieler ist.
        Vec3 feet = destination.subtract(0.0, player.getEyeHeight(), 0.0);
        for (int i = 0; i < 4; i++) {
            Vec3 candidate = feet.add(0.0, i, 0.0);
            if (level.noCollision(player, player.getDimensions(player.getPose()).makeBoundingBox(candidate))
                    && level.isInWorldBounds(BlockPos.containing(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.heart_of_the_expanse.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.shardline.heart_of_the_expanse.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
