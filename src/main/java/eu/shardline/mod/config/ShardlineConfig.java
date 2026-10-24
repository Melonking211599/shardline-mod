package eu.shardline.mod.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Gemeinsame Konfiguration (config/shardline-common.toml). Wird auf Server und Client geladen.
 */
public final class ShardlineConfig {
    public static final ModConfigSpec SPEC;

    /** IDs aller Mobs, deren Spawnrate einzeln konfigurierbar ist. */
    public static final List<String> SPAWNING_MOBS = List.of(
            "shard_creeper", "crystal_golem", "glow_moth", "ash_walker", "shard_scout",
            "void_ray", "crystal_spider", "shard_trader", "shadow_fox", "shattered_knight", "crystal_snail");

    // Spawns
    public static final ModConfigSpec.BooleanValue OVERWORLD_SPAWNS;
    public static final ModConfigSpec.DoubleValue OVERWORLD_SPAWN_CHANCE;
    public static final Map<String, ModConfigSpec.DoubleValue> SPAWN_CHANCES = new LinkedHashMap<>();

    // Mobs
    public static final ModConfigSpec.DoubleValue MOB_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SCOUT_PROJECTILE_DAMAGE;
    public static final ModConfigSpec.IntValue CREEPER_SHARD_COUNT;
    public static final ModConfigSpec.DoubleValue CREEPER_SHARD_DAMAGE;
    public static final ModConfigSpec.BooleanValue CREEPER_BREAKS_BLOCKS;
    public static final ModConfigSpec.IntValue GOLEM_AGGRO_RADIUS;
    public static final ModConfigSpec.IntValue ASH_WALKER_BLINDNESS_SECONDS;
    public static final ModConfigSpec.DoubleValue SPIDER_WEB_CHANCE;
    public static final ModConfigSpec.IntValue FOX_INVISIBILITY_SECONDS;

    // Bosse
    public static final ModConfigSpec.DoubleValue COLOSSUS_HEALTH;
    public static final ModConfigSpec.DoubleValue COLOSSUS_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue COLOSSUS_SLAM_DAMAGE;
    public static final ModConfigSpec.DoubleValue COLOSSUS_PILLAR_DAMAGE;
    public static final ModConfigSpec.IntValue COLOSSUS_MINION_COUNT;
    public static final ModConfigSpec.DoubleValue QUEEN_HEALTH;
    public static final ModConfigSpec.DoubleValue QUEEN_PROJECTILE_DAMAGE;
    public static final ModConfigSpec.IntValue QUEEN_CLONE_COUNT;
    public static final ModConfigSpec.DoubleValue QUEEN_VOID_ZONE_DAMAGE;

    // Ausruestung
    public static final ModConfigSpec.DoubleValue SHARD_PROJECTILE_RESISTANCE;
    public static final ModConfigSpec.IntValue COLOSSUS_BONUS_HEARTS;
    public static final ModConfigSpec.IntValue SHIELD_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue SHIELD_ABSORPTION_HEARTS;
    public static final ModConfigSpec.IntValue SHIELD_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue SHIELD_PUSH_RADIUS;
    public static final ModConfigSpec.IntValue GAUNTLET_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue GAUNTLET_DAMAGE;
    public static final ModConfigSpec.IntValue HEART_BLINK_DISTANCE;
    public static final ModConfigSpec.IntValue HEART_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue SHARDBLADE_VOLLEY_COOLDOWN_SECONDS;

    // Dimension
    public static final ModConfigSpec.IntValue PORTAL_SLOW_FALLING_SECONDS;

    // Story
    public static final ModConfigSpec.BooleanValue CHAPTER_TITLES;
    public static final ModConfigSpec.BooleanValue TRADER_DIALOGUE;
    public static final ModConfigSpec.IntValue DIALOGUE_LINE_DELAY_TICKS;
    public static final ModConfigSpec.IntValue LORE_PAGE_PRICE;

