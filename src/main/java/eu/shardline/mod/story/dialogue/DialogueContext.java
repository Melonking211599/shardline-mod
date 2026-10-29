package eu.shardline.mod.story.dialogue;

import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryManager;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Wer spricht mit wem: Spieler (immer auf dem Server) und optional das NPC-Wesen. */
public record DialogueContext(ServerPlayer player, @Nullable Entity npc) {
    public ShardlineWorldData data() {
        return StoryManager.data(this.player.server);
    }

    public UUID uuid() {
        return this.player.getUUID();
    }
}
