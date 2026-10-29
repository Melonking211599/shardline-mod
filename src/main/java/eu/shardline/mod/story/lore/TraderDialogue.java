package eu.shardline.mod.story.lore;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.mob.ShardTraderEntity;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.item.custom.LorePageItem;
import eu.shardline.mod.network.TraderActionPayload;
import eu.shardline.mod.network.TraderDialoguePayload;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.StoryScheduler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Der Splitterhaendler als Erzaehler. Pro Story-Abschnitt (Kapitel 0-6, versiegelt, beherrscht) gibt es eine
 * Einfuehrung (einmalig), einige kurze Bemerkungen und ein laengeres Gespraech ("Erzaehl mir mehr").
 * Schluessel: {@code dialogue.shardline.trader.<abschnitt>.intro.<n>}, {@code .idle.<n>} und {@code .more.<n>}.
 *
 * <p>Gezeigt wird alles im Dialogfenster ({@link TraderDialoguePayload}); der Server entscheidet, was gesagt wird,
 * und fuehrt die Knoepfe aus ({@link TraderActionPayload}). Clients ohne den Kanal (z. B. Test-Spieler) erhalten die
 * Einfuehrung wie frueher im Chat. Zusaetzlich handelt der Haendler mit losen Lore-Seiten: Er kauft sie gegen
 * Shard-Splitter und bindet vier Seiten desselben Buchs zum ganzen Buch.</p>
 */
public final class TraderDialogue {
    private static final int INTRO_LINES = 4;
    private static final int IDLE_LINES = 3;
    private static final int MORE_LINES = 3;
    /** Seiten, die fuer ein ganzes Buch noetig sind. */
    public static final int PAGES_PER_BOOK = 4;
    private static final double REACH_SQR = 8.0 * 8.0;

    private TraderDialogue() {
    }

    /** Abschnitt fuer den Spieler: "chapter_0" ... "chapter_6", "sealed" oder "dominated". */
    public static String stage(ServerPlayer player) {
        ShardlineWorldData data = StoryManager.data(player.server);
        if (data.ending() == StoryEnding.SEALED) {
            return "sealed";
        }
        if (data.ending() == StoryEnding.DOMINATED) {
            return "dominated";
        }
        // Kapitel 7-9 gibt es nur mit einem Ende, dann gelten "sealed"/"dominated"; per Befehl gesetzt, spricht er wie in Kapitel 6
        return "chapter_" + Math.min(data.chapter(player.getUUID()), StoryChapter.BEFORE_ENDING);
    }

