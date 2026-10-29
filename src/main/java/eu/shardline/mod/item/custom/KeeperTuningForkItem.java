package eu.shardline.mod.item.custom;

import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.StoryManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Stimmgabel der Hueter. Rechtsklick in die Luft: horcht nach der naechsten Schwaere (bis 96 Bloecke, aus der
 * gespeicherten Liste, ohne Weltsuche) und nennt Richtung und Entfernung; ein Klang zeigt die Richtung als Partikelspur.
 * Rechtsklick auf eine Schwaere bringt sie zum Schweigen (siehe FesteringNodeBlock), das kostet Haltbarkeit.
 */
public class KeeperTuningForkItem extends Item {
    public static final int RANGE = 96;
    private static final String[] DIRECTIONS = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};

    public KeeperTuningForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            listen(serverLevel, serverPlayer);
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Naechste Schwaere in Reichweite oder null. */
    public static BlockPos nearestNode(ServerLevel level, BlockPos from) {
        BlockPos best = null;
        double bestDistance = (double) RANGE * RANGE;
        for (long packed : StoryManager.data(level.getServer()).festeringNodes(level.dimension())) {
            BlockPos pos = BlockPos.of(packed);
            double distance = pos.distSqr(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos;
            }
        }
        return best;
    }

    private static void listen(ServerLevel level, ServerPlayer player) {
        BlockPos node = nearestNode(level, player.blockPosition());
        if (node == null) {
            level.playSound(null, player.blockPosition(), ModSounds.TUNING_FORK_RING.get(), SoundSource.PLAYERS, 0.8F, 1.6F);
            player.displayClientMessage(Component.translatable("message.shardline.tuning_fork.silent").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        Vec3 delta = Vec3.atCenterOf(node).subtract(player.position());
        int distance = (int) Math.round(delta.horizontalDistance());
        float yaw = (float) (Mth.atan2(-delta.x, delta.z) * Mth.RAD_TO_DEG);
        String direction = DIRECTIONS[Math.floorMod(Math.round(yaw / 45.0F), 8)];
        level.playSound(null, player.blockPosition(), ModSounds.TUNING_FORK_RING.get(), SoundSource.PLAYERS, 1.0F,
                0.6F + 0.9F * (1.0F - Math.min(1.0F, distance / (float) RANGE)));
        Vec3 step = delta.normalize();
        for (int i = 1; i <= 8; i++) {
            Vec3 point = player.getEyePosition().add(step.scale(i * 0.6));
            level.sendParticles(player, ParticleTypes.NOTE, true, point.x, point.y - 0.3, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        player.displayClientMessage(Component.translatable("message.shardline.tuning_fork.direction",
                Component.translatable("message.shardline.direction." + direction), distance).withStyle(ChatFormatting.AQUA), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.keeper_tuning_fork.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
