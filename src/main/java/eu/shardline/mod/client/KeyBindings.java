package eu.shardline.mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Tastenbelegungen der Mod (in den Steuerungs-Einstellungen unter "Shardline" aenderbar).
 */
public final class KeyBindings {
    public static final String CATEGORY = "key.categories.shardline";

    public static final KeyMapping CRYSTAL_SHIELD = new KeyMapping("key.shardline.crystal_shield", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    /** Splitterherrscher: Splitterruf. */
    public static final KeyMapping SHARD_CALL = new KeyMapping("key.shardline.shard_call", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    /** Splitterherrscher: Kristallwelle. */
    public static final KeyMapping CRYSTAL_WAVE = new KeyMapping("key.shardline.crystal_wave", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);

    /** Auftragsbuch (Kapitel, Auftraege, Figuren, Lore). */
    public static final KeyMapping JOURNAL = new KeyMapping("key.shardline.journal", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);

    private KeyBindings() {
    }
}
