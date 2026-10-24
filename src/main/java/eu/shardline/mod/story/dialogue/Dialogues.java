package eu.shardline.mod.story.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

/**
 * Geladene Dialoge und ihre Auswertung (nur Server). Welcher Knoten startet, welche Antworten sichtbar und frei sind und
 * was eine Antwort bewirkt, entscheidet allein diese Klasse; der Client bekommt nur das Ergebnis.
 */
public final class Dialogues {
    private static Map<ResourceLocation, DialogueDefinition> dialogues = Map.of();
    private static List<String> errors = List.of();

    private Dialogues() {
    }

    public static void set(Map<ResourceLocation, DialogueDefinition> loaded, List<String> loadErrors) {
        dialogues = Map.copyOf(loaded);
        errors = List.copyOf(loadErrors);
    }

    @Nullable
    public static DialogueDefinition get(ResourceLocation id) {
        return dialogues.get(id);
    }

    public static Collection<DialogueDefinition> all() {
        return dialogues.values();
    }

    /** Fehler des letzten Ladens (auch Auftraege), fuer {@code /shardline dialogue reload} und Tests. */
    public static List<String> errors() {
        return errors;
    }

    /** Erster Startknoten, dessen Bedingungen gelten (die Rueckfallregel ohne Bedingung gilt immer). */
    @Nullable
    public static DialogueDefinition.Node startNode(DialogueDefinition dialogue, DialogueContext context) {
        for (DialogueDefinition.Entry entry : dialogue.start()) {
            if (entry.conditions().stream().allMatch(condition -> condition.test(context))) {
                return dialogue.node(entry.node());
            }
        }
        return null;
    }

    /** Eine Antwort, wie der Spieler sie sieht. {@code index} ist die Position im Knoten. */
    public record ChoiceView(int index, DialogueDefinition.Choice choice, boolean enabled) {
    }

    /** Sichtbare Antworten: erfuellte, dazu gesperrte mit {@code show_locked}. */
    public static List<ChoiceView> visibleChoices(DialogueDefinition.Node node, DialogueContext context) {
        List<ChoiceView> list = new ArrayList<>();
        for (int i = 0; i < node.choices().size(); i++) {
            DialogueDefinition.Choice choice = node.choices().get(i);
            boolean allowed = choice.allowed(context);
            if (allowed || choice.showLocked()) {
                list.add(new ChoiceView(i, choice, allowed));
            }
        }
        return list;
    }

    /** Ergebnis einer Auswahl: angenommen und Folgeknoten (null = Dialog endet). */
    public record Outcome(boolean accepted, @Nullable DialogueDefinition.Node next) {
        static final Outcome REJECTED = new Outcome(false, null);
    }

    /**
     * Fuehrt die Auswahl aus. {@code visibleIndex} bezieht sich auf {@link #visibleChoices}; bei Knoten ohne Antworten
     * ist 0 "Weiter". Bedingungen werden hier erneut geprueft, gesperrte Antworten werden abgelehnt.
     */
    public static Outcome choose(DialogueDefinition dialogue, DialogueDefinition.Node node, DialogueContext context, int visibleIndex) {
        String next;
        if (node.choices().isEmpty()) {
            if (visibleIndex != 0) {
                return Outcome.REJECTED;
            }
            next = node.next().isEmpty() ? DialogueDefinition.END : node.next();
        } else {
            List<ChoiceView> visible = visibleChoices(node, context);
            if (visibleIndex < 0 || visibleIndex >= visible.size() || !visible.get(visibleIndex).enabled()) {
                return Outcome.REJECTED;
            }
            DialogueDefinition.Choice choice = visible.get(visibleIndex).choice();
            choice.actions().forEach(action -> action.run(context));
            next = choice.next();
        }
        if (next.equals(DialogueDefinition.END)) {
            return new Outcome(true, null);
        }
        DialogueDefinition.Node target = dialogue.node(next);
        if (target != null) {
            enter(target, context);
        }
        return new Outcome(true, target);
    }

    /** Aktionen beim Betreten eines Knotens. */
    public static void enter(DialogueDefinition.Node node, DialogueContext context) {
        node.onEnter().forEach(action -> action.run(context));
    }
}
