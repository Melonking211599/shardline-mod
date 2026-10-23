package eu.shardline.mod.item.custom;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Splitterklinge der Koenigin: Treffer markieren und verlangsamen das Ziel,
 * Rechtsklick feuert eine Salve aus fuenf Kristallsplittern.
 */
public class QueensShardbladeItem extends SwordItem {
    private static final int VOLLEY_SIZE = 5;
    private static final float VOLLEY_DAMAGE = 6.0F;

    public QueensShardbladeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), attacker);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), target.getX(), target.getY(0.5), target.getZ(),
                    12, 0.3, 0.4, 0.3, 0.1);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle();
            for (int i = 0; i < VOLLEY_SIZE; i++) {
                float spread = (i - (VOLLEY_SIZE - 1) / 2.0F) * 7.5F;
                Vec3 direction = look.yRot(spread * ((float) Math.PI / 180F));
                ShardProjectileEntity.shoot(serverLevel, player, player.getX(), player.getEyeY() - 0.1, player.getZ(),
                        direction, 1.8F, 0.5F, VOLLEY_DAMAGE);
            }
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SHARDBLADE_VOLLEY.get(),
                    SoundSource.PLAYERS, 1.0F, 1.1F);
            stack.hurtAndBreak(2, player, LivingEntity.getSlotForHand(hand));
            player.getCooldowns().addCooldown(this, ShardlineConfig.SHARDBLADE_VOLLEY_COOLDOWN_SECONDS.get() * 20);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.queens_shardblade.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.shardline.queens_shardblade.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
