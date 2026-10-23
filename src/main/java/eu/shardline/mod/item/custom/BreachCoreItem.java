package eu.shardline.mod.item.custom;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Kern des Bruchs: Belohnung fuer drei besiegte Virusherzen. Auf den Thron der Splitterkoenigin
 * gelegt, entscheidet er ueber das Ende der Geschichte (siehe {@code story.ending.EndingManager}).
 */
public class BreachCoreItem extends Item {
    public BreachCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.breach_core.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.shardline.breach_core.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
