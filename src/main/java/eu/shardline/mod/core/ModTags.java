package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class ModTags {
    private ModTags() {
    }

    public static final class Blocks {
        /** Boden, auf dem die Pflanzen der Splitterweite wachsen. */
        public static final TagKey<Block> SHARD_PLANT_SOIL = TagKey.create(Registries.BLOCK, Shardline.id("shard_plant_soil"));
        public static final TagKey<Block> CRYSTAL_LOGS = TagKey.create(Registries.BLOCK, Shardline.id("crystal_logs"));
        /** Bloecke, deren Abbau einen Kristallgolem wuetend macht. */
        public static final TagKey<Block> ANGERS_CRYSTAL_GOLEM = TagKey.create(Registries.BLOCK, Shardline.id("angers_crystal_golem"));
        public static final TagKey<Block> ORES_SHARD = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/shard"));
        public static final TagKey<Block> STORAGE_BLOCKS_SHARD = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/shard"));
        /** Vom Shardvirus befallbare Bloecke, nach Art des infizierten Blocks (per Datapack erweiterbar). */
        public static final TagKey<Block> INFECTABLE_STONE = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/stone"));
        public static final TagKey<Block> INFECTABLE_DIRT = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/dirt"));
        public static final TagKey<Block> INFECTABLE_GRASS = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/grass"));
        public static final TagKey<Block> INFECTABLE_LOG = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/log"));
        public static final TagKey<Block> INFECTABLE_LEAVES = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/leaves"));
        public static final TagKey<Block> INFECTABLE_SAND = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/sand"));
        public static final TagKey<Block> INFECTABLE_DEEPSLATE = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/deepslate"));
        public static final TagKey<Block> INFECTABLE_PLANKS = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/planks"));
        public static final TagKey<Block> INFECTABLE_NETHERRACK = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/netherrack"));
        public static final TagKey<Block> INFECTABLE_BLACKSTONE = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/blackstone"));
        public static final TagKey<Block> INFECTABLE_SOUL_SOIL = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/soul_soil"));
        public static final TagKey<Block> INFECTABLE_NYLIUM = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/nylium"));
        public static final TagKey<Block> INFECTABLE_WART_BLOCK = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/wart_block"));
        public static final TagKey<Block> INFECTABLE_END_STONE = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/end_stone"));
        public static final TagKey<Block> INFECTABLE_PURPUR = TagKey.create(Registries.BLOCK, Shardline.id("virus_infectable/purpur"));
        /** Alle infizierten Bloecke. */
        public static final TagKey<Block> INFECTED = TagKey.create(Registries.BLOCK, Shardline.id("infected"));

        private Blocks() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> CRYSTAL_LOGS = TagKey.create(Registries.ITEM, Shardline.id("crystal_logs"));
        public static final TagKey<Item> SHARD_SET = TagKey.create(Registries.ITEM, Shardline.id("shard_set"));
        public static final TagKey<Item> COLOSSUS_SET = TagKey.create(Registries.ITEM, Shardline.id("colossus_set"));
        public static final TagKey<Item> INGOTS_SHARD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/shard"));
        public static final TagKey<Item> RAW_MATERIALS_SHARD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "raw_materials/shard"));
        public static final TagKey<Item> ORES_SHARD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores/shard"));
        public static final TagKey<Item> STORAGE_BLOCKS_SHARD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/shard"));

        private Items() {
        }
    }

    public static final class EntityTypes {
        /** Ziele des wiederholbaren Auftrags "Nachtwache". */
        public static final TagKey<EntityType<?>> NIGHT_WATCH_TARGETS = TagKey.create(Registries.ENTITY_TYPE, Shardline.id("night_watch_targets"));
        /** Wesen, gegen die Splitterbann zusaetzlichen Schaden macht. */
        public static final TagKey<EntityType<?>> SENSITIVE_TO_SHARD_BANE = TagKey.create(Registries.ENTITY_TYPE, Shardline.id("sensitive_to_shard_bane"));
        /** Wesen der Splitterweite, die nach dem Ende "Beherrschen" die Splitterherrscher nicht angreifen. */
        public static final TagKey<EntityType<?>> OBEYS_RULER = TagKey.create(Registries.ENTITY_TYPE, Shardline.id("obeys_ruler"));

        private EntityTypes() {
        }
    }

    public static final class DamageTypes {
        /** Schaden aus Splittern, Kristallen und dem Shardvirus (Kristallschutz wirkt dagegen). */
        public static final TagKey<DamageType> SHARD_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, Shardline.id("shard_damage"));

        private DamageTypes() {
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> IS_SPLITTERWEITE = TagKey.create(Registries.BIOME, Shardline.id("is_splitterweite"));
        public static final TagKey<Biome> HAS_RUIN_TOWER = TagKey.create(Registries.BIOME, Shardline.id("has_structure/ruin_tower"));
        public static final TagKey<Biome> HAS_COLOSSUS_ARENA = TagKey.create(Registries.BIOME, Shardline.id("has_structure/colossus_arena"));
        public static final TagKey<Biome> HAS_QUEEN_ARENA = TagKey.create(Registries.BIOME, Shardline.id("has_structure/queen_arena"));
        public static final TagKey<Biome> HAS_KEEPER_SHRINE = TagKey.create(Registries.BIOME, Shardline.id("has_structure/keeper_shrine"));
        public static final TagKey<Biome> HAS_TRADER_OUTPOST = TagKey.create(Registries.BIOME, Shardline.id("has_structure/trader_outpost"));
        public static final TagKey<Biome> HAS_ASH_CRYPT = TagKey.create(Registries.BIOME, Shardline.id("has_structure/ash_crypt"));
        public static final TagKey<Biome> HAS_CRYSTAL_GEODE = TagKey.create(Registries.BIOME, Shardline.id("has_structure/crystal_geode"));
        public static final TagKey<Biome> HAS_SHARD_CRATER = TagKey.create(Registries.BIOME, Shardline.id("has_structure/shard_crater"));
        public static final TagKey<Biome> HAS_KEEPER_RUIN = TagKey.create(Registries.BIOME, Shardline.id("has_structure/keeper_ruin"));

        private Biomes() {
        }
    }

    public static final class Structures {
        /** Ziel der Karte des Splitterhaendlers. */
        public static final TagKey<Structure> ON_COLOSSUS_MAPS = TagKey.create(Registries.STRUCTURE, Shardline.id("on_colossus_maps"));

        private Structures() {
        }
    }
}
