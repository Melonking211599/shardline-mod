package eu.shardline.mod.story;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

/**
 * Die Kapitel der Shardline-Geschichte. Der Fortschritt wird pro Spieler in
 * {@link ShardlineWorldData} gespeichert; 0 bedeutet "noch nicht begonnen".
 *
 * <p>Kapitel 7-9 ("Nach dem Ausbruch") spielen nach der Endentscheidung. Kapitel 8 und 9 haben je einen Zweig pro
 * Ende ({@link #branched()}): Titel, Zusammenfassung, Advancement und Emblem haengen dann vom Zweig des Spielers ab
 * ({@link ShardlineWorldData#branch}). Ohne Zweig gilt der allgemeine Titel.</p>
 */
public enum StoryChapter {
    FIRST_SHARD(1, "first_shard"),
    THE_GATE(2, "the_gate"),
    ECHOES_OF_THE_KEEPERS(3, "echoes_of_the_keepers"),
    HEART_OF_THE_EXPANSE(4, "heart_of_the_expanse"),
    THE_BREACH(5, "the_breach"),
    CONTAINMENT(6, "containment"),
    ECHO_OF_THE_WOUND(7, "echo_of_the_wound"),
    AFTERMATH(8, "aftermath"),
    THE_LAST_TONE(9, "the_last_tone");

    public static final int MAX = 9;
    /** Letztes Kapitel vor der Endentscheidung; ab hier braucht jedes weitere Kapitel ein Ende der Welt. */
    public static final int BEFORE_ENDING = 6;

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

    /** Kapitel mit eigenem Inhalt je Ende (8 und 9). */
    public boolean branched() {
        return this.number >= 8;
    }

    /** "Kapitel 3" */
    public Component label() {
        return Component.translatable("story.shardline.chapter", this.number);
    }

    /** "Echos der Hueter" (bei Zweig-Kapiteln der allgemeine Titel) */
    public Component title() {
        return Component.translatable("story.shardline.chapter." + this.id);
    }

    /** Titel im Zweig des Spielers, z. B. "Wiederaufbau" oder "Herrschaft". */
    public Component title(StoryEnding branch) {
        return Component.translatable(this.key(branch));
    }

    /** Ein Satz, der beim Erreichen des Kapitels im Chat erscheint. */
    public Component summary() {
        return Component.translatable("story.shardline.chapter." + this.id + ".summary");
    }

    public Component summary(StoryEnding branch) {
        return Component.translatable(this.key(branch) + ".summary");
    }

    private String key(StoryEnding branch) {
        String base = "story.shardline.chapter." + this.id;
        return this.branched() && branch != StoryEnding.NONE ? base + "." + branch.id() : base;
    }

    /** ID des zugehoerigen Advancements im Story-Tab. */
    public String advancementId() {
        return "story/chapter_" + this.number;
    }

    /** Advancement im Zweig: "story/chapter_8_sealed" bzw. "story/chapter_8_dominated". */
    public String advancementId(StoryEnding branch) {
        return this.branched() && branch != StoryEnding.NONE ? this.advancementId() + "_" + branch.id() : this.advancementId();
    }

    /**
     * Wert der Datenkomponente {@code shardline:chapter} fuer das Emblem: Kapitelnummer, bei Zweig-Kapiteln plus
     * 10 (versiegelt) bzw. 20 (beherrscht), also 18/19 und 28/29. So bleiben die Werte 1-7 alter Embleme gueltig.
     */
    public int emblemValue(StoryEnding branch) {
        if (!this.branched() || branch == StoryEnding.NONE) {
            return this.number;
        }
        return this.number + (branch == StoryEnding.SEALED ? 10 : 20);
    }

    /** Kapitel zu einem Emblem-Wert ({@link #emblemValue}). */
    @Nullable
    public static StoryChapter byEmblemValue(int value) {
        return byNumber(value % 10 == 0 ? value : value > 10 ? value % 10 : value);
    }

    /** Zweig zu einem Emblem-Wert ({@link #emblemValue}). */
    public static StoryEnding branchOfEmblemValue(int value) {
        return value > 20 ? StoryEnding.DOMINATED : value > 10 ? StoryEnding.SEALED : StoryEnding.NONE;
    }

    /** Name der Emblemtextur: chapter_emblem_7, chapter_emblem_8_sealed ... */
    public String emblemTexture(StoryEnding branch) {
        return "chapter_emblem_" + this.number + (this.branched() && branch != StoryEnding.NONE ? "_" + branch.id() : "");
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
