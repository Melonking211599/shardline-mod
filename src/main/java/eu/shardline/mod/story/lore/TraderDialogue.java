package eu.shardline.mod.story.lore;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.StoryScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Der Splitterhaendler als Erzaehler. Pro Story-Abschnitt gibt es eine Einfuehrung (einmalig,
 * Zeile fuer Zeile im Chat) und einige kurze Bemerkungen fuer spaetere Gespraeche.
 * Schluessel: {@code dialogue.shardline.trader.<abschnitt>.intro.<n>} und {@code .idle.<n>}.
 */
public final class TraderDialogue {
    private static final int INTRO_LINES = 4;
    private static final int IDLE_LINES = 3;

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
        return "chapter_" + data.chapter(player.getUUID());
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
