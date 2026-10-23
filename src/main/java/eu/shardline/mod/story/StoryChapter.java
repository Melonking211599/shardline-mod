package eu.shardline.mod.story;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

/**
 * Die sechs Kapitel der Shardline-Geschichte. Der Fortschritt wird pro Spieler in
 * {@link ShardlineWorldData} gespeichert; 0 bedeutet "noch nicht begonnen".
 */
public enum StoryChapter {
    FIRST_SHARD(1, "first_shard"),
    THE_GATE(2, "the_gate"),
    ECHOES_OF_THE_KEEPERS(3, "echoes_of_the_keepers"),
    HEART_OF_THE_EXPANSE(4, "heart_of_the_expanse"),
    THE_BREACH(5, "the_breach"),
    CONTAINMENT(6, "containment");

    public static final int MAX = 6;

    private final int number;
    private final String id;

    StoryChapter(int number, String id) {
        this.number = number;
        this.id = id;
    }

    public int number() {
        return this.number;
    }

    public String id() {
        return this.id;
    }

    /** "Kapitel 3" */
    public Component label() {
        return Component.translatable("story.shardline.chapter", this.number);
    }

    /** "Echos der Hueter" */
    public Component title() {
        return Component.translatable("story.shardline.chapter." + this.id);
    }

    /** Ein Satz, der beim Erreichen des Kapitels im Chat erscheint. */
    public Component summary() {
        return Component.translatable("story.shardline.chapter." + this.id + ".summary");
    }

    /** ID des zugehoerigen Advancements im Story-Tab. */
    public String advancementId() {
        return "story/chapter_" + this.number;
    }

    @Nullable
    public static StoryChapter byNumber(int number) {
        for (StoryChapter chapter : values()) {
            if (chapter.number == number) {
                return chapter;
            }
        }
        return null;
    }
}
