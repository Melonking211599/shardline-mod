package eu.shardline.mod.story.quest;

import eu.shardline.mod.story.StoryEnding;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Ein Auftrag aus {@code data/shardline/quests/<id>.json}. Texte: {@code quest.shardline.<id>.title},
 * {@code quest.shardline.<id>.description} und {@code quest.shardline.<id>.step.<n>} (n ab 1).
 *
 * @param id          Auftrags-ID (Dateiname, bei fremdem Namensraum "ns:name")
 * @param chapterMin  fruehestes Kapitel, ab dem er angenommen werden kann
 * @param branch      nur in diesem Zweig ({@link StoryEnding#NONE} = beide)
 * @param repeatable  wiederholbar (Epilog-Auftraege)
 * @param giver       NPC-ID, die den Auftrag vergibt (fuer das Auftragsbuch)
 */
public record QuestDefinition(String id, int chapterMin, StoryEnding branch, boolean repeatable, String giver, List<Step> steps,
                              List<ItemStack> rewards, int xp, List<String> rewardFlags) {
    /** Schrittarten, die der Server kennt. */
    public static final List<String> STEP_TYPES = List.of("collect", "kill", "visit_structure", "talk", "purify_node", "bind_node",
            "place_anchor", "tame", "read_lore", "escort", "custom");

    /**
     * Ein Schritt.
     *
     * @param type   eine der {@link #STEP_TYPES}
     * @param target Item, Wesen ("#tag" erlaubt), Struktur, NPC oder freie ID; leer = jedes Ziel
     * @param count  noetige Anzahl
     */
    public record Step(String type, String target, int count) {
    }

    public String langKey() {
        return "quest.shardline." + this.id.replace(':', '.');
    }

    public Component title() {
        return Component.translatable(this.langKey() + ".title");
    }

    public Component description() {
        return Component.translatable(this.langKey() + ".description");
    }

    /** Text eines Schritts (0-basiert), z. B. "Bring 32 Kristallholzbretter". */
    public Component stepText(int step) {
        return Component.translatable(this.langKey() + ".step." + (step + 1));
    }
}
