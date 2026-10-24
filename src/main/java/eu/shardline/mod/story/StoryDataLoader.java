package eu.shardline.mod.story;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.story.dialogue.DialogueDefinition;
import eu.shardline.mod.story.dialogue.DialogueParser;
import eu.shardline.mod.story.dialogue.Dialogues;
import eu.shardline.mod.story.quest.QuestDefinition;
import eu.shardline.mod.story.quest.Quests;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Laedt {@code data/<ns>/quests/*.json} und {@code data/<ns>/dialogues/*.json} beim Serverstart und bei {@code /reload}.
 * Auftraege zuerst, damit Dialoge ihre Verweise pruefen koennen. Fehlerhafte Dateien werden mit Pfad, Stelle und
 * Grund im Log gemeldet und uebersprungen; der Rest bleibt nutzbar.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class StoryDataLoader extends SimplePreparableReloadListener<StoryDataLoader.Raw> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter DIALOGUES = FileToIdConverter.json("dialogues");
    private static final FileToIdConverter QUESTS = FileToIdConverter.json("quests");

    /** Rohdaten aus der Vorbereitung (Hintergrund-Thread). */
    public record Raw(Map<ResourceLocation, JsonElement> dialogues, Map<ResourceLocation, JsonElement> quests, List<String> errors) {
    }

    @SubscribeEvent
    static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new StoryDataLoader());
    }

    @Override
    protected Raw prepare(ResourceManager manager, ProfilerFiller profiler) {
        List<String> errors = new ArrayList<>();
        return new Raw(read(manager, DIALOGUES, errors), read(manager, QUESTS, errors), errors);
    }

    private static Map<ResourceLocation, JsonElement> read(ResourceManager manager, FileToIdConverter converter, List<String> errors) {
        Map<ResourceLocation, JsonElement> map = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : converter.listMatchingResources(manager).entrySet()) {
            ResourceLocation id = converter.fileToId(entry.getKey());
            try (Reader reader = entry.getValue().openAsReader()) {
                map.put(id, JsonParser.parseReader(reader));
            } catch (Exception e) {
                errors.add(entry.getKey() + ": kein gueltiges JSON (" + e.getMessage() + ")");
            }
        }
        return map;
    }

    @Override
    protected void apply(Raw raw, ResourceManager manager, ProfilerFiller profiler) {
        Result result = load(raw);
        Quests.setDefinitions(result.quests());
        Dialogues.set(result.dialogues(), result.errors());
        result.errors().forEach(error -> LOGGER.error("[Shardline] Story-Daten: {}", error));
        LOGGER.info("[Shardline] {} Dialoge und {} Auftraege geladen, {} Fehler", result.dialogues().size(), result.quests().size(),
                result.errors().size());
    }

    /** Ergebnis des Ladens (auch fuer Tests mit eigenen Rohdaten). */
    public record Result(Map<ResourceLocation, DialogueDefinition> dialogues, Map<String, QuestDefinition> quests, List<String> errors) {
    }

    public static Result load(Raw raw) {
        List<String> errors = new ArrayList<>(raw.errors());
        Map<String, QuestDefinition> quests = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : raw.quests().entrySet()) {
            String id = questId(entry.getKey());
            DialogueParser.Problems problems = new DialogueParser.Problems("quests/" + entry.getKey());
            if (!entry.getValue().isJsonObject()) {
                problems.error("", "kein Objekt");
            } else {
                QuestDefinition quest = DialogueParser.parseQuest(id, entry.getValue().getAsJsonObject(), problems);
                if (problems.ok()) {
                    quests.put(id, quest);
                }
            }
            errors.addAll(problems.errors());
        }
        Map<ResourceLocation, DialogueDefinition> dialogues = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : raw.dialogues().entrySet()) {
            DialogueParser.Problems problems = new DialogueParser.Problems("dialogues/" + entry.getKey());
            if (!entry.getValue().isJsonObject()) {
                problems.error("", "kein Objekt");
            } else {
                DialogueDefinition dialogue = DialogueParser.parseDialogue(entry.getKey(), entry.getValue().getAsJsonObject(), problems);
                DialogueParser.checkQuestRefs(problems, quests.keySet());
                if (problems.ok()) {
                    dialogues.put(entry.getKey(), dialogue);
                }
            }
            errors.addAll(problems.errors());
        }
        return new Result(dialogues, quests, errors);
    }

    /** Auftrags-ID: Dateiname im Namensraum shardline, sonst "ns:name". */
    public static String questId(ResourceLocation file) {
        return file.getNamespace().equals(Shardline.MODID) ? file.getPath() : file.toString();
    }
}
