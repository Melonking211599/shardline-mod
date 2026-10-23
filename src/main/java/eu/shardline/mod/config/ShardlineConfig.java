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
            "void_ray", "crystal_spider", "shard_trader", "shadow_fox", "shattered_knight");

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
