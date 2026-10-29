package eu.shardline.mod.story;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.custom.DecisionAltarBlock;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.entity.boss.CrystalColossusEntity;
import eu.shardline.mod.entity.boss.ShardQueenEntity;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.story.lore.LoreBooks;
import eu.shardline.mod.story.lore.Visions;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.VirusOutbreak;
import eu.shardline.mod.world.dimension.ModDimensions;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Story-Ausloeser: welche Spielereignisse welches Kapitel erreichen und welche Story-Advancements
 * vergeben. Alles laeuft auf dem Server.
 *
 * <ol>
 *   <li>Der erste Splitter - Shard-Splitter oder Roh-Shard aufheben bzw. Shard-Barren herstellen</li>
 *   <li>Das Tor - die Splitterweite betreten</li>
 *   <li>Echos der Hueter - ein Lore-Buch lesen oder eine Vision sehen</li>
 *   <li>Das Herz der Weite - den Kristallkoloss besiegen</li>
 *   <li>Der Bruch - die Koenigin faellt (oder Kontakt mit dem Virus)</li>
 *   <li>Eindaemmung - Reinheitsanker, Reinigungsessenz oder ein besiegtes Virusherz</li>
 * </ol>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class StoryEvents {
    private static final double BOSS_CREDIT_RADIUS = 48.0;

    private StoryEvents() {
    }

    // ------------------------------------------------------------------ Login & Synchronisierung

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StoryManager.applyTitleTeam(player);
            StoryManager.sync(player);
            StoryManager.syncTitles(player);
            StoryManager.checkAftermath(player);
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StoryManager.sync(player);
        }
    }

    // ------------------------------------------------------------------ Kapitel 1 & 2

    @SubscribeEvent
    static void onPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player && isShard(event.getOriginalStack())) {
            StoryManager.reach(player, StoryChapter.FIRST_SHARD);
            // Eigener Klang fuer Shard-Splitter, hoechstens alle 4 Ticks (beim Einsammeln vieler Stapel)
            if (event.getOriginalStack().is(ModItems.SHARD_FRAGMENT.get()) && player.tickCount - player.getPersistentData().getInt(PICKUP_TICK) >= 4) {
                player.getPersistentData().putInt(PICKUP_TICK, player.tickCount);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SHARD_FRAGMENT_PICKUP.get(),
                        SoundSource.PLAYERS, 0.5F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
        }
    }

    private static final String PICKUP_TICK = "ShardlineShardPickupTick";

    @SubscribeEvent
    static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getCrafting().is(ModItems.SHARD_INGOT.get())) {
            StoryManager.reach(player, StoryChapter.FIRST_SHARD);
        }
    }

    @SubscribeEvent
    static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSmelting().is(ModItems.SHARD_INGOT.get())) {
            StoryManager.reach(player, StoryChapter.FIRST_SHARD);
        }
    }

    private static boolean isShard(ItemStack stack) {
        return stack.is(ModItems.SHARD_FRAGMENT.get()) || stack.is(ModItems.RAW_SHARD.get());
    }

    @SubscribeEvent
    static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getTo() == ModDimensions.SPLITTERWEITE_LEVEL) {
                StoryManager.reach(player, StoryChapter.THE_GATE);
            }
            StoryManager.checkAftermath(player);
        }
    }

    // ------------------------------------------------------------------ Kapitel 3: Lore & Visionen

    public static void onLoreBookRead(ServerPlayer player, LoreBooks.LoreBook book) {
        ShardlineWorldData data = StoryManager.data(player.server);
        if (data.addFlag(player.getUUID(), "book:" + book.id())) {
            StoryManager.award(player, "story/lore_book");
            eu.shardline.mod.story.quest.Quests.trigger(player, "read_lore", target -> target.equals("any") || target.equals(book.id()), 1);
            int read = data.countFlags(player.getUUID(), "book:");
            player.displayClientMessage(Component.translatable("message.shardline.lore_book_found", read, LoreBooks.ALL.size())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            if (read >= LoreBooks.ALL.size()) {
                StoryManager.award(player, "story/all_lore_books");
            }
        }
        if (data.chapter(player.getUUID()) >= StoryChapter.THE_GATE.number()) {
            StoryManager.reach(player, StoryChapter.ECHOES_OF_THE_KEEPERS);
        }
    }

    public static void onVisionSeen(ServerPlayer player, int vision) {
        ShardlineWorldData data = StoryManager.data(player.server);
        if (data.addFlag(player.getUUID(), "vision:" + vision)) {
            StoryManager.award(player, "story/vision");
            if (data.countFlags(player.getUUID(), "vision:") >= Visions.COUNT) {
                StoryManager.award(player, "story/all_visions");
            }
        }
        StoryManager.reach(player, StoryChapter.ECHOES_OF_THE_KEEPERS);
    }

    // ------------------------------------------------------------------ Kapitel 4 & 5: Bosse

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player : null;
        if (entity instanceof CrystalColossusEntity) {
            for (ServerPlayer player : creditedPlayers(level, entity, killer, BOSS_CREDIT_RADIUS)) {
                StoryManager.reach(player, StoryChapter.HEART_OF_THE_EXPANSE);
            }
        } else if (entity instanceof eu.shardline.mod.entity.boss.EchoQueenEntity) {
            Encounters.onBossDefeated(level, entity, killer, Encounters.ECHO_QUEEN);
        } else if (entity instanceof ShardQueenEntity queen) {
            VirusOutbreak.onQueenDeath(level, entity.blockPosition(), killer);
            if (StoryManager.data(level.getServer()).ending() == StoryEnding.NONE) {
                DecisionAltarBlock.raise(level, queen.getHome(), entity.blockPosition());
            }
        } else if (entity instanceof InfectedMob && killer != null && StoryManager.data(level.getServer()).isInfected()) {
            StoryManager.reach(killer, StoryChapter.THE_BREACH);
        }
    }

    // ------------------------------------------------------------------ Kapitel 6: Eindaemmung

    /** Wer gegen den Virus vorgeht (Anker, Essenz, Virusherz), erreicht Kapitel 6. */
    public static void onContainmentAction(ServerPlayer player) {
        if (StoryManager.data(player.server).isInfected()) {
            StoryManager.reach(player, StoryChapter.CONTAINMENT);
        }
    }

    public static void onVirusHeartDefeated(ServerLevel level, VirusHeartEntity heart, DamageSource source) {
        int radius = ShardlineConfig.HEART_PURIFY_RADIUS.get();
        Purification.purifySphere(level, heart.blockPosition(), radius, null);
        ServerPlayer killer = source.getEntity() instanceof ServerPlayer player ? player : null;
        ShardlineWorldData data = StoryManager.data(level.getServer());
        int needed = ShardlineConfig.HEARTS_FOR_CORE.get();
        for (ServerPlayer player : creditedPlayers(level, heart, killer, 32.0)) {
            StoryManager.send(player, new ScreenEffectPayload(ScreenEffectPayload.PURIFY, 40));
            StoryManager.award(player, "story/virus_heart");
            StoryManager.reach(player, StoryChapter.CONTAINMENT);
            int count = data.addHeartDefeated(player.getUUID());
            int progress = count % needed == 0 ? needed : count % needed;
            player.sendSystemMessage(Component.translatable("message.shardline.virus_heart_defeated", progress, needed)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            if (count % needed == 0) {
                ItemStack core = new ItemStack(ModItems.BREACH_CORE.get());
                if (!player.getInventory().add(core)) {
                    player.drop(core, false);
                }
                player.sendSystemMessage(Component.translatable("message.shardline.breach_core_received")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                StoryManager.award(player, "story/breach_core");
            }
        }
    }

    static Set<ServerPlayer> creditedPlayers(ServerLevel level, LivingEntity boss, ServerPlayer killer, double radius) {
        Set<ServerPlayer> players = new LinkedHashSet<>();
        if (killer != null) {
            players.add(killer);
        }
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(boss.blockPosition()).inflate(radius))) {
            if (!player.isSpectator()) {
                players.add(player);
            }
        }
        return players;
    }
}
