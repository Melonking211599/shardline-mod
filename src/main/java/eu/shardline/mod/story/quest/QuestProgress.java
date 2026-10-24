package eu.shardline.mod.story.quest;

import net.minecraft.nbt.CompoundTag;

/**
 * Stand eines Auftrags fuer einen Spieler: aktueller Schritt (0-basiert), Zaehler im Schritt, ob er abgeschlossen ist
 * und wie oft er schon abgeschlossen wurde (wiederholbare Auftraege). Gespeichert in
 * {@link eu.shardline.mod.story.ShardlineWorldData}.
 */
public final class QuestProgress {
    private int step;
    private int count;
    private boolean active;
    private int completions;

    public int step() {
        return this.step;
    }

    public int count() {
        return this.count;
    }

    /** Laeuft gerade (begonnen und noch nicht abgeschlossen). */
    public boolean active() {
        return this.active;
    }

    public int completions() {
        return this.completions;
    }

    public boolean everCompleted() {
        return this.completions > 0;
    }

    void start() {
        this.step = 0;
        this.count = 0;
        this.active = true;
    }

    void setCount(int count) {
        this.count = count;
    }

    void nextStep() {
        this.step++;
        this.count = 0;
    }

    void complete() {
        this.active = false;
        this.count = 0;
        this.completions++;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Step", this.step);
        tag.putInt("Count", this.count);
        tag.putBoolean("Active", this.active);
        tag.putInt("Completions", this.completions);
        return tag;
    }

    public static QuestProgress load(CompoundTag tag) {
        QuestProgress progress = new QuestProgress();
        progress.step = tag.getInt("Step");
        progress.count = tag.getInt("Count");
        progress.active = tag.getBoolean("Active");
        progress.completions = tag.getInt("Completions");
        return progress;
    }
}
