package eu.shardline.mod.story.dialogue;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

/**
 * Ein Dialog aus {@code data/<ns>/dialogues/<name>.json}: Sprecher, Startregeln (erste passende gewinnt) und Knoten.
 * Texte sind Uebersetzungsschluessel ({@code dialogue.shardline.<npc>.<knoten>.<n>}); der Client uebersetzt selbst.
 */
public record DialogueDefinition(ResourceLocation id, String speaker, List<Entry> start, Map<String, Node> nodes) {
    /** Knotenname, der den Dialog beendet. */
    public static final String END = "END";
    /** Hoechstens so viele Antworten pro Knoten (Tasten 1-4). */
    public static final int MAX_CHOICES = 4;
    /** Bekannte Porträt-Posen. */
    public static final List<String> PORTRAITS = List.of("neutral", "happy", "sad", "angry");

    @Nullable
    public Node node(String name) {
        return this.nodes.get(name);
    }

    /** Startregel: alle Bedingungen erfuellt -> dieser Knoten. */
    public record Entry(List<Condition> conditions, String node) {
    }

    /**
     * @param speaker  Sprecher dieses Knotens (sonst der des Dialogs)
     * @param portrait Pose des Porträts
     * @param text     Seiten des Textes (je ein Schluessel)
     * @param onEnter  Aktionen beim Betreten
     * @param choices  Antworten; leer = "Weiter" zu {@code next}
     * @param next     Folgeknoten ohne Antworten ({@link #END} oder leer = Ende)
     */
    public record Node(String name, String speaker, String portrait, List<String> text, List<Action> onEnter, List<Choice> choices,
                       String next) {
    }

    /**
     * @param text       Schluessel des Antworttexts
     * @param next       Folgeknoten oder {@link #END}
     * @param showLocked auch zeigen, wenn die Bedingungen nicht erfuellt sind (gesperrt, mit Hinweis)
     * @param lockedHint Schluessel des Hinweises (leer = "Bedingung nicht erfuellt")
     */
    public record Choice(String text, String next, List<Condition> conditions, List<Action> actions, boolean showLocked, String lockedHint) {
        public boolean allowed(DialogueContext context) {
            for (Condition condition : this.conditions) {
                if (!condition.test(context)) {
                    return false;
                }
            }
            return true;
        }
    }
}
