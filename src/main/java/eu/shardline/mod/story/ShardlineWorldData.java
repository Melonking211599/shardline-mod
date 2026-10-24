package eu.shardline.mod.story;

import eu.shardline.mod.story.quest.QuestProgress;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Weltweiter Story-Zustand (data/shardline_story.dat in der Oberwelt). Enthaelt den
 * Kapitel-Fortschritt jedes Spielers, den Virus-Zustand, die Reinheitsanker, Infektionsherde
 * und das Ende der Geschichte. Wird nur auf dem Server gelesen und geschrieben.
 */
public class ShardlineWorldData extends SavedData {
    private static final String NAME = "shardline_story";
    /** Obergrenze gespeicherter Infektionsherde pro Dimension. */
    public static final int MAX_HERDS = 256;

    private static final Factory<ShardlineWorldData> FACTORY = new Factory<>(ShardlineWorldData::new, ShardlineWorldData::load);

    // --- Spieler ---
    private final Map<UUID, Integer> chapters = new HashMap<>();
    /** Einmalige Ereignisse pro Spieler: gelesene Buecher, gesehene Visionen, gehoerte Dialoge. */
    private final Map<UUID, Set<String>> flags = new HashMap<>();
    private final Map<UUID, Integer> heartsDefeated = new HashMap<>();
    /** Zweig ab Kapitel 8 (Ende der Welt beim Eintritt), damit ein spaeter zurueckgesetztes Ende den Weg nicht kippt. */
    private final Map<UUID, StoryEnding> branches = new HashMap<>();
    /** Gewaehlter Epilog nach Kapitel 9 (z. B. "stillness", "chronicle", "truce", "dominion"); leer = keiner. */
    private final Map<UUID, String> epilogues = new HashMap<>();
    /** Auftragsstand pro Spieler und Auftrag. */
    private final Map<UUID, Map<String, QuestProgress>> quests = new HashMap<>();

    // --- Virus ---
    private boolean infected;
    private long outbreakTime;
    private boolean paused;
    private long lastHeartSpawn;
    private final Map<ResourceKey<Level>, LongSet> anchors = new HashMap<>();
    /** Schwaeren (Kapitel 7-9) je Dimension, damit Stimmgabel, Kompass und Nachbeben keine Welt absuchen muessen. */
    private final Map<ResourceKey<Level>, LongSet> festeringNodes = new HashMap<>();
    /** Spielzeit, zu der ein Anker gesetzt wurde (fuer den wachsenden Schutzradius). Fehlt sie, gilt der volle Radius. */
    private final Map<ResourceKey<Level>, Long2LongOpenHashMap> anchorSince = new HashMap<>();
    private final Map<ResourceKey<Level>, LongList> herds = new HashMap<>();
    /** Pro Chunk: Anzahl infizierter Bloecke (geschaetzt, steigt beim Infizieren, faellt beim Reinigen). */
    private final Map<ResourceKey<Level>, Long2IntOpenHashMap> chunkInfection = new HashMap<>();

    // --- Ende ---
    private StoryEnding ending = StoryEnding.NONE;
    private final Set<UUID> keepers = new LinkedHashSet<>();
    private final Set<UUID> rulers = new LinkedHashSet<>();

    public static ShardlineWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    // ------------------------------------------------------------------ Kapitel

    public int chapter(UUID player) {
        return this.chapters.getOrDefault(player, 0);
    }

    public void setChapter(UUID player, int chapter) {
        if (chapter <= 0) {
            this.chapters.remove(player);
        } else {
            this.chapters.put(player, Math.min(chapter, StoryChapter.MAX));
        }
        this.setDirty();
    }

    /** Hoechstes Kapitel, das irgendein Spieler in dieser Welt erreicht hat. */
    public int worldChapter() {
        int max = 0;
        for (int chapter : this.chapters.values()) {
            max = Math.max(max, chapter);
        }
        return max;
    }

    public Set<UUID> playersAtLeast(int chapter) {
        Set<UUID> result = new HashSet<>();
        this.chapters.forEach((uuid, value) -> {
            if (value >= chapter) {
                result.add(uuid);
            }
        });
        return result;
    }

    // ------------------------------------------------------------------ Nach dem Ausbruch: Zweig, Epilog, Auftraege

    public StoryEnding branch(UUID player) {
        return this.branches.getOrDefault(player, StoryEnding.NONE);
    }

