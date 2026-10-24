package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Alle Sound-Events der Mod. Die Zuordnung zu Audiodateien steht in assets/shardline/sounds.json
 * (generiert von ModSoundDefinitionsProvider): Events mit eigener Datei ({@link #FILES}, .ogg unter
 * assets/shardline/sounds/, importiert mit tools/assetgen/import_sounds.py) spielen diese, alle anderen
 * verweisen auf einen passenden Vanilla-Sound ({@link #PLACEHOLDERS}).
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Shardline.MODID);

    /** Sound-ID -> Vanilla-Platzhalter-Event. Wird vom Datengenerator gelesen. */
    public static final Map<DeferredHolder<SoundEvent, SoundEvent>, String> PLACEHOLDERS = new LinkedHashMap<>();
    /** Sound-ID -> eigene Datei unter assets/shardline/sounds/ (ohne .ogg); ersetzt den Platzhalter. */
    public static final Map<DeferredHolder<SoundEvent, SoundEvent>, String> FILES = new LinkedHashMap<>();

    // Splitterkriecher
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_CREEPER_HURT = register("entity.shard_creeper.hurt", "entity.creeper.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_CREEPER_DEATH = register("entity.shard_creeper.death", "entity.creeper.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_CREEPER_FUSE = register("entity.shard_creeper.fuse", "block.amethyst_block.resonate");
    // Kristallgolem
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_AMBIENT = register("entity.crystal_golem.ambient", "block.amethyst_block.chime");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_HURT = register("entity.crystal_golem.hurt", "entity.iron_golem.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_DEATH = register("entity.crystal_golem.death", "entity.iron_golem.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_ANGRY = register("entity.crystal_golem.angry", "block.amethyst_cluster.break");
    // Leuchtmotte
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_MOTH_AMBIENT = register("entity.glow_moth.ambient", "entity.allay.ambient_with_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_MOTH_HURT = register("entity.glow_moth.hurt", "entity.bee.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_MOTH_DEATH = register("entity.glow_moth.death", "entity.bee.death");
    // Aschewandler
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WALKER_AMBIENT = register("entity.ash_walker.ambient", "entity.husk.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WALKER_HURT = register("entity.ash_walker.hurt", "entity.husk.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WALKER_DEATH = register("entity.ash_walker.death", "entity.husk.death");
    // Shard-Spaeher
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_SCOUT_AMBIENT = register("entity.shard_scout.ambient", "entity.vex.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_SCOUT_HURT = register("entity.shard_scout.hurt", "entity.vex.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_SCOUT_DEATH = register("entity.shard_scout.death", "entity.vex.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_SCOUT_SHOOT = register("entity.shard_scout.shoot", "entity.breeze.shoot");
    // Void-Rochen
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_RAY_AMBIENT = register("entity.void_ray.ambient", "entity.phantom.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_RAY_HURT = register("entity.void_ray.hurt", "entity.phantom.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_RAY_DEATH = register("entity.void_ray.death", "entity.phantom.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_RAY_FLAP = register("entity.void_ray.flap", "entity.phantom.flap");
    // Kristallspinne
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SPIDER_AMBIENT = register("entity.crystal_spider.ambient", "entity.spider.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SPIDER_HURT = register("entity.crystal_spider.hurt", "entity.spider.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SPIDER_DEATH = register("entity.crystal_spider.death", "entity.spider.death");
    // Splitterhaendler
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_TRADER_AMBIENT = register("entity.shard_trader.ambient", "entity.wandering_trader.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_TRADER_HURT = register("entity.shard_trader.hurt", "entity.wandering_trader.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_TRADER_DEATH = register("entity.shard_trader.death", "entity.wandering_trader.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_TRADER_YES = register("entity.shard_trader.yes", "entity.wandering_trader.yes");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_TRADER_NO = register("entity.shard_trader.no", "entity.wandering_trader.no");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_TRADER_TRADE = register("entity.shard_trader.trade", "entity.wandering_trader.trade");
    // Schattenfuchs
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_FOX_AMBIENT = register("entity.shadow_fox.ambient", "entity.fox.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_FOX_HURT = register("entity.shadow_fox.hurt", "entity.fox.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_FOX_DEATH = register("entity.shadow_fox.death", "entity.fox.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_FOX_VANISH = register("entity.shadow_fox.vanish", "entity.illusioner.mirror_move");
    // Kristallschnecke
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SNAIL_AMBIENT = register("entity.crystal_snail.ambient", "block.amethyst_cluster.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SNAIL_HURT = register("entity.crystal_snail.hurt", "entity.slime.hurt_small");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SNAIL_DEATH = register("entity.crystal_snail.death", "entity.slime.death_small");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SNAIL_HIDE = register("entity.crystal_snail.hide", "block.amethyst_block.place");
    // Gesplitterter Ritter
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KNIGHT_AMBIENT = register("entity.shattered_knight.ambient", "entity.wither_skeleton.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KNIGHT_HURT = register("entity.shattered_knight.hurt", "entity.wither_skeleton.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KNIGHT_DEATH = register("entity.shattered_knight.death", "entity.wither_skeleton.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KNIGHT_CHARGE = register("entity.shattered_knight.charge", "entity.ravager.roar");
    // Kristallkoloss
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_AMBIENT = register("entity.crystal_colossus.ambient", "entity.warden.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_HURT = register("entity.crystal_colossus.hurt", "entity.iron_golem.damage");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_DEATH = register("entity.crystal_colossus.death", "entity.wither.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_SLAM = register("entity.crystal_colossus.slam", "entity.warden.sonic_boom");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_PILLAR = register("entity.crystal_colossus.pillar", "block.amethyst_cluster.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_SUMMON = register("entity.crystal_colossus.summon", "entity.evoker.prepare_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOSSUS_ROAR = register("entity.crystal_colossus.roar", "entity.warden.roar");
    // Splitterkoenigin
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEEN_AMBIENT = register("entity.shard_queen.ambient", "entity.allay.ambient_without_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEEN_HURT = register("entity.shard_queen.hurt", "entity.blaze.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEEN_DEATH = register("entity.shard_queen.death", "entity.ender_dragon.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEEN_TELEPORT = register("entity.shard_queen.teleport", "entity.enderman.teleport");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEEN_CAST = register("entity.shard_queen.cast", "entity.evoker.cast_spell");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEEN_SCREAM = register("entity.shard_queen.scream", "entity.ender_dragon.growl");
    // Allgemein
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_SHATTER = register("entity.shard_projectile.shatter", "block.amethyst_block.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_PORTAL_AMBIENT = register("block.shard_portal.ambient", "block.portal.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_PORTAL_TRAVEL = register("block.shard_portal.travel", "block.amethyst_block.resonate");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_KEY_USE = register("item.shard_key.use", "block.end_portal.spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_SHIELD_ACTIVATE = register("item.crystal_shield.activate", "block.amethyst_block.resonate");
    public static final DeferredHolder<SoundEvent, SoundEvent> GAUNTLET_SLAM = register("item.colossus_gauntlet.slam", "entity.warden.attack_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEART_BLINK = register("item.heart_of_the_expanse.blink", "entity.enderman.teleport");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARDBLADE_VOLLEY = register("item.queens_shardblade.volley", "entity.breeze.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_SPLITTERWEITE = register("music.splitterweite", "music.end");
    // Story
    public static final DeferredHolder<SoundEvent, SoundEvent> STORY_CHAPTER = register("story.chapter", "ui.toast.challenge_complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> MEMORY_CRYSTAL_VISION = register("block.memory_crystal.vision", "block.beacon.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> LORE_BOOK_OPEN = register("item.lore_book.open", "item.book.page_turn");
    // Virus
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_OUTBREAK = register("event.virus_outbreak", "entity.wither.spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_SEALED = register("event.virus_sealed", "block.beacon.power_select");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_DOMINATED = register("event.virus_dominated", "entity.warden.emerge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PURITY_ANCHOR_ACTIVATE = register("block.purity_anchor.activate", "block.conduit.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> PURITY_ANCHOR_GROW = register("block.purity_anchor.grow", "block.amethyst_block.resonate");
    public static final DeferredHolder<SoundEvent, SoundEvent> DECISION_ALTAR_RISE = register("block.decision_altar.rise", "block.end_portal.spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> DECISION_ALTAR_AWAKEN = register("block.decision_altar.awaken", "block.beacon.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> DECISION_ALTAR_USE = register("block.decision_altar.use", "block.respawn_anchor.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PURIFICATION_ESSENCE_USE = register("item.purification_essence.use", "block.amethyst_block.resonate");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_ZOMBIE_AMBIENT = register("entity.shard_zombie.ambient", "entity.zombie.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_ZOMBIE_HURT = register("entity.shard_zombie.hurt", "entity.zombie.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_ZOMBIE_DEATH = register("entity.shard_zombie.death", "entity.zombie.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_COW_AMBIENT = register("entity.crystallized_cow.ambient", "entity.cow.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_COW_HURT = register("entity.crystallized_cow.hurt", "entity.cow.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_COW_DEATH = register("entity.crystallized_cow.death", "entity.cow.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_SHEEP_AMBIENT = register("entity.crystallized_sheep.ambient", "entity.sheep.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_SHEEP_HURT = register("entity.crystallized_sheep.hurt", "entity.sheep.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_SHEEP_DEATH = register("entity.crystallized_sheep.death", "entity.sheep.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_PIG_AMBIENT = register("entity.crystallized_pig.ambient", "entity.pig.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_PIG_HURT = register("entity.crystallized_pig.hurt", "entity.pig.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_PIG_DEATH = register("entity.crystallized_pig.death", "entity.pig.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_VILLAGER_AMBIENT = register("entity.crystallized_villager.ambient", "entity.zombie_villager.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_VILLAGER_HURT = register("entity.crystallized_villager.hurt", "entity.zombie_villager.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_VILLAGER_DEATH = register("entity.crystallized_villager.death", "entity.zombie_villager.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_PIGLIN_AMBIENT = register("entity.crystallized_piglin.ambient", "entity.zombified_piglin.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_PIGLIN_HURT = register("entity.crystallized_piglin.hurt", "entity.zombified_piglin.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_PIGLIN_DEATH = register("entity.crystallized_piglin.death", "entity.zombified_piglin.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_ENDERMAN_AMBIENT = register("entity.crystallized_enderman.ambient", "entity.enderman.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_ENDERMAN_HURT = register("entity.crystallized_enderman.hurt", "entity.enderman.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTALLIZED_ENDERMAN_DEATH = register("entity.crystallized_enderman.death", "entity.enderman.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFECTED_CURED = register("entity.infected.cured", "entity.zombie_villager.converted");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_AMBIENT = register("entity.virus_heart.ambient", "entity.warden.heartbeat");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_HURT = register("entity.virus_heart.hurt", "block.amethyst_block.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_DEATH = register("entity.virus_heart.death", "block.respawn_anchor.deplete");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_PULSE = register("entity.virus_heart.pulse", "entity.warden.sonic_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_PHASE = register("entity.virus_heart.phase", "entity.elder_guardian.curse");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_TENDRILS = register("entity.virus_heart.tendrils", "entity.evoker_fangs.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> VIRUS_HEART_SUMMON = register("entity.virus_heart.summon", "entity.evoker.prepare_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABILITY_SHARD_CALL = register("ability.shard_call", "entity.evoker.prepare_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABILITY_CRYSTAL_WAVE = register("ability.crystal_wave", "entity.warden.sonic_boom");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_INFECTED = register("music.infected", "music.nether.soul_sand_valley");
    // Nach dem Ausbruch: Dialoge, Auftraege, Kapitel 7-9
    public static final DeferredHolder<SoundEvent, SoundEvent> RHEA_VOICE = register("entity.rhea.voice", "entity.villager.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHEA_HURT = register("entity.rhea.hurt", "entity.player.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHEA_DEATH = register("entity.rhea.death", "entity.player.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEYL_VOICE = register("entity.veyl.voice", "entity.allay.ambient_without_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEYL_HURT = register("entity.veyl.hurt", "entity.allay.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEYL_DEATH = register("entity.veyl.death", "entity.allay.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ILSA_VOICE = register("entity.ilsa.voice", "entity.wandering_trader.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ILSA_HURT = register("entity.ilsa.hurt", "entity.player.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ILSA_DEATH = register("entity.ilsa.death", "entity.player.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> KESH_VOICE = register("entity.kesh.voice", "entity.zombie_villager.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> KESH_HURT = register("entity.kesh.hurt", "entity.player.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KESH_DEATH = register("entity.kesh.death", "entity.player.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> FESTERING_NODE_HUM = register("block.festering_node.hum", "block.sculk_catalyst.bloom");
    public static final DeferredHolder<SoundEvent, SoundEvent> FESTERING_NODE_PURIFY = register("block.festering_node.purify", "block.amethyst_block.resonate");
    public static final DeferredHolder<SoundEvent, SoundEvent> FESTERING_NODE_BIND = register("block.festering_node.bind", "block.respawn_anchor.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> FESTERING_NODE_SCREAM = register("block.festering_node.scream", "block.sculk_shrieker.shriek");
    public static final DeferredHolder<SoundEvent, SoundEvent> TUNING_FORK_RING = register("item.keeper_tuning_fork.ring", "block.bell.resonate");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIALOGUE_OPEN = register("ui.dialogue.open", "item.book.page_turn");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIALOGUE_CLOSE = register("ui.dialogue.close", "item.book.put");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIALOGUE_TYPE = register("ui.dialogue.type", "block.amethyst_block.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> DIALOGUE_CHOICE = register("ui.dialogue.choice", "ui.button.click");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEST_UPDATE = register("ui.quest.update", "entity.experience_orb.pickup");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUEST_COMPLETE = register("ui.quest.complete", "ui.toast.challenge_complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> STORY_EPILOGUE = register("story.epilogue", "music_disc.otherside");
    public static final DeferredHolder<SoundEvent, SoundEvent> AFTERSHOCK = register("event.aftershock", "entity.warden.dig");
    // Eigene Klaenge ohne bisheriges Event
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_FRAGMENT_PICKUP = register("item.shard_fragment.pickup", "entity.item.pickup");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_ORE_BREAK = register("block.shard_ore.break", "block.amethyst_cluster.break");

    /** Shard-Erz: eigener Abbauklang, sonst Calcit bzw. Tiefenschiefer. */
    public static final SoundType SHARD_ORE_SOUND = new DeferredSoundType(1.0F, 1.0F, SHARD_ORE_BREAK,
            () -> SoundEvents.CALCITE_STEP, () -> SoundEvents.CALCITE_PLACE, () -> SoundEvents.CALCITE_HIT, () -> SoundEvents.CALCITE_FALL);
    public static final SoundType DEEPSLATE_SHARD_ORE_SOUND = new DeferredSoundType(1.0F, 1.0F, SHARD_ORE_BREAK,
            () -> SoundEvents.DEEPSLATE_STEP, () -> SoundEvents.DEEPSLATE_PLACE, () -> SoundEvents.DEEPSLATE_HIT, () -> SoundEvents.DEEPSLATE_FALL);

    static {
        // Gelieferte Soundeffekte (tools/assetgen/import_sounds.py)
        FILES.put(COLOSSUS_ROAR, "entity/crystal_colossus/roar");
        FILES.put(COLOSSUS_SLAM, "entity/crystal_colossus/slam");
        FILES.put(QUEEN_CAST, "entity/shard_queen/cast");
        FILES.put(QUEEN_SCREAM, "entity/shard_queen/scream");
        FILES.put(QUEEN_TELEPORT, "entity/shard_queen/teleport");
        FILES.put(SHARD_ZOMBIE_AMBIENT, "entity/shard_zombie/ambient");
        FILES.put(CRYSTALLIZED_COW_DEATH, "entity/crystallized_cow/death");
        FILES.put(VIRUS_HEART_PULSE, "entity/virus_heart/pulse");
        FILES.put(SHARD_PORTAL_TRAVEL, "block/shard_portal/travel");
        FILES.put(SHARD_KEY_USE, "block/shard_portal/travel");          // Portal oeffnen: derselbe Klang
        FILES.put(MEMORY_CRYSTAL_VISION, "block/memory_crystal/vision");
        FILES.put(PURITY_ANCHOR_ACTIVATE, "block/purity_anchor/activate");
        FILES.put(SHARD_ORE_BREAK, "block/shard_ore/break");
        FILES.put(LORE_BOOK_OPEN, "item/lore_book/open");
        FILES.put(PURIFICATION_ESSENCE_USE, "item/purification_essence/use");
        FILES.put(SHARD_FRAGMENT_PICKUP, "item/shard_fragment/pickup");
        FILES.put(ABILITY_SHARD_CALL, "ability/shard_call");
        FILES.put(ABILITY_CRYSTAL_WAVE, "ability/crystal_wave");
        FILES.put(STORY_CHAPTER, "story/chapter");
        FILES.put(VIRUS_OUTBREAK, "event/virus_outbreak");
        FILES.put(VIRUS_SEALED, "event/virus_sealed");
        FILES.put(VIRUS_DOMINATED, "event/virus_dominated");
        FILES.put(MUSIC_SPLITTERWEITE, "music/splitterweite");
    }

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name, String vanillaPlaceholder) {
        DeferredHolder<SoundEvent, SoundEvent> holder = SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Shardline.id(name)));
        PLACEHOLDERS.put(holder, vanillaPlaceholder);
        return holder;
    }
}
