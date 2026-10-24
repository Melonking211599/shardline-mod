package eu.shardline.mod.story;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.network.ChapterCardPayload;
import eu.shardline.mod.network.StoryStatePayload;
import eu.shardline.mod.network.TitlesPayload;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Zentrale Story-Logik (nur Server): Kapitelfortschritt, Story-Advancements, Titel und
 * Synchronisierung des Story-Zustands zu den Clients.
 */
public final class StoryManager {
    public static final String KEEPER_TEAM = "shardline_keeper";
    public static final String RULER_TEAM = "shardline_ruler";

    private StoryManager() {
    }

    public static ShardlineWorldData data(MinecraftServer server) {
        return ShardlineWorldData.get(server);
    }

    public static int chapter(ServerPlayer player) {
        return data(player.server).chapter(player.getUUID());
    }

    /**
     * Hebt den Spieler mindestens auf das angegebene Kapitel. Uebersprungene Kapitel werden
     * mitvergeben, damit der Advancement-Baum vollstaendig bleibt.
     *
     * @return true, wenn sich das Kapitel geaendert hat
     */
    public static boolean reach(ServerPlayer player, StoryChapter chapter) {
        ShardlineWorldData data = data(player.server);
        int current = data.chapter(player.getUUID());
        if (current >= chapter.number()) {
            return false;
        }
        data.setChapter(player.getUUID(), chapter.number());
        for (int number = current + 1; number <= chapter.number(); number++) {
            StoryChapter passed = StoryChapter.byNumber(number);
            if (passed != null) {
                award(player, passed.advancementId());
            }
        }
        announceChapter(player, chapter);
        sync(player);
        return true;
    }

    /** Setzt das Kapitel exakt (Befehl). Bereits erhaltene Advancements bleiben bestehen. */
    public static void setChapter(ServerPlayer player, int chapter) {
        ShardlineWorldData data = data(player.server);
        data.setChapter(player.getUUID(), chapter);
        for (int number = 1; number <= chapter; number++) {
            StoryChapter passed = StoryChapter.byNumber(number);
            if (passed != null) {
                award(player, passed.advancementId());
            }
        }
        StoryChapter target = StoryChapter.byNumber(chapter);
        if (target != null) {
            announceChapter(player, target);
        }
        sync(player);
    }

    private static void announceChapter(ServerPlayer player, StoryChapter chapter) {
        if (ShardlineConfig.CHAPTER_TITLES.get()) {
            if (player.connection != null && player.connection.hasChannel(ChapterCardPayload.TYPE)) {
                send(player, new ChapterCardPayload(chapter.number()));
            } else {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 70, 25));
                player.connection.send(new ClientboundSetSubtitleTextPacket(chapter.title().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
                player.connection.send(new ClientboundSetTitleTextPacket(chapter.label().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)));
            }
        }
        player.sendSystemMessage(Component.empty()
                .append(Component.translatable("story.shardline.chapter_reached", chapter.label(), chapter.title())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .append(Component.literal(" "))
                .append(chapter.summary().copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        player.playNotifySound(ModSounds.STORY_CHAPTER.get(), SoundSource.MASTER, 0.8F, 1.0F);
    }

    /** Vergibt alle Kriterien eines Advancements aus {@code data/shardline/advancement/<id>.json}. */
    public static void award(ServerPlayer player, String id) {
        AdvancementHolder holder = player.server.getAdvancements().get(Shardline.id(id));
        if (holder == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    // ------------------------------------------------------------------ Synchronisierung

    /** Sendet ein Story-Paket nur an Clients, die den Kanal kennen (z. B. nicht an Test-Spieler). */
    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public static void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, payload);
        }
    }

    public static void sync(ServerPlayer player) {
        ShardlineWorldData data = data(player.server);
        send(player, new StoryStatePayload(data.chapter(player.getUUID()), data.isInfected(),
                isSpreadPaused(player.server), data.ending().id(), data.isRuler(player.getUUID())));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sync(player);
        }
    }