    /**
     * Spricht den Spieler an.
     *
     * @param forceTalk Spieler hat geschlichen (will reden statt handeln)
     * @return true, wenn eine Einfuehrung gestartet wurde und das Handelsfenster diesmal zu bleiben soll
     */
    public static boolean talk(ServerPlayer player, Entity trader, boolean forceTalk) {
        if (!ShardlineConfig.TRADER_DIALOGUE.get()) {
            return false;
        }
        ShardlineWorldData data = StoryManager.data(player.server);
        String stage = stage(player);
        if (data.addFlag(player.getUUID(), "dialogue:" + stage)) {
            playIntro(player, trader, stage);
            if (stage.equals("chapter_" + StoryChapter.HEART_OF_THE_EXPANSE.number())) {
                StoryManager.award(player, "story/trader_warning");
            }
            return true;
        }
        if (forceTalk) {
            int line = 1 + player.getRandom().nextInt(IDLE_LINES);
            say(player, trader, Component.translatable("dialogue.shardline.trader." + stage + ".idle." + line));
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ Dialogfenster

    /** Rechtsklick auf den Haendler: Dialogfenster oeffnen (oder Fallback im Chat). */
    public static void open(ServerPlayer player, ShardTraderEntity trader) {
        if (!ShardlineConfig.TRADER_DIALOGUE.get()) {
            trader.beginTrading(player);
            return;
        }
        if (player.connection == null || !player.connection.hasChannel(TraderDialoguePayload.TYPE)) {
            talk(player, trader, false);
            trader.beginTrading(player);
            return;
        }
        ShardlineWorldData data = StoryManager.data(player.server);
        String stage = stage(player);
        List<String> lines = new ArrayList<>();
        if (data.addFlag(player.getUUID(), "dialogue:" + stage)) {
            for (int line = 1; line <= INTRO_LINES; line++) {
                lines.add("dialogue.shardline.trader." + stage + ".intro." + line);
            }
            if (stage.equals("chapter_" + StoryChapter.HEART_OF_THE_EXPANSE.number())) {
                StoryManager.award(player, "story/trader_warning");
            }
        } else {
            lines.add("dialogue.shardline.trader." + stage + ".idle." + (1 + player.getRandom().nextInt(IDLE_LINES)));
        }
        sendMain(player, trader, lines);
    }

    /** Knopf im Dialogfenster. Prueft, ob der Spieler wirklich bei diesem Haendler steht. */
    public static void handleAction(ServerPlayer player, int entityId, int action) {
        if (!(player.level().getEntity(entityId) instanceof ShardTraderEntity trader) || !trader.isAlive()
                || player.distanceToSqr(trader) > REACH_SQR || trader.isTrading() && trader.getTradingPlayer() != player) {
            return;
        }
        switch (action) {
            case TraderActionPayload.TRADE -> trader.beginTrading(player);
            case TraderActionPayload.MORE -> {
                String stage = stage(player);
                List<String> lines = new ArrayList<>();
                for (int line = 1; line <= MORE_LINES; line++) {
                    lines.add("dialogue.shardline.trader." + stage + ".more." + line);
                }
                sendMain(player, trader, lines);
            }
            case TraderActionPayload.LORE -> sendLore(player, trader, countPages(player) > 0
                    ? (bindableBooks(player).isEmpty() ? "dialogue.shardline.trader.lore.offer" : "dialogue.shardline.trader.lore.offer_bind")
                    : "dialogue.shardline.trader.lore.none");
            case TraderActionPayload.SELL_PAGES -> {
                int sold = sellPages(player);
                if (sold > 0) {
                    player.displayClientMessage(Component.translatable("message.shardline.trader.pages_sold", sold,
                            sold * ShardlineConfig.LORE_PAGE_PRICE.get()).withStyle(ChatFormatting.GOLD), true);
                    player.playNotifySound(ModSounds.SHARD_TRADER_TRADE.get(), SoundSource.NEUTRAL, 0.8F, 1.0F);
                }
                sendLore(player, trader, sold > 0 ? "dialogue.shardline.trader.lore.sold" : "dialogue.shardline.trader.lore.none");
            }
            case TraderActionPayload.BIND_BOOK -> {
                LoreBooks.LoreBook book = bindBook(player);
                if (book != null) {
                    player.displayClientMessage(Component.translatable("message.shardline.trader.book_bound", book.title())
                            .withStyle(ChatFormatting.GOLD), true);
                    player.playNotifySound(ModSounds.LORE_BOOK_OPEN.get(), SoundSource.NEUTRAL, 1.0F, 0.8F);
                    StoryManager.award(player, "story/lore_bound");
                }
                sendLore(player, trader, book != null ? "dialogue.shardline.trader.lore.bound" : "dialogue.shardline.trader.lore.none");
            }
            case TraderActionPayload.BACK -> sendMain(player, trader,
                    List.of("dialogue.shardline.trader." + stage(player) + ".idle." + (1 + player.getRandom().nextInt(IDLE_LINES))));
            default -> {
            }
        }
    }

    private static void sendMain(ServerPlayer player, ShardTraderEntity trader, List<String> lines) {
        player.playNotifySound(ModSounds.SHARD_TRADER_AMBIENT.get(), SoundSource.NEUTRAL, 0.6F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        StoryManager.send(player, new TraderDialoguePayload(trader.getId(), TraderDialoguePayload.MAIN, lines, countPages(player),
                bindableBooks(player).size()));
    }

    private static void sendLore(ServerPlayer player, ShardTraderEntity trader, String line) {
        StoryManager.send(player, new TraderDialoguePayload(trader.getId(), TraderDialoguePayload.LORE, List.of(line), countPages(player),
                bindableBooks(player).size()));
    }

    // ------------------------------------------------------------------ Handel mit losen Seiten

    public static int countPages(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.LORE_PAGE.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** Buecher, von denen der Spieler mindestens vier Seiten hat (ungelesene zuerst). */
    public static List<LoreBooks.LoreBook> bindableBooks(Player player) {
        Map<String, Integer> pages = new LinkedHashMap<>();
        for (ItemStack stack : player.getInventory().items) {
            LoreBooks.LoreBook book = stack.is(ModItems.LORE_PAGE.get()) ? LorePageItem.book(stack) : null;
            if (book != null) {
                pages.merge(book.id(), stack.getCount(), Integer::sum);
            }
        }
        List<LoreBooks.LoreBook> books = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : pages.entrySet()) {
            if (entry.getValue() >= PAGES_PER_BOOK) {
                books.add(LoreBooks.byId(entry.getKey()));
            }
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ShardlineWorldData data = StoryManager.data(serverPlayer.server);
            books.sort(Comparator.comparing(book -> data.hasFlag(player.getUUID(), "book:" + book.id())));
        }
        return books;
    }

    /** Verkauft alle losen Seiten. @return Zahl der verkauften Seiten */
    public static int sellPages(Player player) {
        int sold = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.LORE_PAGE.get())) {
                sold += stack.getCount();
                stack.setCount(0);
            }
        }
        int fragments = sold * ShardlineConfig.LORE_PAGE_PRICE.get();
        while (fragments > 0) {
            int count = Math.min(fragments, 64);
            fragments -= count;
            ItemStack payment = new ItemStack(ModItems.SHARD_FRAGMENT.get(), count);
            if (!player.getInventory().add(payment)) {
                player.drop(payment, false);
            }
        }
        return sold;
    }

