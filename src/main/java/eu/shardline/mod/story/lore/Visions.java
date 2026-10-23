package eu.shardline.mod.story.lore;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.StoryScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * Visionen der Erinnerungskristalle. Jede Vision hat Titel, Untertitel und drei Chatzeilen
 * ({@code vision.shardline.<n>.title}, {@code .subtitle}, {@code .line.<1-3>}).
 */
public final class Visions {
    public static final int COUNT = 8;
    private static final int LINES = 3;

    /** Visionen, die in Ruinen-Tuermen vorkommen koennen. */
    public static final int[] RUIN_VISIONS = {0, 1, 2, 4, 6};
    public static final int COLOSSUS_VISION = 5;
    public static final int QUEEN_VISION = 3;
    public static final int WARNING_VISION = 7;

    private Visions() {
    }

    public static void show(ServerPlayer player, ServerLevel level, BlockPos pos, int vision) {
        String key = "vision.shardline." + vision;
        player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 80, 30));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(key + ".subtitle").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key + ".title").withStyle(ChatFormatting.LIGHT_PURPLE)));
        StoryManager.send(player, new ScreenEffectPayload(ScreenEffectPayload.VISION, 80));

        int delay = Math.max(20, ShardlineConfig.DIALOGUE_LINE_DELAY_TICKS.get());
        for (int line = 1; line <= LINES; line++) {
            Component text = Component.translatable(key + ".line." + line).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC);
            StoryScheduler.schedule(30 + delay * (line - 1), () -> {
                if (!player.hasDisconnected()) {
                    player.sendSystemMessage(text);
                }
            });
        }

        level.playSound(null, pos, ModSounds.MEMORY_CRYSTAL_VISION.get(), SoundSource.BLOCKS, 1.0F, 0.8F + vision * 0.05F);
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        for (int i = 0; i < 48; i++) {
            float angle = i * Mth.TWO_PI / 16.0F;
            double radius = 1.2 - i * 0.02;
            level.sendParticles(ModParticles.SHARD_SPARKLE.get(), cx + Mth.cos(angle) * radius, cy + i * 0.06, cz + Mth.sin(angle) * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.END_ROD, cx, cy + 1.0, cz, 30, 0.3, 0.8, 0.3, 0.02);
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getEyeY(), player.getZ(), 60, 0.5, 0.5, 0.5, 0.8);

        StoryEvents.onVisionSeen(player, vision);
    }
}
