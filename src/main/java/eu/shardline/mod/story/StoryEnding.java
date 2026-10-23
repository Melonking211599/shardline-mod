package eu.shardline.mod.story;

import net.minecraft.network.chat.Component;

/**
 * Ausgang der Geschichte. Wird am Altar der Splitterkoenigin mit dem Kern des Bruchs entschieden.
 */
public enum StoryEnding {
    /** Noch nicht entschieden. */
    NONE("none"),
    /** Der Virus ist wieder versiegelt; die Ausbreitung stoppt weltweit. */
    SEALED("seal"),
    /** Die Spieler beherrschen den Virus; er breitet sich weiter aus. */
    DOMINATED("dominate");

    private final String id;

    StoryEnding(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public Component title() {
        return Component.translatable("story.shardline.ending." + this.id);
    }

    public static StoryEnding byId(String id) {
        for (StoryEnding ending : values()) {
            if (ending.id.equals(id)) {
                return ending;
            }
        }
        return NONE;
    }
}
