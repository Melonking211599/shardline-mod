package eu.shardline.mod.story.ending;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.network.OpenEndingChoicePayload;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.virus.VirusSpreadManager;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Endentscheidung am Thron der Splitterkoenigin.
 *
 * <p>Ein Spieler legt den Kern des Bruchs auf den Thron und waehlt "Versiegeln" oder "Beherrschen".
 * Je nach Config entscheidet er allein ({@code FIRST_PLAYER}) oder er eroeffnet eine Abstimmung
 * unter allen Spielern, die Kapitel 6 erreicht haben ({@code VOTE}). Bei Gleichstand gewinnt
 * "Versiegeln". Spieler, die spaeter einen Kern auf den Thron legen, schliessen sich dem
 * gewaehlten Weg an und erhalten dessen Titel.</p>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class EndingManager {
    /** Wie weit ein Spieler vom Thron entfernt sein darf, wenn seine Wahl eintrifft. */
    private static final double ALTAR_REACH_SQR = 10.0 * 10.0;

    /** Spieler, die gerade vor der Wahl stehen (Kern liegt noch bei ihnen). */
    private static final Map<UUID, PendingChoice> PENDING = new HashMap<>();
    @Nullable
    private static Vote vote;

    private EndingManager() {
    }

    private record PendingChoice(ResourceKey<Level> dimension, BlockPos altar) {
    }

    private static final class Vote {
        final UUID initiator;
        final Set<UUID> eligible = new LinkedHashSet<>();
        final Map<UUID, StoryEnding> votes = new LinkedHashMap<>();
        final long endTick;
        boolean warned;

        Vote(UUID initiator, long endTick) {
            this.initiator = initiator;
            this.endTick = endTick;
        }
    }

    public static boolean isVoteRunning() {
        return vote != null;
    }

    // ------------------------------------------------------------------ Kern am Thron

    /** Ein Spieler benutzt den Kern des Bruchs am Thron. */
    public static void onCoreUsed(ServerLevel level, BlockPos altar, ServerPlayer player, ItemStack core) {
        ShardlineWorldData data = StoryManager.data(level.getServer());
        if (data.ending() != StoryEnding.NONE) {
            core.consume(1, player);
            join(player, data.ending());
            return;
        }
        if (data.chapter(player.getUUID()) < StoryChapter.CONTAINMENT.number()) {
            player.displayClientMessage(Component.translatable("message.shardline.ending.not_ready"), true);
            return;
        }
        if (vote != null) {
            int seconds = secondsLeft(level.getServer());
            StoryManager.send(player, new OpenEndingChoicePayload(true, seconds));
            return;
        }
        PENDING.put(player.getUUID(), new PendingChoice(level.dimension(), altar.immutable()));
        boolean voteMode = ShardlineConfig.ENDING_MODE.get() == ShardlineConfig.EndingMode.VOTE;
        StoryManager.send(player, new OpenEndingChoicePayload(voteMode, voteMode ? ShardlineConfig.VOTE_DURATION_SECONDS.get() : 0));
    }

    /** Wahl aus dem Auswahlfenster oder per {@code /shardline story vote}. */
    public static void onChoice(ServerPlayer player, StoryEnding choice) {
        if (choice == StoryEnding.NONE) {
            return;
        }
        MinecraftServer server = player.server;
        ShardlineWorldData data = StoryManager.data(server);
        if (data.ending() != StoryEnding.NONE) {
            player.displayClientMessage(Component.translatable("message.shardline.ending.already_decided", data.ending().title()), true);
            return;
        }
        if (vote != null) {
            castVote(server, player, choice);
            return;
        }
        PendingChoice pending = PENDING.remove(player.getUUID());
        if (pending == null || player.level().dimension() != pending.dimension()
                || player.distanceToSqr(pending.altar().getCenter()) > ALTAR_REACH_SQR) {
            player.displayClientMessage(Component.translatable("message.shardline.ending.no_core"), true);
            return;
        }
        if (!consumeCore(player)) {
            player.displayClientMessage(Component.translatable("message.shardline.ending.no_core"), true);
            return;
        }
        if (ShardlineConfig.ENDING_MODE.get() == ShardlineConfig.EndingMode.FIRST_PLAYER) {
            resolve(server, choice, Set.of(player.getUUID()));
            return;
        }
        startVote(server, player, choice);
    }

    private static boolean consumeCore(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ModItems.BREACH_CORE.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ Abstimmung

    private static void startVote(MinecraftServer server, ServerPlayer initiator, StoryEnding choice) {
        int duration = ShardlineConfig.VOTE_DURATION_SECONDS.get();
        Vote started = new Vote(initiator.getUUID(), server.getTickCount() + duration * 20L);
        ShardlineWorldData data = StoryManager.data(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (data.chapter(player.getUUID()) >= StoryChapter.CONTAINMENT.number()) {
                started.eligible.add(player.getUUID());
            }
        }
        started.eligible.add(initiator.getUUID());
        vote = started;

        Component announcement = Component.translatable("message.shardline.vote.started", initiator.getDisplayName(), duration)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(announcement);
            if (started.eligible.contains(player.getUUID()) && !player.getUUID().equals(initiator.getUUID())) {
                player.sendSystemMessage(voteButtons());
                StoryManager.send(player, new OpenEndingChoicePayload(true, duration));
            }
        }
        castVote(server, initiator, choice);
    }

    private static Component voteButtons() {
        MutableComponent seal = Component.translatable("message.shardline.vote.button.seal").withStyle(Style.EMPTY
                .withColor(ChatFormatting.GOLD).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shardline story vote seal"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("story.shardline.ending.seal.description"))));
        MutableComponent dominate = Component.translatable("message.shardline.vote.button.dominate").withStyle(Style.EMPTY
                .withColor(ChatFormatting.DARK_PURPLE).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shardline story vote dominate"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("story.shardline.ending.dominate.description"))));
        return Component.empty().append(seal).append(Component.literal("   ")).append(dominate);
    }

    private static void castVote(MinecraftServer server, ServerPlayer player, StoryEnding choice) {
        Vote current = vote;
        if (current == null) {
            return;
        }
        if (!current.eligible.contains(player.getUUID())) {
            if (StoryManager.data(server).chapter(player.getUUID()) < StoryChapter.CONTAINMENT.number()) {
                player.displayClientMessage(Component.translatable("message.shardline.vote.not_eligible"), true);
                return;
            }
            current.eligible.add(player.getUUID());
        }
        current.votes.put(player.getUUID(), choice);
        player.displayClientMessage(Component.translatable("message.shardline.vote.cast", choice.title()), true);
        Component progress = Component.translatable("message.shardline.vote.progress", player.getDisplayName(), current.votes.size(),
                current.eligible.size()).withStyle(ChatFormatting.GRAY);
        for (UUID uuid : current.eligible) {
            ServerPlayer other = server.getPlayerList().getPlayer(uuid);
            if (other != null) {
                other.sendSystemMessage(progress);
            }
        }
        if (current.votes.keySet().containsAll(onlineEligible(server, current))) {
            finishVote(server);
        }
    }

    private static Set<UUID> onlineEligible(MinecraftServer server, Vote current) {
        Set<UUID> online = new LinkedHashSet<>();
        for (UUID uuid : current.eligible) {
            if (server.getPlayerList().getPlayer(uuid) != null) {
                online.add(uuid);
            }
        }
        return online;
    }

    private static void finishVote(MinecraftServer server) {
        Vote current = vote;
        vote = null;
        if (current == null) {
            return;
        }
        int seal = 0;
        int dominate = 0;
        for (StoryEnding choice : current.votes.values()) {
            if (choice == StoryEnding.SEALED) {
                seal++;
            } else if (choice == StoryEnding.DOMINATED) {
                dominate++;
            }
        }
        StoryEnding result = dominate > seal ? StoryEnding.DOMINATED : StoryEnding.SEALED;
        Set<UUID> winners = new LinkedHashSet<>();
        current.votes.forEach((uuid, choice) -> {
            if (choice == result) {
                winners.add(uuid);
            }
        });
        Component summary = Component.translatable("message.shardline.vote.result", seal, dominate, result.title())
                .withStyle(ChatFormatting.GOLD);
        server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(summary));
        resolve(server, result, winners);
    }

    private static int secondsLeft(MinecraftServer server) {
        return vote == null ? 0 : (int) Math.max(0L, (vote.endTick - server.getTickCount()) / 20L);
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        Vote current = vote;
        if (current == null) {
            return;
        }
        MinecraftServer server = event.getServer();
        long left = current.endTick - server.getTickCount();
        if (!current.warned && left <= 20L * 15L) {
            current.warned = true;
            Component warning = Component.translatable("message.shardline.vote.ending_soon").withStyle(ChatFormatting.YELLOW);
            for (UUID uuid : current.eligible) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null && !current.votes.containsKey(uuid)) {
                    player.sendSystemMessage(warning);
                    player.sendSystemMessage(voteButtons());
                }
            }
        }
        if (left <= 0L) {
            finishVote(server);
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        vote = null;
        PENDING.clear();
    }

    // ------------------------------------------------------------------ Aufloesung

    /** Setzt das Ende der Geschichte und vergibt die Titel. */
    public static void resolve(MinecraftServer server, StoryEnding ending, Set<UUID> winners) {
        ShardlineWorldData data = StoryManager.data(server);
        data.setEnding(ending);
        for (UUID uuid : winners) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                if (ending == StoryEnding.SEALED) {
                    StoryManager.grantKeeper(player);
                } else {
                    StoryManager.grantRuler(player);
                }
            } else if (ending == StoryEnding.SEALED) {
                data.addKeeper(uuid);
            } else {
                data.addRuler(uuid);
            }
        }
        if (ending == StoryEnding.SEALED) {
            VirusSpreadManager.clearQueues();
        }

        boolean sealed = ending == StoryEnding.SEALED;
        Component title = Component.translatable(sealed ? "message.shardline.ending.sealed.title" : "message.shardline.ending.dominated.title")
                .withStyle(sealed ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        Component subtitle = Component.translatable(sealed ? "message.shardline.ending.sealed.subtitle" : "message.shardline.ending.dominated.subtitle")
                .withStyle(sealed ? ChatFormatting.YELLOW : ChatFormatting.LIGHT_PURPLE);
        Component broadcast = Component.translatable(sealed ? "message.shardline.ending.sealed.broadcast" : "message.shardline.ending.dominated.broadcast")
                .withStyle(sealed ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(broadcast);
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 90, 40));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.playNotifySound(sealed ? ModSounds.VIRUS_SEALED.get() : ModSounds.VIRUS_DOMINATED.get(), SoundSource.MASTER, 1.0F, 1.0F);
        }
        StoryManager.sendToAll(server, new ScreenEffectPayload(sealed ? ScreenEffectPayload.SEALED : ScreenEffectPayload.DOMINATED, 120));
        StoryManager.syncAll(server);
        StoryManager.syncTitles(server);
    }

    /** Spaeterer Kern am Thron: dem bereits gewaehlten Weg beitreten. */
    private static void join(ServerPlayer player, StoryEnding ending) {
        if (ending == StoryEnding.SEALED) {
            StoryManager.grantKeeper(player);
        } else {
            StoryManager.grantRuler(player);
        }
        player.sendSystemMessage(Component.translatable(ending == StoryEnding.SEALED ? "message.shardline.ending.join_sealed"
                : "message.shardline.ending.join_dominated").withStyle(ending == StoryEnding.SEALED ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE));
        StoryManager.send(player, new ScreenEffectPayload(ending == StoryEnding.SEALED ? ScreenEffectPayload.SEALED
                : ScreenEffectPayload.DOMINATED, 60));
    }

    /** Fuer Admins: Ende zuruecksetzen (Titel bleiben, bis sie per Befehl entfernt werden). */
    public static void reset(MinecraftServer server) {
        vote = null;
        PENDING.clear();
        ShardlineWorldData data = StoryManager.data(server);
        data.setEnding(StoryEnding.NONE);
        data.clearTitles();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            StoryManager.applyTitleTeam(player);
        }
        StoryManager.syncAll(server);
        StoryManager.syncTitles(server);
    }
}