    // Virus
    public static final ModConfigSpec.BooleanValue VIRUS_ENABLED;
    public static final ModConfigSpec.BooleanValue VIRUS_SPREAD_PAUSED;
    public static final ModConfigSpec.DoubleValue VIRUS_SPREAD_CHANCE;
    public static final ModConfigSpec.IntValue VIRUS_SPREAD_ATTEMPTS;
    public static final ModConfigSpec.IntValue VIRUS_SPREAD_RANGE;
    public static final ModConfigSpec.IntValue VIRUS_MAX_RANGE;
    public static final ModConfigSpec.IntValue VIRUS_MAX_BLOCKS_PER_TICK;
    public static final ModConfigSpec.IntValue VIRUS_QUEUE_LIMIT;
    public static final ModConfigSpec.DoubleValue VIRUS_ORE_SEED_CHANCE;
    public static final ModConfigSpec.DoubleValue VIRUS_PORTAL_SEED_CHANCE;
    public static final ModConfigSpec.IntValue VIRUS_PORTAL_SEED_RADIUS;
    public static final ModConfigSpec.DoubleValue INFECTED_MOB_SEED_CHANCE;
    public static final ModConfigSpec.DoubleValue MOB_CONVERSION_CHANCE;
    public static final ModConfigSpec.BooleanValue VILLAGERS_FLEE;
    public static final ModConfigSpec.BooleanValue VILLAGERS_TRANSFORM;
    public static final ModConfigSpec.BooleanValue ANIMALS_TRANSFORM;
    public static final ModConfigSpec.IntValue VIRUS_OVERWORLD_SEED_MINUTES;
    public static final ModConfigSpec.DoubleValue FEVER_CHANCE_ON_INFECTED_BLOCK;
    public static final ModConfigSpec.IntValue FEVER_DURATION_SECONDS;
    public static final ModConfigSpec.DoubleValue FEVER_DAMAGE;
    public static final ModConfigSpec.BooleanValue FEVER_CAN_KILL;
    public static final ModConfigSpec.BooleanValue FEVER_CURED_BY_MILK;

    // Gegenmittel
    public static final ModConfigSpec.IntValue ANCHOR_RADIUS;
    public static final ModConfigSpec.IntValue ANCHOR_PURIFY_PER_PULSE;
    public static final ModConfigSpec.IntValue ANCHOR_START_RADIUS;
    public static final ModConfigSpec.IntValue ANCHOR_GROW_SECONDS;
    public static final ModConfigSpec.IntValue ESSENCE_RADIUS;
    public static final ModConfigSpec.IntValue IMMUNITY_ESSENCE_SECONDS;
    public static final ModConfigSpec.IntValue IMMUNITY_ANCHOR_SECONDS;
    public static final ModConfigSpec.IntValue HEART_PURIFY_RADIUS;
    public static final ModConfigSpec.DoubleValue VIRUS_HEART_HEALTH;
    public static final ModConfigSpec.EnumValue<HeartDifficulty> VIRUS_HEART_DIFFICULTY;
    public static final ModConfigSpec.IntValue HEART_SPAWN_THRESHOLD;
    public static final ModConfigSpec.IntValue HEART_SPAWN_COOLDOWN_MINUTES;
    public static final ModConfigSpec.IntValue HEARTS_FOR_CORE;

    // Ende
    public static final ModConfigSpec.EnumValue<EndingMode> ENDING_MODE;
    public static final ModConfigSpec.IntValue VOTE_DURATION_SECONDS;
    public static final ModConfigSpec.BooleanValue TITLE_PREFIX;

    // Faehigkeiten des Splitterherrschers
    public static final ModConfigSpec.IntValue SHARD_CALL_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue SHARD_CALL_MAX_MOBS;
    public static final ModConfigSpec.IntValue SHARD_CALL_STRENGTH_SECONDS;
    public static final ModConfigSpec.IntValue CRYSTAL_WAVE_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue CRYSTAL_WAVE_RADIUS;
    public static final ModConfigSpec.DoubleValue CRYSTAL_WAVE_DAMAGE;
    public static final ModConfigSpec.IntValue CRYSTAL_WAVE_FEVER_SECONDS;
    public static final ModConfigSpec.BooleanValue VIRUS_RUN;