    /** Bindet vier Seiten eines Buchs zum ganzen Buch. @return das Buch oder {@code null} */
    @Nullable
    public static LoreBooks.LoreBook bindBook(Player player) {
        List<LoreBooks.LoreBook> books = bindableBooks(player);
        if (books.isEmpty()) {
            return null;
        }
        LoreBooks.LoreBook book = books.get(0);
        int needed = PAGES_PER_BOOK;
        for (ItemStack stack : player.getInventory().items) {
            if (needed > 0 && stack.is(ModItems.LORE_PAGE.get()) && LorePageItem.book(stack) == book) {
                int take = Math.min(needed, stack.getCount());
                stack.shrink(take);
                needed -= take;
            }
        }
        ItemStack bound = LoreBooks.create(book);
        if (!player.getInventory().add(bound)) {
            player.drop(bound, false);
        }
        return book;
    }

    // ------------------------------------------------------------------ Chat (Fallback)

    private static void playIntro(ServerPlayer player, Entity trader, String stage) {
        int delay = ShardlineConfig.DIALOGUE_LINE_DELAY_TICKS.get();
        for (int line = 1; line <= INTRO_LINES; line++) {
            Component text = Component.translatable("dialogue.shardline.trader." + stage + ".intro." + line);
            if (line == 1) {
                say(player, trader, text);
            } else {
                StoryScheduler.schedule(delay * (line - 1), () -> {
                    if (!player.hasDisconnected()) {
                        say(player, trader, text);
                    }
                });
            }
        }
        StoryScheduler.schedule(delay * INTRO_LINES, () -> {
            if (!player.hasDisconnected()) {
                player.sendSystemMessage(Component.translatable("dialogue.shardline.trader.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });
    }

    private static void say(ServerPlayer player, Entity trader, Component text) {
        player.sendSystemMessage(Component.empty()
                .append(Component.literal("<").withStyle(ChatFormatting.GOLD))
                .append(trader.getDisplayName().copy().withStyle(ChatFormatting.GOLD))
                .append(Component.literal("> ").withStyle(ChatFormatting.GOLD))
                .append(text.copy().withStyle(ChatFormatting.YELLOW)));
        player.playNotifySound(ModSounds.SHARD_TRADER_AMBIENT.get(), SoundSource.NEUTRAL, 0.6F, 0.9F + player.getRandom().nextFloat() * 0.2F);
    }
}
