package eu.shardline.mod.item.custom;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.effect.ShardFeverEffect;
import eu.shardline.mod.entity.infected.CrystallizedVillagerEntity;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.VirusEffects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Reinigungsessenz (aus dem Herz der Splitterweite): reinigt infizierte Bloecke im Umkreis,
 * heilt Splitterfieber und verwandelt infizierte Wesen zurueck (Kuh, Schaf, Schwein, Zombie; ein
 * kristallisierter Dorfbewohner erhaelt seinen Beruf zurueck).
 * Rechtsklick in die Luft wirkt um den Spieler, auf einen Block um diesen Block.
 */
public class PurificationEssenceItem extends Item {
    public PurificationEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            this.purify(serverLevel, serverPlayer, player.blockPosition(), stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel && context.getPlayer() instanceof ServerPlayer player) {
            this.purify(serverLevel, player, context.getClickedPos(), context.getItemInHand());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel level)) {
            return VirusEffects.isCurable(target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!VirusEffects.isCurable(target)) {
            return InteractionResult.PASS;
        }
        boolean villager = target instanceof CrystallizedVillagerEntity;
        if (VirusEffects.purifyMob(level, target) == null) {
            return InteractionResult.PASS;
        }
        if (villager && player instanceof ServerPlayer serverPlayer) {
            StoryManager.award(serverPlayer, "story/cure_villager");
        }
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, target.blockPosition(), ModSounds.PURIFICATION_ESSENCE_USE.get(), SoundSource.PLAYERS, 1.0F, 1.4F);
        stack.consume(1, player);
        return InteractionResult.CONSUME;
    }

    private void purify(ServerLevel level, ServerPlayer player, BlockPos center, ItemStack stack) {
        int radius = ShardlineConfig.ESSENCE_RADIUS.get();
        Purification.purifySphere(level, center, radius, purified -> player.displayClientMessage(
                Component.translatable("message.shardline.purified", purified).withStyle(ChatFormatting.AQUA), true));
        int immunity = ShardlineConfig.IMMUNITY_ESSENCE_SECONDS.get() * 20;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(radius))) {
            entity.removeEffectsCuredBy(ShardFeverEffect.PURIFICATION);
            VirusEffects.giveImmunity(entity, immunity, false);
        }
        player.removeEffectsCuredBy(ShardFeverEffect.PURIFICATION);
        VirusEffects.giveImmunity(player, immunity, false);

        level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 80, radius * 0.4, 1.0, radius * 0.4, 0.05);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 40, radius * 0.5, 0.5, radius * 0.5, 0.0);
        level.playSound(null, center, ModSounds.PURIFICATION_ESSENCE_USE.get(), SoundSource.PLAYERS, 1.2F, 1.2F);
        StoryManager.send(player, new ScreenEffectPayload(ScreenEffectPayload.PURIFY, 30));

        StoryManager.award(player, "story/purification");
        StoryEvents.onContainmentAction(player);
        stack.consume(1, player);
        player.getCooldowns().addCooldown(this, 20);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.purification_essence.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.shardline.purification_essence.tooltip2").withStyle(ChatFormatting.GRAY));
        int seconds = ShardlineConfig.IMMUNITY_ESSENCE_SECONDS.get();
        if (seconds > 0) {
            tooltip.add(Component.translatable("item.shardline.purification_essence.tooltip_immunity",
                    String.format("%d:%02d", seconds / 60, seconds % 60)).withStyle(ChatFormatting.GOLD));
        }
    }
}
