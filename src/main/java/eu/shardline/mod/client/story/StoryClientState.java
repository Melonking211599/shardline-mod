package eu.shardline.mod.client.story;

import eu.shardline.mod.story.StoryEnding;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Story-Zustand, wie ihn der Server dem Client mitteilt. Rein lesend fuer Darstellung
 * (Himmel, Nebel, Musik, Umhang, Faehigkeitstasten); alle Entscheidungen trifft der Server.
 */
public final class StoryClientState {
    private static int chapter;
    private static boolean infected;
    private static boolean paused;
    private static StoryEnding ending = StoryEnding.NONE;
    private static boolean ruler;
    private static final Set<UUID> KEEPERS = new HashSet<>();
    private static final Set<UUID> RULERS = new HashSet<>();

    private StoryClientState() {
    }

    static void update(int newChapter, boolean newInfected, boolean newPaused, StoryEnding newEnding, boolean newRuler) {
        chapter = newChapter;
        infected = newInfected;
        paused = newPaused;
        ending = newEnding;
        ruler = newRuler;
    }

    static void updateTitles(List<UUID> keepers, List<UUID> rulers) {
        KEEPERS.clear();
        KEEPERS.addAll(keepers);
        RULERS.clear();
        RULERS.addAll(rulers);
    }

    /** Beim Verlassen einer Welt zuruecksetzen. */
    public static void reset() {
        update(0, false, false, StoryEnding.NONE, false);
        KEEPERS.clear();
        RULERS.clear();
    }

    public static int chapter() {
        return chapter;
    }

    /** Virus frei und nicht versiegelt: violetter Himmel, Atmosphaere. */
    public static boolean virusActive() {
        return infected && ending != StoryEnding.SEALED;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static StoryEnding ending() {
        return ending;
    }

    public static boolean isRuler() {
        return ruler;
    }

    public static boolean isKeeper(UUID player) {
        return KEEPERS.contains(player);
    }

    public static boolean isRuler(UUID player) {
        return RULERS.contains(player);
    }
}
