package eu.shardline.mod.story.quest;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Auftraege (nur Server). Die Definitionen kommen aus {@code data/shardline/quests/*.json}
 * ({@link eu.shardline.mod.story.StoryDataLoader}); der Stand pro Spieler liegt in {@link ShardlineWorldData}.
 *
 * <p>Fortschritt entsteht auf drei Wegen: Ereignisse ({@link #trigger}, z. B. "kill" beim Tod eines Wesens),
 * eine Pruefung alle {@code dialogue.questCheckTicks} fuer Sammel- und Ortsschritte, und Dialogaktionen
 * ({@link #completeStep}). Belohnungen gibt es beim Abschluss des letzten Schritts.</p>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class Quests {
    private static Map<String, QuestDefinition> definitions = Map.of();

    private Quests() {
    }

    public static void setDefinitions(Map<String, QuestDefinition> loaded) {
        definitions = Map.copyOf(new LinkedHashMap<>(loaded));
    }

    @Nullable
    public static QuestDefinition get(String id) {
        return definitions.get(id);
    }

    public static Collection<QuestDefinition> all() {
        return definitions.values();
    }

    // ------------------------------------------------------------------ Zustand

    public static boolean isActive(ShardlineWorldData data, ServerPlayer player, String id) {
        QuestProgress progress = data.quest(player.getUUID(), id);
        return progress != null && progress.active();
    }

    public static boolean isDone(ShardlineWorldData data, ServerPlayer player, String id) {
        QuestProgress progress = data.quest(player.getUUID(), id);
        return progress != null && progress.everCompleted();
    }

    /** Ob der Spieler den Auftrag jetzt annehmen kann (Kapitel, Zweig, nicht aktiv, nicht erledigt oder wiederholbar). */
    public static boolean canStart(ServerPlayer player, QuestDefinition quest) {
        ShardlineWorldData data = StoryManager.data(player.server);
        QuestProgress progress = data.quest(player.getUUID(), quest.id());
        if (progress != null && (progress.active() || progress.everCompleted() && !quest.repeatable())) {
            return false;
        }
        if (data.chapter(player.getUUID()) < quest.chapterMin()) {
            return false;
        }
        return quest.branch() == StoryEnding.NONE || data.branch(player.getUUID()) == quest.branch();
    }

    // ------------------------------------------------------------------ Aenderungen

    /** Nimmt einen Auftrag an. @return true, wenn er neu begonnen wurde */
    public static boolean start(ServerPlayer player, String id) {
        QuestDefinition quest = get(id);
        if (quest == null || !canStart(player, quest)) {
            return false;
        }
        StoryManager.data(player.server).questOrCreate(player.getUUID(), id).start();
        player.sendSystemMessage(Component.translatable("quest.shardline.started", quest.title()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(" ▸ ").append(quest.stepText(0)).withStyle(ChatFormatting.GRAY));
        player.playNotifySound(ModSounds.QUEST_UPDATE.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
        check(player);
        QuestSync.send(player);
        return true;
    }

    /** Schliesst den aktuellen Schritt ab (Dialogaktion). @return true, wenn der Auftrag aktiv war */
    public static boolean completeStep(ServerPlayer player, String id) {
        QuestDefinition quest = get(id);
        QuestProgress progress = StoryManager.data(player.server).quest(player.getUUID(), id);
        if (quest == null || progress == null || !progress.active()) {
            return false;
        }
        advance(player, quest, progress);
        return true;
    }

    /** Ob ein aktiver Auftrag des Spielers gerade auf einem Schritt dieses Typs mit diesem Ziel steht. */
    public static boolean hasActiveStep(ServerPlayer player, String type, String target) {
        ShardlineWorldData data = StoryManager.data(player.server);
        for (Map.Entry<String, QuestProgress> entry : data.quests(player.getUUID()).entrySet()) {
            QuestDefinition quest = get(entry.getKey());
            QuestProgress progress = entry.getValue();
            if (quest != null && progress.active() && progress.step() < quest.steps().size()) {
                QuestDefinition.Step step = quest.steps().get(progress.step());
                if (step.type().equals(type) && step.target().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Ereignis melden, z. B. trigger(spieler, "kill", "shardline:shard_zombie"). */
    public static void trigger(ServerPlayer player, String type, String target) {
        trigger(player, type, target::equals, 1);
    }

    /**
     * Ereignis melden. Jeder aktive Auftrag, dessen aktueller Schritt diesen Typ hat und dessen Ziel passt (leeres
     * Ziel passt immer), zaehlt um {@code amount} weiter.
     */
    public static void trigger(ServerPlayer player, String type, Predicate<String> targetMatches, int amount) {
        ShardlineWorldData data = StoryManager.data(player.server);
        for (Map.Entry<String, QuestProgress> entry : new ArrayList<>(data.quests(player.getUUID()).entrySet())) {
            QuestProgress progress = entry.getValue();
            QuestDefinition quest = get(entry.getKey());
            if (quest == null || !progress.active() || progress.step() >= quest.steps().size()) {
                continue;
            }
            QuestDefinition.Step step = quest.steps().get(progress.step());
            if (!step.type().equals(type) || !(step.target().isEmpty() || targetMatches.test(step.target()))) {
                continue;
            }
            progress.setCount(Math.min(step.count(), progress.count() + amount));
            data.setDirty();
            if (progress.count() >= step.count()) {
                advance(player, quest, progress);
            } else {
                player.displayClientMessage(Component.translatable("quest.shardline.progress", quest.title(), progress.count(), step.count())
                        .withStyle(ChatFormatting.GOLD), true);
                QuestSync.send(player);
            }
        }
    }

    private static void advance(ServerPlayer player, QuestDefinition quest, QuestProgress progress) {
        ShardlineWorldData data = StoryManager.data(player.server);
        if (progress.step() + 1 < quest.steps().size()) {
            progress.nextStep();
            data.setDirty();
            player.sendSystemMessage(Component.translatable("quest.shardline.step", quest.title()).withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal(" ▸ ").append(quest.stepText(progress.step())).withStyle(ChatFormatting.GRAY));
            player.playNotifySound(ModSounds.QUEST_UPDATE.get(), SoundSource.PLAYERS, 0.7F, 1.2F);
            check(player);
        } else {
            progress.complete();
            data.setDirty();
            data.addFlag(player.getUUID(), "quest_done:" + quest.id());
            for (ItemStack reward : quest.rewards()) {
                ItemStack copy = reward.copy();
                if (!player.getInventory().add(copy)) {
                    player.drop(copy, false);
                }
            }
            if (quest.xp() > 0) {
                player.giveExperiencePoints(quest.xp());
            }
            quest.rewardFlags().forEach(flag -> data.addFlag(player.getUUID(), flag));
            player.sendSystemMessage(Component.translatable("quest.shardline.completed", quest.title()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            player.playNotifySound(ModSounds.QUEST_COMPLETE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        QuestSync.send(player);
    }

    /** Setzt einen Auftrag zurueck (Befehl, Tests). */
    public static void reset(ServerPlayer player, String id) {
        ShardlineWorldData data = StoryManager.data(player.server);
        data.removeQuest(player.getUUID(), id);
        data.removeFlag(player.getUUID(), "quest_done:" + id);
        QuestSync.send(player);
    }

    // ------------------------------------------------------------------ Pruefung (Sammeln, Orte)

    /** Prueft Sammel- und Ortsschritte des Spielers (keine Weltsuche: nur Inventar und die Struktur am eigenen Standort). */
    public static void check(ServerPlayer player) {
        ShardlineWorldData data = StoryManager.data(player.server);
        for (Map.Entry<String, QuestProgress> entry : new ArrayList<>(data.quests(player.getUUID()).entrySet())) {
            QuestProgress progress = entry.getValue();
            QuestDefinition quest = get(entry.getKey());
            if (quest == null || !progress.active() || progress.step() >= quest.steps().size()) {
                continue;
            }
            QuestDefinition.Step step = quest.steps().get(progress.step());
            switch (step.type()) {
                case "collect" -> {
                    int have = countItem(player, step.target());
                    if (have != progress.count()) {
                        progress.setCount(Math.min(have, step.count()));
                        data.setDirty();
                        if (have >= step.count()) {
                            advance(player, quest, progress);
                        } else {
                            QuestSync.send(player);
                        }
                    }
                }
                case "visit_structure" -> {
                    if (isInStructure(player, step.target())) {
                        progress.setCount(step.count());
                        advance(player, quest, progress);
                    }
                }
                default -> {
                }
            }
        }
    }

    public static int countItem(ServerPlayer player, String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return 0;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean isInStructure(ServerPlayer player, String structureId) {
        ResourceLocation id = ResourceLocation.tryParse(structureId);
        if (id == null) {
            return false;
        }
        Structure structure = player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE).get(id);
        return structure != null && player.serverLevel().structureManager().getStructureWithPieceAt(player.blockPosition(), structure).isValid();
    }

    /** Passt ein Wesen auf ein Ziel ("ns:name" oder "#ns:tag")? */
    public static boolean matchesEntity(EntityType<?> type, String target) {
        if (target.startsWith("#")) {
            ResourceLocation tag = ResourceLocation.tryParse(target.substring(1));
            return tag != null && type.is(TagKey.create(Registries.ENTITY_TYPE, tag));
        }
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals(target);
    }

    public static List<QuestDefinition> activeQuests(ServerPlayer player) {
        ShardlineWorldData data = StoryManager.data(player.server);
        List<QuestDefinition> list = new ArrayList<>();
        data.quests(player.getUUID()).forEach((id, progress) -> {
            QuestDefinition quest = get(id);
            if (quest != null && progress.active()) {
                list.add(quest);
            }
        });
        return list;
    }

    // ------------------------------------------------------------------ Ereignisse

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            EntityType<?> type = event.getEntity().getType();
            trigger(player, "kill", target -> matchesEntity(type, target), 1);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.tickCount % ShardlineConfig.QUEST_CHECK_TICKS.get() == 0
                && !StoryManager.data(player.server).quests(player.getUUID()).isEmpty()) {
            check(player);
        }
    }

    /** Fuer Tests: Strukturschluessel bauen. */
    public static ResourceKey<Structure> structureKey(String id) {
        return ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(id));
    }
}
