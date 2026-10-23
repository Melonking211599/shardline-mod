package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Loot-Tabellen der Strukturtruhen.
 */
public final class ModLootTables {
    public static final ResourceKey<LootTable> RUIN_TOWER_CHEST = key("chests/ruin_tower");
    public static final ResourceKey<LootTable> RUIN_TOWER_TREASURE = key("chests/ruin_tower_treasure");
    public static final ResourceKey<LootTable> COLOSSUS_ARENA_CHEST = key("chests/colossus_arena");
    public static final ResourceKey<LootTable> QUEEN_ARENA_CHEST = key("chests/queen_arena");

    private ModLootTables() {
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Shardline.id(path));
    }
}
