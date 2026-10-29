package eu.shardline.mod.story.dialogue;

/** Aktion beim Betreten eines Knotens oder beim Waehlen einer Antwort. Laeuft nur auf dem Server. */
@FunctionalInterface
public interface Action {
    void run(DialogueContext context);
}