    /** Wer am Altar ueber das Ende entscheidet. */
    /** Schwierigkeit des Virusherzens: Abklingzeiten, Schaden und wie viele Infizierte es in Phase 3 ruft. */
    public enum HeartDifficulty {
        EASY(1.4, 0.7, 2),
        NORMAL(1.0, 1.0, 4),
        HARD(0.7, 1.35, 6);

        private final double cooldown;
        private final double damage;
        private final int minions;

        HeartDifficulty(double cooldown, double damage, int minions) {
            this.cooldown = cooldown;
            this.damage = damage;
            this.minions = minions;
        }

        public double cooldown() {
            return this.cooldown;
        }

        public double damage() {
            return this.damage;
        }

        public int minions() {
            return this.minions;
        }
    }

    public enum EndingMode {
        /** Der erste Spieler mit dem Kern des Bruchs entscheidet allein. */
        FIRST_PLAYER,
        /** Alle Spieler, die Kapitel 6 erreicht haben, stimmen ab. */
        VOTE
    }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.translation(key("spawns")).push("spawns");
        OVERWORLD_SPAWNS = b.comment("Ob Splitterkriecher, Aschewandler, Kristallspinne, Schattenfuchs und Leuchtmotte nachts selten in der Oberwelt spawnen.")
                .translation(key("overworld_spawns"))
                .define("overworldSpawns", true);
        OVERWORLD_SPAWN_CHANCE = b.comment("Zusaetzlicher Faktor (0-1) fuer alle Spawns in der Oberwelt.")
                .translation(key("overworld_spawn_chance"))
                .defineInRange("overworldSpawnChance", 0.35, 0.0, 1.0);
        b.comment("Chance (0-1), dass ein Spawnversuch pro Mob gelingt. 0 deaktiviert den natuerlichen Spawn.")
                .translation(key("spawn_chances")).push("spawnChances");
        for (String mob : SPAWNING_MOBS) {
            SPAWN_CHANCES.put(mob, b.translation("entity.shardline." + mob).defineInRange(mob, 1.0, 0.0, 1.0));
        }
        b.pop();
        b.pop();

        b.translation(key("mobs")).push("mobs");
        MOB_DAMAGE_MULTIPLIER = b.comment("Multiplikator fuer den Nahkampfschaden aller normalen Shardline-Mobs.")
                .translation(key("mob_damage_multiplier"))
                .defineInRange("mobDamageMultiplier", 1.0, 0.0, 10.0);
        SCOUT_PROJECTILE_DAMAGE = b.comment("Schaden eines Splitter-Projektils des Shard-Spaehers.")
                .translation(key("scout_projectile_damage"))
                .defineInRange("scoutProjectileDamage", 5.0, 0.0, 100.0);
        CREEPER_SHARD_COUNT = b.comment("Anzahl der Kristallsplitter, in die der Splitterkriecher zerspringt.")
                .translation(key("creeper_shard_count"))
                .defineInRange("creeperShardCount", 10, 0, 64);
        CREEPER_SHARD_DAMAGE = b.comment("Schaden pro Kristallsplitter des Splitterkriechers.")
                .translation(key("creeper_shard_damage"))
                .defineInRange("creeperShardDamage", 4.0, 0.0, 100.0);
        CREEPER_BREAKS_BLOCKS = b.comment("Ob die Explosion des Splitterkriechers Bloecke zerstoert.")
                .translation(key("creeper_breaks_blocks"))
                .define("creeperBreaksBlocks", false);
        GOLEM_AGGRO_RADIUS = b.comment("Radius in Bloecken, in dem Bergbau einen Kristallgolem wuetend macht.")
                .translation(key("golem_aggro_radius"))
                .defineInRange("golemAggroRadius", 12, 1, 64);
        ASH_WALKER_BLINDNESS_SECONDS = b.comment("Dauer der Blindheit durch den Aschewandler in Sekunden.")
                .translation(key("ash_walker_blindness_seconds"))
                .defineInRange("ashWalkerBlindnessSeconds", 3, 0, 60);
        SPIDER_WEB_CHANCE = b.comment("Chance (0-1), dass die Kristallspinne beim Treffer ein Kristallnetz erzeugt.")
                .translation(key("spider_web_chance"))
                .defineInRange("spiderWebChance", 0.3, 0.0, 1.0);
        FOX_INVISIBILITY_SECONDS = b.comment("Wie lange der Schattenfuchs unsichtbar wird, in Sekunden.")
                .translation(key("fox_invisibility_seconds"))
                .defineInRange("foxInvisibilitySeconds", 6, 1, 60);
        b.pop();

