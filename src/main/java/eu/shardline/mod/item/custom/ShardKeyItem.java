package eu.shardline.mod.item.custom;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.world.portal.ShardPortalShape;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Shard-Schluessel: aktiviert einen Rahmen aus Splitterrahmen-Bloecken zu einem Splitterportal.
 */
public class ShardKeyItem extends Item {
    public ShardKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (!level.getBlockState(clicked).is(ModBlocks.SHARD_PORTAL_FRAME.get())) {
            return InteractionResult.PASS;
        }
        BlockPos inside = clicked.relative(context.getClickedFace());
        Optional<ShardPortalShape> shape = ShardPortalShape.findEmptyPortalShape(level, inside, Direction.Axis.X);
        Player player = context.getPlayer();
        if (shape.isEmpty()) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.translatable("message.shardline.portal_invalid"), true);
            }
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel serverLevel) {
            shape.get().createPortalBlocks();
            serverLevel.playSound(null, inside, ModSounds.SHARD_KEY_USE.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
            serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), inside.getX() + 0.5, inside.getY() + 1.0, inside.getZ() + 0.5,
                    50, 0.6, 1.0, 0.6, 0.1);
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.shard_key.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
