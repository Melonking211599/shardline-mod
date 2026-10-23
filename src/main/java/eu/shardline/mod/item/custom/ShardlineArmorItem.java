package eu.shardline.mod.item.custom;

import eu.shardline.mod.config.ShardlineConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Ruestungsteil eines Shardline-Sets. Zeigt den Set-Bonus im Tooltip an; die Wirkung selbst
 * liegt in {@link eu.shardline.mod.event.SetBonusEvents}.
 */
public class ShardlineArmorItem extends ArmorItem {
    public enum SetType {
        SHARD, COLOSSUS
    }

    private final SetType setType;

    public ShardlineArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, SetType setType) {
        super(material, type, properties);
        this.setType = setType;
    }

    public SetType getSetType() {
        return this.setType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.shardline.set_bonus").withStyle(ChatFormatting.GOLD));
        if (this.setType == SetType.SHARD) {
            tooltip.add(Component.translatable("tooltip.shardline.shard_set.night_vision").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.shardline.shard_set.resistance",
                    Math.round(safeResistance() * 100)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.shardline.colossus_set.knockback").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.shardline.colossus_set.hearts", safeBonusHearts()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.shardline.colossus_set.shield",
                    Component.keybind("key.shardline.crystal_shield")).withStyle(ChatFormatting.GRAY));
        }
    }

    private static double safeResistance() {
        return ShardlineConfig.SPEC.isLoaded() ? ShardlineConfig.SHARD_PROJECTILE_RESISTANCE.get() : 0.5;
    }

    private static int safeBonusHearts() {
        return ShardlineConfig.SPEC.isLoaded() ? ShardlineConfig.COLOSSUS_BONUS_HEARTS.get() : 4;
    }
}