        b.translation(key("bosses")).push("bosses");
        COLOSSUS_HEALTH = b.comment("Lebenspunkte des Kristallkolosses (2 = 1 Herz).")
                .translation(key("colossus_health"))
                .defineInRange("colossusHealth", 400.0, 1.0, 100000.0);
        COLOSSUS_ATTACK_DAMAGE = b.comment("Nahkampfschaden des Kristallkolosses.")
                .translation(key("colossus_attack_damage"))
                .defineInRange("colossusAttackDamage", 18.0, 0.0, 1000.0);
        COLOSSUS_SLAM_DAMAGE = b.comment("Schaden des Bodenstampfers.")
                .translation(key("colossus_slam_damage"))
                .defineInRange("colossusSlamDamage", 14.0, 0.0, 1000.0);
        COLOSSUS_PILLAR_DAMAGE = b.comment("Schaden einer Kristallsaeule.")
                .translation(key("colossus_pillar_damage"))
                .defineInRange("colossusPillarDamage", 10.0, 0.0, 1000.0);
        COLOSSUS_MINION_COUNT = b.comment("Wie viele Splitterkriecher der Koloss in Phase 2 pro Ruf beschwoert.")
                .translation(key("colossus_minion_count"))
                .defineInRange("colossusMinionCount", 3, 0, 16);
        QUEEN_HEALTH = b.comment("Lebenspunkte der Splitterkoenigin.")
                .translation(key("queen_health"))
                .defineInRange("queenHealth", 600.0, 1.0, 100000.0);
        QUEEN_PROJECTILE_DAMAGE = b.comment("Schaden eines Projektils der Splitterkoenigin.")
                .translation(key("queen_projectile_damage"))
                .defineInRange("queenProjectileDamage", 8.0, 0.0, 1000.0);
        QUEEN_CLONE_COUNT = b.comment("Anzahl der Klone, die die Koenigin in Phase 2 erzeugt.")
                .translation(key("queen_clone_count"))
                .defineInRange("queenCloneCount", 3, 0, 10);
        QUEEN_VOID_ZONE_DAMAGE = b.comment("Schaden pro Sekunde in einer Void-Zone.")
                .translation(key("queen_void_zone_damage"))
                .defineInRange("queenVoidZoneDamage", 4.0, 0.0, 1000.0);
        b.pop();