    public void setBranch(UUID player, StoryEnding branch) {
        if (branch == StoryEnding.NONE) {
            this.branches.remove(player);
        } else {
            this.branches.put(player, branch);
        }
        this.setDirty();
    }

    public String epilogue(UUID player) {
        return this.epilogues.getOrDefault(player, "");
    }

    public void setEpilogue(UUID player, String epilogue) {
        if (epilogue.isEmpty()) {
            this.epilogues.remove(player);
        } else {
            this.epilogues.put(player, epilogue);
        }
        this.setDirty();
    }

    /** Auftragsstand oder null, wenn der Auftrag nie begonnen wurde. */
    @Nullable
    public QuestProgress quest(UUID player, String quest) {
        Map<String, QuestProgress> map = this.quests.get(player);
        return map == null ? null : map.get(quest);
    }

    public Map<String, QuestProgress> quests(UUID player) {
        return this.quests.getOrDefault(player, Map.of());
    }

    /** Legt den Auftragsstand an (oder liefert den vorhandenen). */
    public QuestProgress questOrCreate(UUID player, String quest) {
        this.setDirty();
        return this.quests.computeIfAbsent(player, uuid -> new HashMap<>()).computeIfAbsent(quest, id -> new QuestProgress());
    }

    public void removeQuest(UUID player, String quest) {
        Map<String, QuestProgress> map = this.quests.get(player);
        if (map != null && map.remove(quest) != null) {
            this.setDirty();
        }
    }

    // ------------------------------------------------------------------ Flags

    public boolean hasFlag(UUID player, String flag) {
        Set<String> set = this.flags.get(player);
        return set != null && set.contains(flag);
    }

    /** @return true, wenn das Flag neu gesetzt wurde */
    public boolean addFlag(UUID player, String flag) {
        boolean added = this.flags.computeIfAbsent(player, uuid -> new HashSet<>()).add(flag);
        if (added) {
            this.setDirty();
        }
        return added;
    }

