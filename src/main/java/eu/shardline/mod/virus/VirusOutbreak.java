package eu.shardline.mod.virus;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;

/**
 * Der Bruch: Mit dem Tod der Splitterkoenigin zerbricht das Siegel und der Shardvirus wird frei.
 * Loest die globalen Effekte aus (Chat, Titel, Bildschirmblitz, Kamerawackeln, violetter Himmel)
 * und setzt den Weltzustand auf "infiziert".
 */
public final class VirusOutbreak {
    /** Spieler in diesem Umkreis erleben den Bruch direkt (Kapitel 5). */
    private static final double WITNESS_RADIUS = 64.0;

    private VirusOutbreak() {
    }

    /**
     * @param killer Spieler, der die Koenigin besiegt hat (darf fehlen, z. B. bei /kill)
     */
    public static void onQueenDeath(ServerLevel level, BlockPos pos, @Nullable ServerPlayer killer) {
        MinecraftServer server = level.getServer();
        ShardlineWorldData data = StoryManager.data(server);
        if (data.ending() == StoryEnding.SEALED) {
            Component held = Component.translatable("message.shardline.seal_holds").withStyle(ChatFormatting.GOLD);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(WITNESS_RADIUS))) {
                player.sendSystemMessage(held);
            }
            return;
        }
        if (!ShardlineConfig.VIRUS_ENABLED.get()) {
            return;
        }
        trigger(level, pos, killer);
    }

    /** Startet den Ausbruch (auch per Befehl). Bei bereits infizierter Welt nur neue Infektionsstellen. */
    public static void trigger(ServerLevel level, BlockPos pos, @Nullable ServerPlayer killer) {
        MinecraftServer server = level.getServer();
        ShardlineWorldData data = StoryManager.data(server);
        boolean first = !data.isInfected();
        if (first) {
            data.setInfected(true, level.getGameTime());
            Component name = killer != null ? killer.getDisplayName() : Component.translatable("message.shardline.someone");
            Component broadcast = Component.translatable("message.shardline.outbreak.broadcast", name)
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
            Component warning = Component.translatable("message.shardline.outbreak.warning").withStyle(ChatFormatting.LIGHT_PURPLE);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(broadcast);
                player.sendSystemMessage(warning);
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 40));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("message.shardline.outbreak.subtitle")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.shardline.outbreak.title")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)));
                player.playNotifySound(ModSounds.VIRUS_OUTBREAK.get(), SoundSource.MASTER, 1.0F, 0.7F);
            }
            StoryManager.sendToAll(server, new ScreenEffectPayload(ScreenEffectPayload.OUTBREAK, 140));
        }

        data.addHerd(level.dimension(), pos, 1);
        VirusSpreadManager.seedAround(level, pos, 24, 160, level.getRandom());
        if (first) {
            // Der Ton kriecht durch die Portale: erste Herde in der Oberwelt, nahe den Spielern und am Weltspawn.
            ServerLevel overworld = server.overworld();
            for (ServerPlayer player : overworld.players()) {
                VirusDrift.seedNear(overworld, player.blockPosition(), overworld.getRandom());
            }
            VirusDrift.seedNear(overworld, overworld.getSharedSpawnPos(), overworld.getRandom());
        }

        if (killer != null) {
            StoryManager.reach(killer, StoryChapter.THE_BREACH);
        }
        for (ServerPlayer witness : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(WITNESS_RADIUS))) {
            StoryManager.reach(witness, StoryChapter.THE_BREACH);
        }
        StoryManager.syncAll(server);
    }
}