        b.translation(key("equipment")).push("equipment");
        SHARD_PROJECTILE_RESISTANCE = b.comment("Schadensreduktion (0-1) gegen Splitter-Projektile mit vollem Shard-Set.")
                .translation(key("shard_projectile_resistance"))
                .defineInRange("shardProjectileResistance", 0.5, 0.0, 1.0);
        COLOSSUS_BONUS_HEARTS = b.comment("Zusaetzliche Herzen durch das volle Koloss-Set.")
                .translation(key("colossus_bonus_hearts"))
                .defineInRange("colossusBonusHearts", 4, 0, 50);
        SHIELD_DURATION_SECONDS = b.comment("Dauer des Kristallschilds in Sekunden.")
                .translation(key("shield_duration_seconds"))
                .defineInRange("shieldDurationSeconds", 6, 1, 120);
        SHIELD_ABSORPTION_HEARTS = b.comment("Absorptionsherzen des Kristallschilds (gerade Zahl empfohlen).")
                .translation(key("shield_absorption_hearts"))
                .defineInRange("shieldAbsorptionHearts", 8, 2, 40);
        SHIELD_COOLDOWN_SECONDS = b.comment("Abklingzeit des Kristallschilds in Sekunden.")
                .translation(key("shield_cooldown_seconds"))
                .defineInRange("shieldCooldownSeconds", 60, 0, 3600);
        SHIELD_PUSH_RADIUS = b.comment("Radius der Rueckstosswelle des Kristallschilds.")
                .translation(key("shield_push_radius"))
                .defineInRange("shieldPushRadius", 4.0, 0.0, 16.0);
        GAUNTLET_COOLDOWN_SECONDS = b.comment("Abklingzeit der Kolossfaust in Sekunden.")
                .translation(key("gauntlet_cooldown_seconds"))
                .defineInRange("gauntletCooldownSeconds", 10, 0, 600);
        GAUNTLET_DAMAGE = b.comment("Flaechenschaden der Kolossfaust.")
                .translation(key("gauntlet_damage"))
                .defineInRange("gauntletDamage", 10.0, 0.0, 1000.0);
        HEART_BLINK_DISTANCE = b.comment("Maximale Sprungweite des Herzens der Splitterweite in Bloecken.")
                .translation(key("heart_blink_distance"))
                .defineInRange("heartBlinkDistance", 12, 2, 64);
        HEART_COOLDOWN_SECONDS = b.comment("Abklingzeit des Herzens der Splitterweite in Sekunden.")
                .translation(key("heart_cooldown_seconds"))
                .defineInRange("heartCooldownSeconds", 8, 0, 600);
        SHARDBLADE_VOLLEY_COOLDOWN_SECONDS = b.comment("Abklingzeit der Splittersalve der Splitterklinge in Sekunden.")
                .translation(key("shardblade_volley_cooldown_seconds"))
                .defineInRange("shardbladeVolleyCooldownSeconds", 3, 0, 600);
        b.pop();

        b.translation(key("dimension")).push("dimension");
        PORTAL_SLOW_FALLING_SECONDS = b.comment("Sekunden Sanfter Fall nach der Reise durch das Splitterportal (Schutz vor dem Void).")
                .translation(key("portal_slow_falling_seconds"))
                .defineInRange("portalSlowFallingSeconds", 10, 0, 120);
        b.pop();

        b.translation(key("story")).push("story");
        CHAPTER_TITLES = b.comment("Zeigt beim Erreichen eines Kapitels einen Titel auf dem Bildschirm.")
                .translation(key("chapter_titles"))
                .define("chapterTitles", true);
        TRADER_DIALOGUE = b.comment("Der Splitterhaendler erzaehlt beim Ansprechen die Geschichte des aktuellen Kapitels.")
                .translation(key("trader_dialogue"))
                .define("traderDialogue", true);
        DIALOGUE_LINE_DELAY_TICKS = b.comment("Abstand zwischen zwei Dialogzeilen in Ticks (20 = 1 Sekunde).")
                .translation(key("dialogue_line_delay_ticks"))
                .defineInRange("dialogueLineDelayTicks", 50, 0, 400);
        LORE_PAGE_PRICE = b.comment("Shard-Splitter, die der Splitterhaendler fuer eine lose Lore-Seite zahlt.")
                .translation(key("lore_page_price"))
                .defineInRange("lorePagePrice", 2, 0, 64);
        b.pop();

