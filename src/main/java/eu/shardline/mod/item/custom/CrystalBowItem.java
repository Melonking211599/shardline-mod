package eu.shardline.mod.item.custom;

import eu.shardline.mod.item.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Kristallbogen: Pfeile verursachen mehr Schaden, fliegen weiter und lassen Getroffene leuchten.
 */
public class CrystalBowItem extends BowItem {
    private static final double BONUS_DAMAGE = 1.5;

    public CrystalBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setBaseDamage(arrow.getBaseDamage() + BONUS_DAMAGE);
        if (arrow instanceof Arrow plainArrow) {
            plainArrow.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100));
        }
        return arrow;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 20;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(ModItems.SHARD_INGOT.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.crystal_bow.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
