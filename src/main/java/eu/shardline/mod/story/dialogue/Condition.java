package eu.shardline.mod.story.dialogue;

/** Bedingung fuer Startknoten und Antworten. Wird bei jeder Auswahl auf dem Server neu geprueft. */
@FunctionalInterface
public interface Condition {
    Condition ALWAYS = context -> true;

    boolean test(DialogueContext context);
}