        b.translation(key("virus")).push("virus");
        VIRUS_ENABLED = b.comment("Ob der Tod der Splitterkoenigin den Shardvirus freisetzt.")
                .translation(key("virus_enabled"))
                .define("enabled", true);
        VIRUS_SPREAD_PAUSED = b.comment("Haelt die Ausbreitung dauerhaft an (zusaetzlich zu /shardline virus pause).")
                .translation(key("virus_spread_paused"))
                .define("spreadPaused", false);
        VIRUS_SPREAD_CHANCE = b.comment("Chance (0-1), dass ein infizierter Block bei einem Random Tick versucht, sich auszubreiten.")
                .translation(key("virus_spread_chance"))
                .defineInRange("spreadChance", 0.75, 0.0, 1.0);
        VIRUS_SPREAD_ATTEMPTS = b.comment("Wie viele Nachbarbloecke pro Ausbreitungsversuch geprueft werden.")
                .translation(key("virus_spread_attempts"))
                .defineInRange("spreadAttempts", 2, 1, 8);
        VIRUS_SPREAD_RANGE = b.comment("Maximale Sprungweite eines Ausbreitungsschritts in Bloecken.")
                .translation(key("virus_spread_range"))
                .defineInRange("spreadRange", 2, 1, 8);
        VIRUS_MAX_RANGE = b.comment("Maximale Entfernung (Bloecke) vom naechsten Infektionsherd. 0 = unbegrenzt.")
                .translation(key("virus_max_range"))
                .defineInRange("maxRange", 0, 0, 1000000);
        VIRUS_MAX_BLOCKS_PER_TICK = b.comment("Hoechstens so viele Bloecke werden pro Tick und Dimension umgewandelt (Performance).")
                .translation(key("virus_max_blocks_per_tick"))
                .defineInRange("maxBlocksPerTick", 80, 1, 4096);
        VIRUS_QUEUE_LIMIT = b.comment("Groesse der Warteschlange fuer Umwandlungen pro Dimension. Ueberschuss wird verworfen.")
                .translation(key("virus_queue_limit"))
                .defineInRange("queueLimit", 8192, 64, 262144);
        VIRUS_ORE_SEED_CHANCE = b.comment("Chance (0-1) pro Random Tick, dass Shard-Erz seine Umgebung infiziert.")
                .translation(key("virus_ore_seed_chance"))
                .defineInRange("oreSeedChance", 0.2, 0.0, 1.0);
        VIRUS_PORTAL_SEED_CHANCE = b.comment("Chance (0-1) pro Random Tick, dass ein Splitterportal in der Oberwelt seine Umgebung infiziert.")
                .translation(key("virus_portal_seed_chance"))
                .defineInRange("portalSeedChance", 0.75, 0.0, 1.0);
        VIRUS_PORTAL_SEED_RADIUS = b.comment("Radius um Splitterportale, in dem neue Infektionen entstehen.")
                .translation(key("virus_portal_seed_radius"))
                .defineInRange("portalSeedRadius", 6, 1, 32);
        INFECTED_MOB_SEED_CHANCE = b.comment("Chance (0-1) pro Sekunde, dass ein infizierter Mob den Boden unter sich infiziert.")
                .translation(key("infected_mob_seed_chance"))
                .defineInRange("infectedMobSeedChance", 0.065, 0.0, 1.0);
        MOB_CONVERSION_CHANCE = b.comment("Chance (0-1) pro Sekunde, dass Zombies, Kuehe, Schafe, Schweine und Dorfbewohner auf infiziertem Boden kristallisieren.")
                .translation(key("mob_conversion_chance"))
                .defineInRange("mobConversionChance", 0.02, 0.0, 1.0);
        VILLAGERS_FLEE = b.comment("Dorfbewohner fliehen vor infizierten Mobs.")
                .translation(key("villagers_flee"))
                .define("villagersFlee", true);
        VILLAGERS_TRANSFORM = b.comment("Dorfbewohner verwandeln sich: auf infiziertem Boden in kristallisierte Dorfbewohner, durch einen Shard-Zombie getoetet in Shard-Zombies.")
                .translation(key("villagers_transform"))
                .define("villagersTransform", true);
        ANIMALS_TRANSFORM = b.comment("Kuehe, Schafe und Schweine auf infiziertem Boden kristallisieren.")
                .translation(key("animals_transform"))
                .define("animalsTransform", true);
        VIRUS_OVERWORLD_SEED_MINUTES = b.comment("Alle so viele Minuten setzt der Virus nach dem Bruch einen neuen Herd nahe einem Spieler in der Oberwelt (0 = aus).")
                .translation(key("overworld_seed_minutes"))
                .defineInRange("overworldSeedMinutes", 4, 0, 1440);
        FEVER_CHANCE_ON_INFECTED_BLOCK = b.comment("Chance (0-1) pro Sekunde auf infiziertem Boden, Splitterfieber zu bekommen.")
                .translation(key("fever_chance_on_infected_block"))
                .defineInRange("feverChanceOnInfectedBlock", 0.04, 0.0, 1.0);
        FEVER_DURATION_SECONDS = b.comment("Dauer des Splitterfiebers in Sekunden.")
                .translation(key("fever_duration_seconds"))
                .defineInRange("feverDurationSeconds", 30, 1, 3600);
        FEVER_DAMAGE = b.comment("Schaden des Splitterfiebers alle drei Sekunden.")
                .translation(key("fever_damage"))
                .defineInRange("feverDamage", 1.0, 0.0, 100.0);
        FEVER_CAN_KILL = b.comment("Ob Splitterfieber toeten kann. Sonst bleibt mindestens ein halbes Herz uebrig.")
                .translation(key("fever_can_kill"))
                .define("feverCanKill", false);
        FEVER_CURED_BY_MILK = b.comment("Ob Milch das Splitterfieber heilt. Die Reinigungsessenz heilt es immer.")
                .translation(key("fever_cured_by_milk"))
                .define("feverCuredByMilk", false);
        b.pop();