    public static void syncTitles(MinecraftServer server) {
        ShardlineWorldData data = data(server);
        sendToAll(server, new TitlesPayload(new ArrayList<>(data.keepers()), new ArrayList<>(data.rulers())));
    }

    public static void syncTitles(ServerPlayer player) {
        ShardlineWorldData data = data(player.server);
        send(player, new TitlesPayload(new ArrayList<>(data.keepers()), new ArrayList<>(data.rulers())));
    }

    // ------------------------------------------------------------------ Virus-Zustand

    /** Ob sich der Virus gerade ausbreitet (infiziert, nicht pausiert, nicht versiegelt). */
    public static boolean isVirusActive(MinecraftServer server) {
        if (!ShardlineConfig.VIRUS_ENABLED.get()) {
            return false;
        }
        ShardlineWorldData data = data(server);
        return data.isInfected() && data.ending() != StoryEnding.SEALED;
    }

    public static boolean isSpreadPaused(MinecraftServer server) {
        return ShardlineConfig.VIRUS_SPREAD_PAUSED.get() || data(server).isPaused();
    }

    /** Virus aktiv und Ausbreitung nicht pausiert. */
    public static boolean isSpreading(MinecraftServer server) {
        return isVirusActive(server) && !isSpreadPaused(server);
    }

    public static boolean isRuler(ServerPlayer player) {
        return data(player.server).isRuler(player.getUUID());
    }

    // ------------------------------------------------------------------ Titel (Scoreboard-Praefix)

    /** Stellt sicher, dass der Spieler im passenden Titel-Team steht (oder in keinem davon). */
    public static void applyTitleTeam(ServerPlayer player) {
        MinecraftServer server = player.server;
        Scoreboard scoreboard = server.getScoreboard();
        ShardlineWorldData data = data(server);
        String name = player.getScoreboardName();
        PlayerTeam current = scoreboard.getPlayersTeam(name);
        @Nullable PlayerTeam wanted = null;
        if (ShardlineConfig.TITLE_PREFIX.get()) {
            if (data.isKeeper(player.getUUID())) {
                wanted = team(scoreboard, KEEPER_TEAM, "title.shardline.keeper.prefix", ChatFormatting.GOLD);
            } else if (data.isRuler(player.getUUID())) {
                wanted = team(scoreboard, RULER_TEAM, "title.shardline.ruler.prefix", ChatFormatting.DARK_PURPLE);
            }
        }
        if (wanted != null) {
            if (current != wanted) {
                scoreboard.addPlayerToTeam(name, wanted);
            }
        } else if (current != null && (current.getName().equals(KEEPER_TEAM) || current.getName().equals(RULER_TEAM))) {
            scoreboard.removePlayerFromTeam(name, current);
        }
    }

    private static PlayerTeam team(Scoreboard scoreboard, String name, String prefixKey, ChatFormatting color) {
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) {
            team = scoreboard.addPlayerTeam(name);
        }
        team.setDisplayName(Component.translatable(prefixKey.replace(".prefix", "")));
        team.setPlayerPrefix(Component.translatable(prefixKey).withStyle(color));
        team.setColor(ChatFormatting.RESET);
        return team;
    }

    public static void grantKeeper(ServerPlayer player) {
        data(player.server).addKeeper(player.getUUID());
        applyTitleTeam(player);
        award(player, "story/ending_seal");
        syncTitles(player.server);
        sync(player);
    }

    public static void grantRuler(ServerPlayer player) {
        data(player.server).addRuler(player.getUUID());
        applyTitleTeam(player);
        award(player, "story/ending_dominate");
        player.sendSystemMessage(Component.translatable("message.shardline.ruler_allegiance").withStyle(ChatFormatting.LIGHT_PURPLE));
        syncTitles(player.server);
        sync(player);
    }

    @Nullable
    public static ServerPlayer onlinePlayer(MinecraftServer server, UUID uuid) {
        return server.getPlayerList().getPlayer(uuid);
    }
}
