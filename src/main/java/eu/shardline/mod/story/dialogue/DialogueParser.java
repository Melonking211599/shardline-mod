package eu.shardline.mod.story.dialogue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.quest.QuestDefinition;
import eu.shardline.mod.story.quest.QuestProgress;
import eu.shardline.mod.story.quest.Quests;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Liest Dialoge und Auftraege aus JSON und prueft sie. Fehler werden gesammelt ({@link Problems}) statt geworfen, damit
 * eine kaputte Datei nur sich selbst abschaltet. Querverweise auf Auftraege prueft {@link #checkQuestRefs} nach dem
 * Laden aller Dateien.
 *
 * <p>Bedingungen sind Objekte mit genau einem Schluessel ({@code {"chapter_min": 7}}), Listen sind "alle muessen
 * gelten". Aktionen ebenso ({@code {"give_item": {"item": "minecraft:bread", "count": 2}}}).</p>
 */
public final class DialogueParser {
    public static final Set<String> CONDITIONS = Set.of("chapter_min", "chapter_max", "ending", "branch", "title", "has_item", "flag",
            "not_flag", "quest_active", "quest_done", "quest_new", "quest_step", "epilogue", "any", "not");
    public static final Set<String> ACTIONS = Set.of("give_item", "take_item", "set_flag", "clear_flag", "advance_chapter", "start_quest",
            "complete_quest_step", "open_trade", "play_sound", "run_command_safe", "set_epilogue", "give_xp", "npc_state");
    /** Zustaende fuer {@code npc_state} (siehe {@link DialogueNpc#applyState}). */
    public static final Set<String> NPC_STATES = Set.of("follow", "stay", "healed");
    private static final Set<String> ENDINGS = Set.of("none", "seal", "dominate");
    private static final Set<String> TITLES = Set.of("none", "keeper", "ruler");

    private DialogueParser() {
    }

    /** Gesammelte Fehler einer Datei. */
    public static final class Problems {
        private final String file;
        private final List<String> errors = new ArrayList<>();
        /** Auftraege, auf die verwiesen wird (Pruefung nach dem Laden aller Auftraege). */
        final Set<String> questRefs = new HashSet<>();

        public Problems(String file) {
            this.file = file;
        }

        public void error(String where, String message) {
            this.errors.add(this.file + " [" + where + "]: " + message);
        }

        public List<String> errors() {
            return this.errors;
        }

        public boolean ok() {
            return this.errors.isEmpty();
        }
    }

    // ------------------------------------------------------------------ Dialoge

    public static DialogueDefinition parseDialogue(ResourceLocation id, JsonObject json, Problems problems) {
        String speaker = string(json, "speaker", "", problems, "speaker");
        if (speaker.isEmpty()) {
            problems.error("speaker", "fehlt");
        }
        Map<String, DialogueDefinition.Node> nodes = new LinkedHashMap<>();
        JsonObject nodeJson = json.has("nodes") && json.get("nodes").isJsonObject() ? json.getAsJsonObject("nodes") : new JsonObject();
        if (nodeJson.size() == 0) {
            problems.error("nodes", "keine Knoten");
        }
        for (Map.Entry<String, JsonElement> entry : nodeJson.entrySet()) {
            String name = entry.getKey();
            if (name.equals(DialogueDefinition.END)) {
                problems.error("nodes." + name, "der Name END ist reserviert");
                continue;
            }
            if (!entry.getValue().isJsonObject()) {
                problems.error("nodes." + name, "kein Objekt");
                continue;
            }
            nodes.put(name, parseNode(name, speaker, entry.getValue().getAsJsonObject(), problems));
        }

        List<DialogueDefinition.Entry> start = new ArrayList<>();
        boolean fallback = false;
        JsonArray startJson = json.has("start") && json.get("start").isJsonArray() ? json.getAsJsonArray("start") : new JsonArray();
        for (int i = 0; i < startJson.size(); i++) {
            String where = "start[" + i + "]";
            if (!startJson.get(i).isJsonObject()) {
                problems.error(where, "kein Objekt");
                continue;
            }
            JsonObject entry = startJson.get(i).getAsJsonObject();
            String node = string(entry, "node", "", problems, where);
            if (!nodes.containsKey(node)) {
                problems.error(where, "unbekannter Knoten '" + node + "'");
            }
            List<Condition> conditions = entry.has("if") ? conditions(entry.get("if"), problems, where + ".if") : List.of();
            fallback |= conditions.isEmpty();
            start.add(new DialogueDefinition.Entry(conditions, node));
        }
        if (start.isEmpty()) {
            problems.error("start", "keine Startregel");
        } else if (!fallback) {
            problems.error("start", "braucht eine Regel ohne Bedingung als Rueckfall");
        }

        // Verweise und Endlosschleifen pruefen
        for (DialogueDefinition.Node node : nodes.values()) {
            for (int i = 0; i < node.choices().size(); i++) {
                String next = node.choices().get(i).next();
                if (!next.equals(DialogueDefinition.END) && !nodes.containsKey(next)) {
                    problems.error("nodes." + node.name() + ".choices[" + i + "]", "unbekannter Knoten '" + next + "'");
                }
            }
            if (!node.next().isEmpty() && !node.next().equals(DialogueDefinition.END) && !nodes.containsKey(node.next())) {
                problems.error("nodes." + node.name() + ".next", "unbekannter Knoten '" + node.next() + "'");
            }
        }
        for (DialogueDefinition.Node node : nodes.values()) {
            // Knoten ohne Antworten laufen von selbst weiter; eine Schleife nur aus solchen Knoten endet nie.
            Set<String> seen = new HashSet<>();
            DialogueDefinition.Node current = node;
            while (current != null && current.choices().isEmpty() && !current.next().isEmpty() && !current.next().equals(DialogueDefinition.END)) {
                if (!seen.add(current.name())) {
                    problems.error("nodes." + node.name(), "Endlosschleife ohne Antworten ueber " + seen);
                    break;
                }
                current = nodes.get(current.next());
            }
        }
        return new DialogueDefinition(id, speaker, List.copyOf(start), Map.copyOf(nodes));
    }

    private static DialogueDefinition.Node parseNode(String name, String speaker, JsonObject json, Problems problems) {
        String where = "nodes." + name;
        String nodeSpeaker = string(json, "speaker", speaker, problems, where);
        String portrait = string(json, "portrait", "neutral", problems, where);
        if (!DialogueDefinition.PORTRAITS.contains(portrait)) {
            problems.error(where + ".portrait", "unbekannte Pose '" + portrait + "', erlaubt: " + DialogueDefinition.PORTRAITS);
        }
        List<String> text = new ArrayList<>();
        if (json.has("text") && json.get("text").isJsonArray()) {
            json.getAsJsonArray("text").forEach(element -> text.add(element.getAsString()));
        } else if (json.has("text")) {
            text.add(json.get("text").getAsString());
        }
        if (text.isEmpty()) {
            problems.error(where + ".text", "fehlt");
        }
        List<Action> onEnter = json.has("do") ? actions(json.get("do"), problems, where + ".do") : List.of();
        List<DialogueDefinition.Choice> choices = new ArrayList<>();
        if (json.has("choices")) {
            JsonArray array = json.getAsJsonArray("choices");
            if (array.size() > DialogueDefinition.MAX_CHOICES) {
                problems.error(where + ".choices", "hoechstens " + DialogueDefinition.MAX_CHOICES + " Antworten");
            }
            for (int i = 0; i < array.size(); i++) {
                String w = where + ".choices[" + i + "]";
                JsonObject choice = array.get(i).getAsJsonObject();
                choices.add(new DialogueDefinition.Choice(
                        string(choice, "text", "", problems, w),
                        string(choice, "next", DialogueDefinition.END, problems, w),
                        choice.has("if") ? conditions(choice.get("if"), problems, w + ".if") : List.of(),
                        choice.has("do") ? actions(choice.get("do"), problems, w + ".do") : List.of(),
                        choice.has("show_locked") && choice.get("show_locked").getAsBoolean(),
                        string(choice, "locked_hint", "", problems, w)));
                if (choices.get(i).text().isEmpty()) {
                    problems.error(w + ".text", "fehlt");
                }
            }
        }
        String next = string(json, "next", "", problems, where);
        if (!choices.isEmpty() && !next.isEmpty()) {
            problems.error(where, "'next' und 'choices' zugleich; 'next' gilt nur ohne Antworten");
        }
        return new DialogueDefinition.Node(name, nodeSpeaker, portrait, List.copyOf(text), onEnter, List.copyOf(choices), next);
    }

    // ------------------------------------------------------------------ Bedingungen

    public static List<Condition> conditions(JsonElement json, Problems problems, String where) {
        List<Condition> list = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                list.add(condition(array.get(i), problems, where + "[" + i + "]"));
            }
        } else {
            list.add(condition(json, problems, where));
        }
        return List.copyOf(list);
    }

    public static Condition condition(JsonElement json, Problems problems, String where) {
        if (!json.isJsonObject() || json.getAsJsonObject().size() != 1) {
            problems.error(where, "Bedingung muss ein Objekt mit genau einem Schluessel sein");
            return Condition.ALWAYS;
        }
        Map.Entry<String, JsonElement> entry = json.getAsJsonObject().entrySet().iterator().next();
        String type = entry.getKey();
        JsonElement value = entry.getValue();
        try {
            switch (type) {
                case "chapter_min" -> {
                    int n = value.getAsInt();
                    return context -> context.data().chapter(context.uuid()) >= n;
                }
                case "chapter_max" -> {
                    int n = value.getAsInt();
                    return context -> context.data().chapter(context.uuid()) <= n;
                }
                case "ending" -> {
                    StoryEnding ending = ending(value.getAsString(), problems, where);
                    return context -> context.data().ending() == ending;
                }
                case "branch" -> {
                    StoryEnding branch = ending(value.getAsString(), problems, where);
                    return context -> context.data().branch(context.uuid()) == branch;
                }
                case "title" -> {
                    String title = value.getAsString();
                    if (!TITLES.contains(title)) {
                        problems.error(where, "unbekannter Titel '" + title + "', erlaubt: " + TITLES);
                    }
                    return context -> title.equals(titleOf(context.data(), context));
                }
                case "has_item" -> {
                    ItemStack stack = itemStack(value, problems, where);
                    String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    int count = stack.getCount();
                    return context -> Quests.countItem(context.player(), item) >= count;
                }
                case "flag" -> {
                    String flag = value.getAsString();
                    return context -> context.data().hasFlag(context.uuid(), flag);
                }
                case "not_flag" -> {
                    String flag = value.getAsString();
                    return context -> !context.data().hasFlag(context.uuid(), flag);
                }
                case "quest_active" -> {
                    String quest = questRef(value.getAsString(), problems);
                    return context -> {
                        QuestProgress progress = context.data().quest(context.uuid(), quest);
                        return progress != null && progress.active();
                    };
                }
                case "quest_done" -> {
                    String quest = questRef(value.getAsString(), problems);
                    return context -> {
                        QuestProgress progress = context.data().quest(context.uuid(), quest);
                        return progress != null && progress.everCompleted();
                    };
                }
                case "quest_new" -> {
                    // Kann angenommen werden: nie begonnen, oder wiederholbar und gerade nicht aktiv
                    String quest = questRef(value.getAsString(), problems);
                    return context -> {
                        QuestDefinition definition = Quests.get(quest);
                        return definition != null && Quests.canStart(context.player(), definition);
                    };
                }
                case "quest_step" -> {
                    JsonObject object = value.getAsJsonObject();
                    String quest = questRef(object.get("quest").getAsString(), problems);
                    int step = object.get("step").getAsInt() - 1;
                    return context -> {
                        QuestProgress progress = context.data().quest(context.uuid(), quest);
                        return progress != null && progress.active() && progress.step() == step;
                    };
                }
                case "epilogue" -> {
                    String epilogue = value.getAsString();
                    return context -> context.data().epilogue(context.uuid()).equals(epilogue);
                }
                case "any" -> {
                    List<Condition> any = conditions(value, problems, where + ".any");
                    return context -> any.stream().anyMatch(condition -> condition.test(context));
                }
                case "not" -> {
                    Condition inner = condition(value, problems, where + ".not");
                    return context -> !inner.test(context);
                }
                default -> {
                    problems.error(where, "unbekannte Bedingung '" + type + "', erlaubt: " + CONDITIONS);
                    return Condition.ALWAYS;
                }
            }
        } catch (RuntimeException e) {
            problems.error(where, "Bedingung '" + type + "' unlesbar: " + e.getMessage());
            return Condition.ALWAYS;
        }
    }

    private static String titleOf(ShardlineWorldData data, DialogueContext context) {
        return data.isKeeper(context.uuid()) ? "keeper" : data.isRuler(context.uuid()) ? "ruler" : "none";
    }

    // ------------------------------------------------------------------ Aktionen

    public static List<Action> actions(JsonElement json, Problems problems, String where) {
        List<Action> list = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                list.add(action(array.get(i), problems, where + "[" + i + "]"));
            }
        } else {
            list.add(action(json, problems, where));
        }
        return List.copyOf(list);
    }

    public static Action action(JsonElement json, Problems problems, String where) {
        if (!json.isJsonObject() || json.getAsJsonObject().size() != 1) {
            problems.error(where, "Aktion muss ein Objekt mit genau einem Schluessel sein");
            return context -> { };
        }
        Map.Entry<String, JsonElement> entry = json.getAsJsonObject().entrySet().iterator().next();
        String type = entry.getKey();
        JsonElement value = entry.getValue();
        try {
            switch (type) {
                case "give_item" -> {
                    ItemStack stack = itemStack(value, problems, where);
                    return context -> {
                        ItemStack copy = stack.copy();
                        if (!context.player().getInventory().add(copy)) {
                            context.player().drop(copy, false);
                        }
                    };
                }
                case "take_item" -> {
                    ItemStack stack = itemStack(value, problems, where);
                    return context -> takeItem(context.player(), stack.getItem(), stack.getCount());
                }
                case "set_flag" -> {
                    String flag = value.getAsString();
                    return context -> context.data().addFlag(context.uuid(), flag);
                }
                case "clear_flag" -> {
                    String flag = value.getAsString();
                    return context -> context.data().removeFlag(context.uuid(), flag);
                }
                case "advance_chapter" -> {
                    int chapter = value.getAsInt();
                    if (chapter < 1 || chapter > eu.shardline.mod.story.StoryChapter.MAX) {
                        problems.error(where, "Kapitel " + chapter + " gibt es nicht");
                    }
                    return context -> StoryManager.advance(context.player(), chapter);
                }
                case "start_quest" -> {
                    String quest = questRef(value.getAsString(), problems);
                    return context -> Quests.start(context.player(), quest);
                }
                case "complete_quest_step" -> {
                    String quest = questRef(value.getAsString(), problems);
                    return context -> Quests.completeStep(context.player(), quest);
                }
                case "open_trade" -> {
                    return context -> {
                        if (context.npc() instanceof DialogueNpc npc) {
                            npc.openTrade(context.player());
                        }
                    };
                }
                case "npc_state" -> {
                    String state = value.getAsString();
                    if (!NPC_STATES.contains(state)) {
                        problems.error(where, "unbekannter NPC-Zustand '" + state + "', erlaubt: " + NPC_STATES);
                        return context -> { };
                    }
                    return context -> {
                        if (context.npc() instanceof DialogueNpc npc) {
                            npc.applyState(state, context.player());
                        }
                    };
                }
                case "play_sound" -> {
                    String sound = value.isJsonObject() ? value.getAsJsonObject().get("sound").getAsString() : value.getAsString();
                    float pitch = value.isJsonObject() && value.getAsJsonObject().has("pitch") ? value.getAsJsonObject().get("pitch").getAsFloat() : 1.0F;
                    ResourceLocation id = ResourceLocation.tryParse(sound);
                    if (id == null || !BuiltInRegistries.SOUND_EVENT.containsKey(id)) {
                        problems.error(where, "unbekannter Sound '" + sound + "'");
                        return context -> { };
                    }
                    SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(id);
                    return context -> context.player().playNotifySound(event, SoundSource.NEUTRAL, 0.8F, pitch);
                }
                case "run_command_safe" -> {
                    String command = value.getAsString().trim();
                    String problem = unsafeReason(command);
                    if (problem != null) {
                        problems.error(where, "Befehl abgelehnt: " + problem);
                        return context -> { };
                    }
                    return context -> runSafe(context.player(), command);
                }
                case "set_epilogue" -> {
                    String epilogue = value.getAsString();
                    return context -> StoryManager.setEpilogue(context.player(), epilogue);
                }
                case "give_xp" -> {
                    int xp = value.getAsInt();
                    return context -> context.player().giveExperiencePoints(xp);
                }
                default -> {
                    problems.error(where, "unbekannte Aktion '" + type + "', erlaubt: " + ACTIONS);
                    return context -> { };
                }
            }
        } catch (RuntimeException e) {
            problems.error(where, "Aktion '" + type + "' unlesbar: " + e.getMessage());
            return context -> { };
        }
    }

    /** Nimmt genau {@code count} Stueck, oder gar nichts, wenn der Spieler zu wenige hat. */
    public static boolean takeItem(ServerPlayer player, Item item, int count) {
        if (Quests.countItem(player, BuiltInRegistries.ITEM.getKey(item).toString()) < count) {
            return false;
        }
        int left = count;
        for (ItemStack stack : player.getInventory().items) {
            if (left > 0 && stack.is(item)) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (left > 0 && stack.is(item)) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
        return true;
    }

    /**
     * Grund, warum ein Befehl nicht sicher ist, oder null. Erlaubt sind nur Befehle, die mit einem Praefix aus
     * {@code dialogue.commandWhitelist} beginnen, ohne Verkettung, ohne {@code execute} und nur mit dem Ziel {@code @s}.
     */
    public static String unsafeReason(String command) {
        String lower = command.toLowerCase(Locale.ROOT);
        if (lower.startsWith("/")) {
            return "ohne fuehrenden Schraegstrich angeben";
        }
        if (lower.contains(";") || lower.contains("\n") || lower.contains("execute")) {
            return "Verkettung und execute sind verboten";
        }
        for (int i = lower.indexOf('@'); i >= 0; i = lower.indexOf('@', i + 1)) {
            if (i + 1 >= lower.length() || lower.charAt(i + 1) != 's') {
                return "nur das Ziel @s ist erlaubt";
            }
        }
        for (String prefix : ShardlineConfig.DIALOGUE_COMMAND_WHITELIST.get()) {
            if (lower.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                return null;
            }
        }
        return "nicht in dialogue.commandWhitelist";
    }

    private static void runSafe(ServerPlayer player, String command) {
        if (unsafeReason(command) != null) {
            return;   // Whitelist kann sich seit dem Laden geaendert haben
        }
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(2).withSuppressedOutput(), command);
    }

    // ------------------------------------------------------------------ Auftraege

    public static QuestDefinition parseQuest(String id, JsonObject json, Problems problems) {
        int chapterMin = json.has("chapter_min") ? json.get("chapter_min").getAsInt() : 0;
        StoryEnding branch = json.has("branch") ? ending(json.get("branch").getAsString(), problems, "branch") : StoryEnding.NONE;
        boolean repeatable = json.has("repeatable") && json.get("repeatable").getAsBoolean();
        String giver = string(json, "giver", "", problems, "giver");
        List<QuestDefinition.Step> steps = new ArrayList<>();
        JsonArray stepJson = json.has("steps") && json.get("steps").isJsonArray() ? json.getAsJsonArray("steps") : new JsonArray();
        for (int i = 0; i < stepJson.size(); i++) {
            String where = "steps[" + i + "]";
            JsonObject step = stepJson.get(i).getAsJsonObject();
            String type = string(step, "type", "", problems, where);
            String target = string(step, "target", "", problems, where);
            int count = step.has("count") ? step.get("count").getAsInt() : 1;
            if (!QuestDefinition.STEP_TYPES.contains(type)) {
                problems.error(where, "unbekannter Schritt '" + type + "', erlaubt: " + QuestDefinition.STEP_TYPES);
            }
            if (type.equals("collect")) {
                ResourceLocation item = ResourceLocation.tryParse(target);
                if (item == null || !BuiltInRegistries.ITEM.containsKey(item)) {
                    problems.error(where, "unbekanntes Item '" + target + "'");
                }
            }
            if (type.equals("kill") && !target.isEmpty() && !target.startsWith("#")) {
                ResourceLocation entity = ResourceLocation.tryParse(target);
                if (entity == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entity)) {
                    problems.error(where, "unbekanntes Wesen '" + target + "'");
                }
            }
            if (count < 1) {
                problems.error(where, "count muss mindestens 1 sein");
            }
            steps.add(new QuestDefinition.Step(type, target, Math.max(1, count)));
        }
        if (steps.isEmpty()) {
            problems.error("steps", "keine Schritte");
        }
        List<ItemStack> rewards = new ArrayList<>();
        if (json.has("rewards")) {
            JsonArray array = json.getAsJsonArray("rewards");
            for (int i = 0; i < array.size(); i++) {
                rewards.add(itemStack(array.get(i), problems, "rewards[" + i + "]"));
            }
        }
        int xp = json.has("xp") ? json.get("xp").getAsInt() : 0;
        List<String> flags = new ArrayList<>();
        if (json.has("flags")) {
            json.getAsJsonArray("flags").forEach(element -> flags.add(element.getAsString()));
        }
        return new QuestDefinition(id, chapterMin, branch, repeatable, giver, List.copyOf(steps), List.copyOf(rewards), xp, List.copyOf(flags));
    }

    /** Prueft nach dem Laden aller Auftraege, ob jeder Verweis aufloest. */
    public static void checkQuestRefs(Problems problems, Set<String> knownQuests) {
        for (String quest : problems.questRefs) {
            if (!knownQuests.contains(quest)) {
                problems.error("quests", "unbekannter Auftrag '" + quest + "'");
            }
        }
    }

    // ------------------------------------------------------------------ Hilfen

    private static String questRef(String quest, Problems problems) {
        problems.questRefs.add(quest);
        return quest;
    }

    private static StoryEnding ending(String id, Problems problems, String where) {
        if (!ENDINGS.contains(id)) {
            problems.error(where, "unbekanntes Ende '" + id + "', erlaubt: " + ENDINGS);
        }
        return StoryEnding.byId(id);
    }

    private static ItemStack itemStack(JsonElement json, Problems problems, String where) {
        String id = json.isJsonObject() ? json.getAsJsonObject().get("item").getAsString() : json.getAsString();
        int count = json.isJsonObject() && json.getAsJsonObject().has("count") ? json.getAsJsonObject().get("count").getAsInt() : 1;
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null || !BuiltInRegistries.ITEM.containsKey(location)) {
            problems.error(where, "unbekanntes Item '" + id + "'");
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(location), Math.max(1, count));
    }

    private static String string(JsonObject json, String key, String fallback, Problems problems, String where) {
        if (!json.has(key)) {
            return fallback;
        }
        JsonElement element = json.get(key);
        if (!element.isJsonPrimitive()) {
            problems.error(where + "." + key, "muss ein Text sein");
            return fallback;
        }
        return element.getAsString();
    }

    /** Hilfe fuer Tests: Fehler mehrerer Dateien zusammenfassen. */
    public static Map<String, List<String>> group(List<Problems> list) {
        Map<String, List<String>> map = new HashMap<>();
        for (Problems problems : list) {
            if (!problems.ok()) {
                map.put(problems.file, problems.errors);
            }
        }
        return map;
    }
}
