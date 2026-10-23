package eu.shardline.mod.item.custom;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.item.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Kolossfaust (Spezialitem des Kristallkolosses): Rechtsklick loest einen Bodenstampfer aus,
 * der Gegner im Umkreis verletzt und in die Luft schleudert.
 */
public class ColossusGauntletItem extends Item {
    private static final double RADIUS = 5.0;

    public ColossusGauntletItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 9.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(Shardline.id("gauntlet_knockback_resistance"), 0.2,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            slam(serverLevel, player);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            player.getCooldowns().addCooldown(this, ShardlineConfig.GAUNTLET_COOLDOWN_SECONDS.get() * 20);
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void slam(ServerLevel level, Player player) {
        float damage = ShardlineConfig.GAUNTLET_DAMAGE.get().floatValue();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS, 2.0, RADIUS),
                target -> target != player && target.isAlive() && !isAlly(player, target));
        for (LivingEntity target : targets) {
            double distance = target.distanceTo(player);
            if (distance > RADIUS) {
                continue;
            }
            float scaled = (float) (damage * (1.0 - distance / (RADIUS * 1.5)));
            target.hurt(level.damageSources().playerAttack(player), Math.max(1.0F, scaled));
            Vec3 push = target.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
            if (push.lengthSqr() > 1.0E-4) {
                push = push.normalize();
                target.knockback(1.2, -push.x, -push.z);
            }
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.55, 0.0));
            target.hurtMarked = true;
        }
        for (int i = 0; i < 36; i++) {
            double angle = i * Mth.TWO_PI / 36.0;
            double x = player.getX() + Math.cos(angle) * 2.5;
            double z = player.getZ() + Math.sin(angle) * 2.5;
            level.sendParticles(ModParticles.SHARD_SPARKLE.get(), x, player.getY() + 0.2, z, 2, 0.1, 0.1, 0.1, 0.05);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.3, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.GAUNTLET_SLAM.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
    }

    private static boolean isAlly(Player player, LivingEntity target) {
        if (target instanceof OwnableEntity ownable && player.getUUID().equals(ownable.getOwnerUUID())) {
            return true;
        }
        return target.isAlliedTo(player);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ModItems.COLOSSUS_CORE.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.colossus_gauntlet.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
