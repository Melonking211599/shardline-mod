package eu.shardline.mod.story.dialogue;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.network.CloseDialoguePayload;
import eu.shardline.mod.network.OpenDialoguePayload;
import eu.shardline.mod.story.StoryManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Offene Dialoge (nur Server). Jeder Spieler hat hoechstens eine Sitzung; ein NPC kann mit mehreren Spielern zugleich
 * sprechen. Jede Antwort wird gegen Sitzungsnummer, Abstand, lebenden NPC und die Bedingungen des Knotens geprueft.
 * Abbruch bei Schaden, Tod, Abmelden, zu grossem Abstand oder verschwundenem NPC.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class DialogueSessions {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static int nextId = 1;

    private DialogueSessions() {
    }

    /** Eine offene Unterhaltung. {@code entityId} = -1 ohne NPC (Befehl); dann zaehlt der Startpunkt fuer den Abstand. */
    public static final class Session {
        private final int id;
        private final ResourceLocation dialogue;
        private final int entityId;
        private final Vec3 anchor;
        private String node;

        Session(int id, ResourceLocation dialogue, int entityId, Vec3 anchor, String node) {
            this.id = id;
            this.dialogue = dialogue;
            this.entityId = entityId;
            this.anchor = anchor;
            this.node = node;
        }

        public int id() {
            return this.id;
        }

        public ResourceLocation dialogue() {
            return this.dialogue;
        }

        public String node() {
            return this.node;
        }

        public int entityId() {
            return this.entityId;
        }
    }

    @Nullable
    public static Session current(ServerPlayer player) {
        return SESSIONS.get(player.getUUID());
    }

    /** Ob gerade jemand mit diesem NPC spricht (Animation "talk", Stehenbleiben). */
    public static boolean isTalking(Entity npc) {
        for (Session session : SESSIONS.values()) {
            if (session.entityId == npc.getId()) {
                return true;
            }
        }
        return false;
    }

    /** Spieler, die gerade mit diesem NPC sprechen. */
    public static List<UUID> listeners(Entity npc) {
        List<UUID> list = new ArrayList<>();
        SESSIONS.forEach((uuid, session) -> {
            if (session.entityId == npc.getId()) {
                list.add(uuid);
            }
        });
        return list;
    }

    /**
     * Beginnt einen Dialog. Eine bereits offene Sitzung des Spielers wird ersetzt.
     *
     * @return false, wenn der Dialog nicht existiert oder kein Startknoten passt
     */
    public static boolean start(ServerPlayer player, @Nullable Entity npc, ResourceLocation dialogueId) {
        DialogueDefinition dialogue = Dialogues.get(dialogueId);
        if (dialogue == null) {
            return false;
        }
        DialogueContext context = new DialogueContext(player, npc);
        DialogueDefinition.Node node = Dialogues.startNode(dialogue, context);
        if (node == null) {
            return false;
        }
        Session session = new Session(nextId++, dialogueId, npc != null ? npc.getId() : -1,
                npc != null ? npc.position() : player.position(), node.name());
        SESSIONS.put(player.getUUID(), session);
        player.playNotifySound(ModSounds.DIALOGUE_OPEN.get(), SoundSource.NEUTRAL, 0.6F, 1.0F);
        Dialogues.enter(node, context);
        if (SESSIONS.get(player.getUUID()) == session) {   // eine Aktion kann den Dialog schon beendet haben
            send(player, session, dialogue, node, context);
        }
        return true;
    }

    /** Antwort des Clients. Ungueltige Antworten fuehren zum erneuten Senden des aktuellen Knotens. */
    public static void onChoice(ServerPlayer player, int sessionId, int index) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.id != sessionId) {
            StoryManager.send(player, new CloseDialoguePayload(sessionId));
            return;
        }
        Entity npc = npc(player, session);
        if (!inRange(player, session, npc)) {
            close(player, true);
            return;
        }
        DialogueDefinition dialogue = Dialogues.get(session.dialogue);
        DialogueDefinition.Node node = dialogue == null ? null : dialogue.node(session.node);
        if (dialogue == null || node == null) {
            close(player, true);   // nach /reload verschwunden
            return;
        }
        DialogueContext context = new DialogueContext(player, npc);
        Dialogues.Outcome outcome = Dialogues.choose(dialogue, node, context, index);
        if (!outcome.accepted()) {
            send(player, session, dialogue, node, context);   // Client mit dem echten Stand abgleichen
            return;
        }
        if (SESSIONS.get(player.getUUID()) != session) {
            return;   // eine Aktion (z. B. open_trade) hat den Dialog beendet
        }
        if (outcome.next() == null) {
            close(player, true);
            return;
        }
        session.node = outcome.next().name();
        send(player, session, dialogue, outcome.next(), context);
    }

    /** Beendet die Sitzung des Spielers; mit {@code notifyClient} schliesst sich auch das Fenster. */
    public static void close(ServerPlayer player, boolean notifyClient) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session != null && notifyClient) {
            StoryManager.send(player, new CloseDialoguePayload(session.id));
            player.playNotifySound(ModSounds.DIALOGUE_CLOSE.get(), SoundSource.NEUTRAL, 0.5F, 1.0F);
        }
    }

    /** Client hat das Fenster geschlossen (Esc). */
    public static void onClientClose(ServerPlayer player, int sessionId) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && session.id == sessionId) {
            SESSIONS.remove(player.getUUID());
        }
    }

    private static void send(ServerPlayer player, Session session, DialogueDefinition dialogue, DialogueDefinition.Node node, DialogueContext context) {
        List<Component> text = new ArrayList<>();
        node.text().forEach(key -> text.add(Component.translatable(key)));
        List<OpenDialoguePayload.Choice> choices = new ArrayList<>();
        for (Dialogues.ChoiceView view : Dialogues.visibleChoices(node, context)) {
            String hint = view.choice().lockedHint().isEmpty() ? "screen.shardline.dialogue.locked" : view.choice().lockedHint();
            choices.add(new OpenDialoguePayload.Choice(Component.translatable(view.choice().text()), view.enabled(), Component.translatable(hint)));
        }
        boolean ends = node.choices().isEmpty() && (node.next().isEmpty() || node.next().equals(DialogueDefinition.END));
        StoryManager.send(player, new OpenDialoguePayload(session.id, session.entityId, node.speaker(), node.portrait(), text, choices, ends));
    }

    @Nullable
    private static Entity npc(ServerPlayer player, Session session) {
        return session.entityId < 0 ? null : player.serverLevel().getEntity(session.entityId);
    }

    private static boolean inRange(ServerPlayer player, Session session, @Nullable Entity npc) {
        if (session.entityId >= 0 && (npc == null || !npc.isAlive())) {
            return false;
        }
        Vec3 point = npc != null ? npc.position() : session.anchor;
        double max = ShardlineConfig.DIALOGUE_MAX_DISTANCE.get();
        return player.isAlive() && player.position().distanceToSqr(point) <= max * max;
    }

    // ------------------------------------------------------------------ Abbruch

    @SubscribeEvent
    static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0.0F && SESSIONS.containsKey(player.getUUID())) {
            close(player, true);
        }
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            close(player, true);
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            close(player, false);
        }
    }

    @SubscribeEvent
    static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            close(player, true);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 10 == 0) {
            Session session = SESSIONS.get(player.getUUID());
            if (session != null && !inRange(player, session, npc(player, session))) {
                close(player, true);
            }
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        SESSIONS.clear();
    }
}
