package eu.shardline.mod.story.dialogue;

import net.minecraft.server.level.ServerPlayer;

/** Ein Wesen, das Dialoge fuehrt. Die NPC-ID ist der Sprecher in den Dialogdateien und der Schluessel fuer Porträts. */
public interface DialogueNpc {
    /** z. B. "rhea"; Porträt: textures/gui/portraits/&lt;id&gt;_&lt;pose&gt;.png, Name: npc.shardline.&lt;id&gt; */
    String npcId();

    /** Oeffnet das Handelsfenster (Aktion {@code open_trade}); Standard: kein Handel. */
    default void openTrade(ServerPlayer player) {
    }
}