        b.translation(key("remedies")).push("remedies");
        ANCHOR_RADIUS = b.comment("Endgueltiger Schutzradius eines Reinheitsankers in Bloecken (die Zone waechst bis dahin).")
                .translation(key("anchor_radius"))
                .defineInRange("anchorRadius", 24, 1, 128);
        ANCHOR_PURIFY_PER_PULSE = b.comment("Wie viele zufaellige Positionen ein Reinheitsanker alle zwei Sekunden reinigt.")
                .translation(key("anchor_purify_per_pulse"))
                .defineInRange("anchorPurifyPerPulse", 24, 0, 512);
        ANCHOR_START_RADIUS = b.comment("Schutzradius direkt nach dem Setzen eines Reinheitsankers.")
                .translation(key("anchor_start_radius"))
                .defineInRange("anchorStartRadius", 6, 1, 128);
        ANCHOR_GROW_SECONDS = b.comment("Sekunden, bis die Zone eines Reinheitsankers den vollen Radius erreicht (0 = sofort).")
                .translation(key("anchor_grow_seconds"))
                .defineInRange("anchorGrowSeconds", 300, 0, 36000);
        ESSENCE_RADIUS = b.comment("Reinigungsradius der Reinigungsessenz.")
                .translation(key("essence_radius"))
                .defineInRange("essenceRadius", 6, 1, 32);
        IMMUNITY_ESSENCE_SECONDS = b.comment("Wie lange die Reinigungsessenz Immunitaet gegen Splitterfieber verleiht (Sekunden, 0 = keine).")
                .translation(key("immunity_essence_seconds"))
                .defineInRange("immunityEssenceSeconds", 120, 0, 3600);
        IMMUNITY_ANCHOR_SECONDS = b.comment("Immunitaet, die ein Reinheitsanker alle zwei Sekunden in seiner Zone auffrischt (Sekunden, 0 = keine).")
                .translation(key("immunity_anchor_seconds"))
                .defineInRange("immunityAnchorSeconds", 15, 0, 600);
        HEART_PURIFY_RADIUS = b.comment("Radius, den ein besiegtes Virusherz reinigt.")
                .translation(key("heart_purify_radius"))
                .defineInRange("heartPurifyRadius", 40, 4, 128);
        VIRUS_HEART_HEALTH = b.comment("Lebenspunkte eines Virusherzens.")
                .translation(key("virus_heart_health"))
                .defineInRange("virusHeartHealth", 200.0, 1.0, 100000.0);
        VIRUS_HEART_DIFFICULTY = b.comment("Schwierigkeit des Virusherzens (Phasen bei 2/3 und 1/3 Leben): EASY, NORMAL oder HARD."
                        + " Wirkt auf Abklingzeiten, Schaden und die Zahl der beschworenen Infizierten.")
                .translation(key("virus_heart_difficulty"))
                .defineEnum("virusHeartDifficulty", HeartDifficulty.NORMAL);
        HEART_SPAWN_THRESHOLD = b.comment("Ab so vielen infizierten Bloecken in einem Chunk kann dort ein Virusherz entstehen.")
                .translation(key("heart_spawn_threshold"))
                .defineInRange("heartSpawnThreshold", 160, 16, 65536);
        HEART_SPAWN_COOLDOWN_MINUTES = b.comment("Mindestabstand zwischen zwei natuerlich entstehenden Virusherzen (Minuten, pro Welt).")
                .translation(key("heart_spawn_cooldown_minutes"))
                .defineInRange("heartSpawnCooldownMinutes", 8, 0, 1440);
        HEARTS_FOR_CORE = b.comment("Wie viele Virusherzen ein Spieler besiegen muss, um den Kern des Bruchs zu erhalten.")
                .translation(key("hearts_for_core"))
                .defineInRange("heartsForCore", 3, 1, 64);
        b.pop();

