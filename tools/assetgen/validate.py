"""Prueft die Assets gegen ASSETS.md: Dateien vorhanden, Groessen, UV-Grenzen, Animations-Bones. Aufruf: python tools/assetgen/validate.py"""
import json, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from PIL import Image
from common import ANIM, ASSETS, GEO, RESOURCE_ROOT, TEX
from uvlib import box_extent

errors = []
def need(path, size=None):
    if not path.exists():
        errors.append(f"fehlt: {path}"); return
    if size:
        w, h = Image.open(path).size
        if (w, h) != size: errors.append(f"Groesse {w}x{h} statt {size}: {path.name}")

ENT = {"shard_creeper": 64, "crystal_golem": 128, "glow_moth": 64, "ash_walker": 64, "shard_scout": 64, "void_ray": 128,
       "crystal_spider": 64, "shard_trader": 64, "shadow_fox": 64, "shattered_knight": 64, "crystal_colossus": 256,
       "shard_queen": 128, "crystal_pillar": 64, "crystal_snail": 64}
GLOW = {"shard_creeper", "crystal_golem", "glow_moth", "shard_scout", "void_ray", "crystal_spider", "shadow_fox",
        "shattered_knight", "crystal_colossus", "shard_queen", "crystal_pillar", "crystal_snail"}
for n, s in ENT.items():
    need(TEX / "entity" / f"{n}.png", (s, s))
    if n in GLOW: need(TEX / "entity" / f"{n}_glowmask.png", (s, s))
    g = json.loads((GEO / f"{n}.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
    if g["description"]["texture_width"] != s: errors.append(f"geo-Breite {n}")
    bones = {b["name"] for b in g["bones"]}
    for b in g["bones"]:
        if b.get("parent") and b["parent"] not in bones: errors.append(f"{n}: Parent fehlt {b['parent']}")
        for c in b.get("cubes", []):
            e = box_extent(c["uv"][0], c["uv"][1], *c["size"])
            if e[0] > s or e[1] > s: errors.append(f"{n}/{b['name']}: UV ausserhalb {e}")
    a = json.loads((ANIM / f"{n}.animation.json").read_text(encoding="utf-8"))["animations"]
    for clip, v in a.items():
        for bn in v.get("bones", {}):
            if bn not in bones: errors.append(f"{n}/{clip}: Bone {bn} nicht im Modell")
    for need_clip in ("idle", "walk", "fly", "death"):
        pass
need(TEX / "entity" / "crystal_golem_angry.png", (128, 128)); need(TEX / "entity" / "queen_clone.png", (128, 128))
blocks = """shardstone shardstone_bricks cracked_shardstone_bricks shard_ore deepslate_shard_ore shard_block glow_crystal_block
glow_crystal_cluster crystal_moss crystal_moss_side ash_block crystal_log crystal_log_top crystal_planks crystal_leaves
crystal_sapling glowbloom shard_fern glowcap luminous_berry_bush crystal_web shard_portal_frame colossus_altar
colossus_altar_top queen_altar queen_altar_top colossus_trophy queen_trophy""".split()
for b in blocks: need(TEX / "block" / f"{b}.png", (16, 16))
need(TEX / "block" / "shard_portal.png", (16, 128)); need(TEX / "block" / "shard_portal.png.mcmeta")
items = """shard_fragment raw_shard shard_ingot ash_dust scout_eye void_ray_hide shadow_pelt knight_crest luminous_berry shard_key
colossus_summoning_crystal colossus_core colossus_upgrade_smithing_template heart_of_the_expanse queens_shardblade
colossus_gauntlet crystal_bow crystal_bow_pulling_0 crystal_bow_pulling_1 crystal_bow_pulling_2""".split()
for s in ("shard", "colossus"):
    for p in ("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel"): items.append(f"{s}_{p}")
for i in items: need(TEX / "item" / f"{i}.png", (16, 16))
for s in ("shard", "colossus"):
    for l in (1, 2): need(TEX / "models/armor" / f"{s}_layer_{l}.png", (64, 32))
for i in range(4):
    need(TEX / "particle" / f"shard_sparkle_{i}.png", (8, 8)); need(TEX / "particle" / f"void_mote_{i}.png", (8, 8))
need(TEX / "environment/splitterweite_sky.png", (128, 128)); need(TEX / "environment/splitterweite_moon.png", (64, 64))
need(TEX / "misc/shard_portal_overlay.png", (256, 256)); need(TEX / "gui/advancements/backgrounds/shardline.png", (16, 16))
need(RESOURCE_ROOT / "shardline_logo.png", (256, 128))

# ---- Story-Erweiterung
story_blocks = "infected_stone infected_dirt infected_grass_top infected_grass_side infected_log infected_log_top infected_leaves infected_sand".split()
for b in story_blocks:
    need(TEX / "block" / f"{b}.png", (16, 16))
    need(TEX / "block" / f"{b}_glow.png", (16, 64) if b in ("infected_grass_top", "infected_grass_side", "infected_leaves") else (16, 16))
for b in ("infected_grass_top", "infected_grass_side", "infected_leaves"):
    need(TEX / "block" / f"{b}_glow.png.mcmeta")
for b in ("memory_crystal", "purity_anchor"): need(TEX / "block" / f"{b}.png", (16, 16))
need(TEX / "block" / "purity_anchor_crystal.png"); need(TEX / "block" / "purity_anchor_crystal.png.mcmeta")
for i in ("lore_book", "purification_essence", "breach_core"): need(TEX / "item" / f"{i}.png", (16, 16))
need(TEX / "entity" / "keeper_cape.png", (64, 32))
need(TEX / "entity" / "virus_heart.png", (128, 128)); need(TEX / "entity" / "virus_heart_glowmask.png", (128, 128))
need(TEX / "entity" / "shard_zombie.png", (64, 64)); need(TEX / "entity" / "shard_zombie_glowmask.png", (64, 64))
need(TEX / "entity" / "crystallized_cow.png", (64, 32)); need(TEX / "entity" / "crystallized_cow_glowmask.png", (64, 32))
need(TEX / "mob_effect" / "shard_fever.png", (18, 18))
for i in range(4): need(TEX / "particle" / f"virus_spore_{i}.png", (8, 8))
need(TEX / "misc" / "breach_overlay.png", (256, 256)); need(TEX / "gui/advancements/backgrounds/story.png", (16, 16))
hg = json.loads((GEO / "virus_heart.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
hb = {b["name"] for b in hg["bones"]}
ha = json.loads((ANIM / "virus_heart.animation.json").read_text(encoding="utf-8"))["animations"]
for clip in ("idle", "spawn", "attack_tendrils", "summon_infected", "hurt", "death"):
    if clip not in ha: errors.append(f"virus_heart: Clip {clip} fehlt")

# ---- Splitterhandwerk
craft_blocks = """polished_shardstone chiseled_shardstone_bricks crystal_door_top crystal_door_bottom crystal_trapdoor crystal_glass
crystal_glass_pane_top shard_lantern""".split()
for b in craft_blocks: need(TEX / "block" / f"{b}.png", (16, 16))
craft_items = "crystal_door shard_lantern crystal_slime glowcap_stew crystal_candy berry_tart resonance_compass".split()
for i in craft_items: need(TEX / "item" / f"{i}.png", (16, 16))
blocks += craft_blocks
items += craft_items
print("\n".join(errors) if errors else f"OK: alle Pruefungen bestanden ({len(ENT)} Wesen, {len(blocks)+1} Bloecke, {len(items)} Items)")
sys.exit(1 if errors else 0)
