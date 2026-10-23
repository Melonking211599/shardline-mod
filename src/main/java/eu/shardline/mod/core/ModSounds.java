package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Alle Sound-Events der Mod. Die Zuordnung zu Audiodateien steht in assets/shardline/sounds.json
 * (generiert von ModSoundDefinitionsProvider). Solange keine eigenen .ogg-Dateien existieren,
 * verweisen die Events auf passende Vanilla-Sounds.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Shardline.MODID);

    /** Sound-ID -> Vanilla-Platzhalter-Event. Wird vom Datengenerator gelesen. */
    public static final Map<DeferredHolder<SoundEvent, SoundEvent>, String> PLACEHOLDERS = new LinkedHashMap<>();

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

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name, String vanillaPlaceholder) {
        DeferredHolder<SoundEvent, SoundEvent> holder = SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Shardline.id(name)));
        PLACEHOLDERS.put(holder, vanillaPlaceholder);
        return holder;
    }
}