        b.translation(key("ending")).push("ending");
        ENDING_MODE = b.comment("FIRST_PLAYER: Der erste Spieler am Altar entscheidet. VOTE: Alle Spieler mit Kapitel 6 stimmen ab.")
                .translation(key("ending_mode"))
                .defineEnum("mode", EndingMode.VOTE);
        VOTE_DURATION_SECONDS = b.comment("Dauer der Abstimmung in Sekunden.")
                .translation(key("vote_duration_seconds"))
                .defineInRange("voteDurationSeconds", 120, 10, 3600);
        TITLE_PREFIX = b.comment("Zeigt die Titel Hueter und Splitterherrscher als Praefix im Namen (Scoreboard-Team).")
                .translation(key("title_prefix"))
                .define("titlePrefix", true);
        b.pop();

        b.translation(key("abilities")).push("abilities");
        SHARD_CALL_COOLDOWN_SECONDS = b.comment("Abklingzeit des Splitterrufs in Sekunden.")
                .translation(key("shard_call_cooldown_seconds"))
                .defineInRange("shardCallCooldownSeconds", 90, 0, 3600);
        SHARD_CALL_MAX_MOBS = b.comment("Wie viele infizierte Diener der Splitterruf herbeiholt.")
                .translation(key("shard_call_max_mobs"))
                .defineInRange("shardCallMaxMobs", 3, 1, 16);
        SHARD_CALL_STRENGTH_SECONDS = b.comment("Dauer der Staerke durch den Splitterruf in Sekunden.")
                .translation(key("shard_call_strength_seconds"))
                .defineInRange("shardCallStrengthSeconds", 10, 0, 600);
        CRYSTAL_WAVE_COOLDOWN_SECONDS = b.comment("Abklingzeit der Kristallwelle in Sekunden.")
                .translation(key("crystal_wave_cooldown_seconds"))
                .defineInRange("crystalWaveCooldownSeconds", 45, 0, 3600);
        CRYSTAL_WAVE_RADIUS = b.comment("Radius der Kristallwelle.")
                .translation(key("crystal_wave_radius"))
                .defineInRange("crystalWaveRadius", 6.0, 1.0, 32.0);
        CRYSTAL_WAVE_DAMAGE = b.comment("Schaden der Kristallwelle.")
                .translation(key("crystal_wave_damage"))
                .defineInRange("crystalWaveDamage", 8.0, 0.0, 1000.0);
        CRYSTAL_WAVE_FEVER_SECONDS = b.comment("Wie lange die Kristallwelle getroffene Gegner mit Splitterfieber belegt.")
                .translation(key("crystal_wave_fever_seconds"))
                .defineInRange("crystalWaveFeverSeconds", 4, 0, 120);
        VIRUS_RUN = b.comment("Virus-Lauf: Splitterherrscher erhalten auf infiziertem Boden Tempo II und Regeneration I.")
                .translation(key("virus_run"))
                .define("virusRun", true);
        b.pop();

        SPEC = b.build();
    }

    private ShardlineConfig() {
    }

    private static String key(String name) {
        return "shardline.configuration." + name;
    }

    public static double spawnChance(String mobId) {
        ModConfigSpec.DoubleValue value = SPAWN_CHANCES.get(mobId);
        return value == null ? 1.0 : value.get();
    }
}