    public int countFlags(UUID player, String prefix) {
        Set<String> set = this.flags.get(player);
        if (set == null) {
            return 0;
        }
        int count = 0;
        for (String flag : set) {
            if (flag.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    public void removeFlag(UUID player, String flag) {
        Set<String> set = this.flags.get(player);
        if (set != null && set.remove(flag)) {
            this.setDirty();
        }
    }

    public void clearFlags(UUID player, String prefix) {
        Set<String> set = this.flags.get(player);
        if (set != null && set.removeIf(flag -> flag.startsWith(prefix))) {
            this.setDirty();
        }
    }

    // ------------------------------------------------------------------ Virusherzen

    public int heartsDefeated(UUID player) {
        return this.heartsDefeated.getOrDefault(player, 0);
    }

    public int addHeartDefeated(UUID player) {
        int count = this.heartsDefeated(player) + 1;
        this.heartsDefeated.put(player, count);
        this.setDirty();
        return count;
    }

    // ------------------------------------------------------------------ Virus

    public boolean isInfected() {
        return this.infected;
    }

    public long outbreakTime() {
        return this.outbreakTime;
    }

    public void setInfected(boolean infected, long gameTime) {
        this.infected = infected;
        this.outbreakTime = infected ? gameTime : 0L;
        this.setDirty();
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        this.setDirty();
    }

    public long lastHeartSpawn() {
        return this.lastHeartSpawn;
    }

    public void setLastHeartSpawn(long gameTime) {
        this.lastHeartSpawn = gameTime;
        this.setDirty();
    }

    public LongSet anchors(ResourceKey<Level> dimension) {
        return this.anchors.computeIfAbsent(dimension, key -> new LongOpenHashSet());
    }

    public LongSet festeringNodes(ResourceKey<Level> dimension) {
        return this.festeringNodes.computeIfAbsent(dimension, key -> new LongOpenHashSet());
    }

    public void addFesteringNode(ResourceKey<Level> dimension, BlockPos pos) {
        if (this.festeringNodes(dimension).add(pos.asLong())) {
            this.setDirty();
        }
    }

    public void removeFesteringNode(ResourceKey<Level> dimension, BlockPos pos) {
        if (this.festeringNodes(dimension).remove(pos.asLong())) {
            this.setDirty();
        }
    }

    public void addAnchor(ResourceKey<Level> dimension, BlockPos pos) {
        if (this.anchors(dimension).add(pos.asLong())) {
            this.setDirty();
        }
    }

    public void removeAnchor(ResourceKey<Level> dimension, BlockPos pos) {
        if (this.anchors(dimension).remove(pos.asLong())) {
            this.setDirty();
        }
        if (this.anchorSince(dimension).remove(pos.asLong()) != this.anchorSince(dimension).defaultReturnValue()) {
            this.setDirty();
        }
    }

    private Long2LongOpenHashMap anchorSince(ResourceKey<Level> dimension) {
        return this.anchorSince.computeIfAbsent(dimension, key -> {
            Long2LongOpenHashMap map = new Long2LongOpenHashMap();
            map.defaultReturnValue(-1L);
            return map;
        });
    }

    /** Merkt sich, wann ein Anker gesetzt wurde. */
    public void setAnchorSince(ResourceKey<Level> dimension, BlockPos pos, long gameTime) {
        this.anchorSince(dimension).put(pos.asLong(), gameTime);
        this.setDirty();
    }

    /** Spielzeit, zu der der Anker gesetzt wurde, oder -1 (alte Anker: voller Radius). */
    public long anchorSince(ResourceKey<Level> dimension, long packedPos) {
        return this.anchorSince(dimension).get(packedPos);
    }

    public LongList herds(ResourceKey<Level> dimension) {
        return this.herds.computeIfAbsent(dimension, key -> new LongArrayList());
    }

    /**
     * Merkt sich einen Infektionsherd, sofern im Umkreis von {@code spacing} Bloecken noch keiner liegt.
     *
     * @return true, wenn ein neuer Herd eingetragen wurde
     */
    public boolean addHerd(ResourceKey<Level> dimension, BlockPos pos, int spacing) {
        LongList list = this.herds(dimension);
        long spacingSqr = (long) spacing * spacing;
        for (int i = 0; i < list.size(); i++) {
            BlockPos other = BlockPos.of(list.getLong(i));
            long dx = other.getX() - pos.getX();
            long dz = other.getZ() - pos.getZ();
            if (dx * dx + dz * dz < spacingSqr) {
                return false;
            }
        }
        if (list.size() >= MAX_HERDS) {
            list.removeLong(0);
        }
        list.add(pos.asLong());
        this.setDirty();
        return true;
    }

    public void removeHerdsNear(ResourceKey<Level> dimension, BlockPos pos, int radius) {
        LongList list = this.herds(dimension);
        long radiusSqr = (long) radius * radius;
        boolean changed = list.removeIf(packed -> {
            BlockPos other = BlockPos.of(packed);
            long dx = other.getX() - pos.getX();
            long dz = other.getZ() - pos.getZ();
            return dx * dx + dz * dz <= radiusSqr;
        });
        if (changed) {
            this.setDirty();
        }
    }

    /** Horizontaler Abstand (quadriert) zum naechsten Herd oder {@link Long#MAX_VALUE}, wenn es keinen gibt. */
    public long distanceSqrToNearestHerd(ResourceKey<Level> dimension, BlockPos pos) {
        LongList list = this.herds.get(dimension);
        long best = Long.MAX_VALUE;
        if (list == null) {
            return best;
        }
        for (int i = 0; i < list.size(); i++) {
            BlockPos other = BlockPos.of(list.getLong(i));
            long dx = other.getX() - pos.getX();
            long dz = other.getZ() - pos.getZ();
            best = Math.min(best, dx * dx + dz * dz);
        }
        return best;
    }

    public Long2IntOpenHashMap chunkInfection(ResourceKey<Level> dimension) {
        return this.chunkInfection.computeIfAbsent(dimension, key -> new Long2IntOpenHashMap());
    }

    public int changeChunkInfection(ResourceKey<Level> dimension, ChunkPos chunk, int delta) {
        Long2IntOpenHashMap map = this.chunkInfection(dimension);
        int value = Math.max(0, map.get(chunk.toLong()) + delta);
        if (value == 0) {
            map.remove(chunk.toLong());
        } else {
            map.put(chunk.toLong(), value);
        }
        this.setDirty();
        return value;
    }

    public int totalInfectedChunks() {
        int total = 0;
        for (Long2IntOpenHashMap map : this.chunkInfection.values()) {
            total += map.size();
        }
        return total;
    }

    public long totalInfectedBlocks() {
        long total = 0;
        for (Long2IntOpenHashMap map : this.chunkInfection.values()) {
            for (int value : map.values()) {
                total += value;
            }
        }
        return total;
    }

    public void clearInfectionStats() {
        this.chunkInfection.clear();
        this.herds.clear();
        this.setDirty();
    }

    // ------------------------------------------------------------------ Ende

    public StoryEnding ending() {
        return this.ending;
    }

    public void setEnding(StoryEnding ending) {
        this.ending = ending;
        this.setDirty();
    }

    public Set<UUID> keepers() {
        return this.keepers;
    }

    public Set<UUID> rulers() {
        return this.rulers;
    }

    public boolean isKeeper(UUID player) {
        return this.keepers.contains(player);
    }

    public boolean isRuler(UUID player) {
        return this.rulers.contains(player);
    }

    public void addKeeper(UUID player) {
        this.rulers.remove(player);
        this.keepers.add(player);
        this.setDirty();
    }

    public void addRuler(UUID player) {
        this.keepers.remove(player);
        this.rulers.add(player);
        this.setDirty();
    }

    public void clearTitles() {
        this.keepers.clear();
        this.rulers.clear();
        this.setDirty();
    }

    // ------------------------------------------------------------------ Speichern

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        Set<UUID> all = new HashSet<>(this.chapters.keySet());
        all.addAll(this.flags.keySet());
        all.addAll(this.heartsDefeated.keySet());
        all.addAll(this.branches.keySet());
        all.addAll(this.epilogues.keySet());
        all.addAll(this.quests.keySet());
        for (UUID uuid : all) {
            CompoundTag player = new CompoundTag();
            player.putInt("Chapter", this.chapter(uuid));
            player.putInt("Hearts", this.heartsDefeated(uuid));
            if (this.branches.containsKey(uuid)) {
                player.putString("Branch", this.branches.get(uuid).id());
            }
            if (this.epilogues.containsKey(uuid)) {
                player.putString("Epilogue", this.epilogues.get(uuid));
            }
            Map<String, QuestProgress> questMap = this.quests.get(uuid);
            if (questMap != null && !questMap.isEmpty()) {
                CompoundTag questTag = new CompoundTag();
                questMap.forEach((id, progress) -> questTag.put(id, progress.save()));
                player.put("Quests", questTag);
            }
            ListTag flagList = new ListTag();
            this.flags.getOrDefault(uuid, Set.of()).forEach(flag -> flagList.add(StringTag.valueOf(flag)));
            player.put("Flags", flagList);
            players.put(uuid.toString(), player);
        }
        tag.put("Players", players);

        tag.putBoolean("Infected", this.infected);
        tag.putLong("OutbreakTime", this.outbreakTime);
        tag.putBoolean("Paused", this.paused);
        tag.putLong("LastHeartSpawn", this.lastHeartSpawn);

        CompoundTag dimensions = new CompoundTag();
        Set<ResourceKey<Level>> keys = new HashSet<>(this.anchors.keySet());
        keys.addAll(this.anchorSince.keySet());
        keys.addAll(this.herds.keySet());
        keys.addAll(this.chunkInfection.keySet());
        keys.addAll(this.festeringNodes.keySet());
        for (ResourceKey<Level> key : keys) {
            CompoundTag dimension = new CompoundTag();
            dimension.put("Anchors", new LongArrayTag(this.anchors(key).toLongArray()));
            Long2LongOpenHashMap since = this.anchorSince(key);
            long[] sincePairs = new long[since.size() * 2];
            int s = 0;
            for (Long2LongMap.Entry entry : since.long2LongEntrySet()) {
                sincePairs[s++] = entry.getLongKey();
                sincePairs[s++] = entry.getLongValue();
            }
            dimension.put("AnchorSince", new LongArrayTag(sincePairs));
            dimension.put("Herds", new LongArrayTag(this.herds(key).toLongArray()));
            dimension.put("FesteringNodes", new LongArrayTag(this.festeringNodes(key).toLongArray()));
            Long2IntOpenHashMap infection = this.chunkInfection(key);
            long[] chunkKeys = new long[infection.size()];
            int[] chunkValues = new int[infection.size()];
            int i = 0;
            for (Long2IntMap.Entry entry : infection.long2IntEntrySet()) {
                chunkKeys[i] = entry.getLongKey();
                chunkValues[i] = entry.getIntValue();
                i++;
            }
            dimension.putLongArray("InfectionChunks", chunkKeys);
            dimension.putIntArray("InfectionCounts", chunkValues);
            dimensions.put(key.location().toString(), dimension);
        }
        tag.put("Dimensions", dimensions);

        tag.putString("Ending", this.ending.id());
        ListTag keeperList = new ListTag();
        this.keepers.forEach(uuid -> keeperList.add(StringTag.valueOf(uuid.toString())));
        tag.put("Keepers", keeperList);
        ListTag rulerList = new ListTag();
        this.rulers.forEach(uuid -> rulerList.add(StringTag.valueOf(uuid.toString())));
        tag.put("Rulers", rulerList);
        return tag;
    }

    public static ShardlineWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        ShardlineWorldData data = new ShardlineWorldData();
        CompoundTag players = tag.getCompound("Players");
        for (String key : players.getAllKeys()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            CompoundTag player = players.getCompound(key);
            if (player.getInt("Chapter") > 0) {
                data.chapters.put(uuid, player.getInt("Chapter"));
            }
            if (player.getInt("Hearts") > 0) {
                data.heartsDefeated.put(uuid, player.getInt("Hearts"));
            }
            // Felder ab "Nach dem Ausbruch": fehlen sie (alte Welt), bleiben die Standardwerte
            StoryEnding branch = StoryEnding.byId(player.getString("Branch"));
            if (branch != StoryEnding.NONE) {
                data.branches.put(uuid, branch);
            }
            if (!player.getString("Epilogue").isEmpty()) {
                data.epilogues.put(uuid, player.getString("Epilogue"));
            }
            CompoundTag questTag = player.getCompound("Quests");
            for (String quest : questTag.getAllKeys()) {
                data.quests.computeIfAbsent(uuid, id -> new HashMap<>()).put(quest, QuestProgress.load(questTag.getCompound(quest)));
            }
            ListTag flagList = player.getList("Flags", Tag.TAG_STRING);
            if (!flagList.isEmpty()) {
                Set<String> set = new HashSet<>();
                for (int i = 0; i < flagList.size(); i++) {
                    set.add(flagList.getString(i));
                }
                data.flags.put(uuid, set);
            }
        }

        data.infected = tag.getBoolean("Infected");
        data.outbreakTime = tag.getLong("OutbreakTime");
        data.paused = tag.getBoolean("Paused");
        data.lastHeartSpawn = tag.getLong("LastHeartSpawn");

        CompoundTag dimensions = tag.getCompound("Dimensions");
        for (String key : dimensions.getAllKeys()) {
            ResourceLocation location = ResourceLocation.tryParse(key);
            if (location == null) {
                continue;
            }
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, location);
            CompoundTag dimension = dimensions.getCompound(key);
            data.anchors(dimensionKey).addAll(LongArrayList.wrap(dimension.getLongArray("Anchors")));
            long[] sincePairs = dimension.getLongArray("AnchorSince");
            for (int s = 0; s + 1 < sincePairs.length; s += 2) {
                data.anchorSince(dimensionKey).put(sincePairs[s], sincePairs[s + 1]);
            }
            data.herds(dimensionKey).addAll(LongArrayList.wrap(dimension.getLongArray("Herds")));
            data.festeringNodes(dimensionKey).addAll(LongArrayList.wrap(dimension.getLongArray("FesteringNodes")));
            long[] chunkKeys = dimension.getLongArray("InfectionChunks");
            int[] chunkValues = dimension.getIntArray("InfectionCounts");
            Long2IntOpenHashMap infection = data.chunkInfection(dimensionKey);
            for (int i = 0; i < Math.min(chunkKeys.length, chunkValues.length); i++) {
                infection.put(chunkKeys[i], chunkValues[i]);
            }
        }

        data.ending = StoryEnding.byId(tag.getString("Ending"));
        ListTag keeperList = tag.getList("Keepers", Tag.TAG_STRING);
        for (int i = 0; i < keeperList.size(); i++) {
            data.keepers.add(UUID.fromString(keeperList.getString(i)));
        }
        ListTag rulerList = tag.getList("Rulers", Tag.TAG_STRING);
        for (int i = 0; i < rulerList.size(); i++) {
            data.rulers.add(UUID.fromString(rulerList.getString(i)));
        }
        return data;
    }
}
