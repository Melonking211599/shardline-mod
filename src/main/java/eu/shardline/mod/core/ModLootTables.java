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
    public static final ResourceKey<LootTable> KEEPER_SHRINE_CHEST = key("chests/keeper_shrine");
    public static final ResourceKey<LootTable> TRADER_OUTPOST_CHEST = key("chests/trader_outpost");
    public static final ResourceKey<LootTable> ASH_CRYPT_CHEST = key("chests/ash_crypt");
    public static final ResourceKey<LootTable> CRYSTAL_GEODE_CHEST = key("chests/crystal_geode");
    public static final ResourceKey<LootTable> SHARD_CRATER_CHEST = key("chests/shard_crater");
    public static final ResourceKey<LootTable> SURVIVOR_CAMP_CHEST = key("chests/survivor_camp");
    public static final ResourceKey<LootTable> KEEPER_LIBRARY_CHEST = key("chests/keeper_library");
    public static final ResourceKey<LootTable> KEEPER_RUIN_CHEST = key("chests/keeper_ruin");

    private ModLootTables() {
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Shardline.id(path));
    }
}
