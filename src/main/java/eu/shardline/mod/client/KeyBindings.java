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

    private KeyBindings() {
    }
}
